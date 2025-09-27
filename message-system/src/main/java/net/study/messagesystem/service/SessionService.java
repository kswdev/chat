package net.study.messagesystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.config.KeyPrefix;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    public boolean deActiveChannel(UserId userId) {
        String channelIdKey = buildChannelIdKey(userId);

        try {
            stringRedisTemplate.delete(channelIdKey);
            return true;
        } catch (Exception e) {
            log.error("Redis delete key failed. key: {}", channelIdKey);
            return false;
        }
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

    public List<UserId> getOnlineParticipantUserIds(ChannelId channelId, List<UserId> userIds) {
        List<String> channelIdKeys = userIds.stream().map(this::buildChannelIdKey).toList();

        try {
            List<String> channelIds = stringRedisTemplate.opsForValue().multiGet(channelIdKeys);
            assert channelIds != null;
            if (!channelIds.isEmpty()) {
                List<UserId> onlineParticipantUserIds = new ArrayList<>(userIds.size());
                for (int i = 0; i < userIds.size(); i++) {
                    String value = channelIds.get(i);
                    onlineParticipantUserIds.add(value != null && value.equals(channelId.id().toString()) ? userIds.get(i) : null);
                }
                return onlineParticipantUserIds;
            }
        } catch (Exception e) {
            log.error("Redis get failed. channelId: {}, cause: {}", channelId, e.getMessage());
        }
        return Collections.emptyList();
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
        String NAMESPACE = KeyPrefix.USER;
        return "%s:%d:%s".formatted(NAMESPACE, userId.id(), IdKey.CHANNEL_ID.getValue());
    }
}
