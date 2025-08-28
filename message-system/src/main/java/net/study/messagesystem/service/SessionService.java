package net.study.messagesystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.IdKey;
import net.study.messagesystem.dto.domain.channel.ChannelId;
import net.study.messagesystem.dto.domain.user.UserId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository<? extends Session> httpSessionRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final long TTL = 300;

    public String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    public boolean setActiveChannel(UserId userId, ChannelId channelId) {
        String channelIdKey = buildChannelIdKey(userId);

        try {
            stringRedisTemplate.opsForValue().set(channelIdKey, channelId.id().toString(), TTL, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            log.error("Redis set key failed. key: {}, channelId: {}", channelIdKey, channelId);
            return false;
        }
    }

    public boolean isOnline(UserId userId, ChannelId channelId) {
        String channelIdKey = buildChannelIdKey(userId);
        try {
            String chId = stringRedisTemplate.opsForValue().get(channelIdKey);
            if (chId != null && chId.equals(channelId.id().toString())) {
                return true;
            }
        } catch (Exception e) {
            log.error("Redis get failed. key: {}, cause: {}", channelIdKey, e.getMessage());
        }

        return false;
    }

    public void refreshTTL(UserId userId, String httpSessionId) {
        String channelIdKey = buildChannelIdKey(userId);

        try {
            Session httpSession = httpSessionRepository.findById(httpSessionId);
            if (httpSession != null) {
                httpSession.setLastAccessedTime(Instant.now());
                stringRedisTemplate.expire(channelIdKey, TTL, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.error("Redis expire failed. key: {}", channelIdKey);
        }
    }

    private String buildChannelIdKey(UserId userId) {
        String NAMESPACE = "message:user-session";
        return "%s:%d:%s".formatted(NAMESPACE, userId.id(), IdKey.CHANNEL_ID);
    }
}
