package net.study.messagesystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.KeyPrefix;
import net.study.messagesystem.constant.IdKey;
import net.study.messagesystem.dto.domain.channel.ChannelId;
import net.study.messagesystem.dto.domain.user.UserId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository<? extends Session> httpSessionRepository;
    private final CacheService cacheService;
    private final long TTL = 300;

    public boolean deActiveChannel(UserId userId) {
        String channelIdKey = buildChannelIdKey(userId);
        return cacheService.delete(channelIdKey);
    }

    public boolean setActiveChannel(UserId userId, ChannelId channelId) {
        String channelIdKey = buildChannelIdKey(userId);
        return cacheService.set(channelIdKey, channelId.id().toString(), TTL);
    }

    public List<UserId> getOnlineParticipantUserIds(ChannelId channelId, List<UserId> userIds) {
        List<String> channelIdKeys = userIds.stream().map(this::buildChannelIdKey).toList();
        List<String> channelIds = cacheService.get(channelIdKeys);

        assert channelIds != null;

        if (!channelIds.isEmpty()) {
            List<UserId> onlineParticipantUserIds = new ArrayList<>(userIds.size());
            for (int i = 0; i < userIds.size(); i++) {
                String value = channelIds.get(i);
                onlineParticipantUserIds.add(value != null && value.equals(channelId.id().toString()) ? userIds.get(i) : null);
            }
            return onlineParticipantUserIds;
        }

        return Collections.emptyList();
    }

    public void refreshTTL(UserId userId, String httpSessionId) {
        String channelIdKey = buildChannelIdKey(userId);

        try {
            Session httpSession = httpSessionRepository.findById(httpSessionId);
            if (httpSession != null) {
                httpSession.setLastAccessedTime(Instant.now());
                cacheService.expire(channelIdKey, TTL);
            }
        } catch (Exception e) {
            log.error("Redis find failed. id: {}, cause:{}", httpSessionId, e.getMessage());
        }
    }

    private String buildChannelIdKey(UserId userId) {
        return cacheService.buildKey(KeyPrefix.USER, userId.id().toString(), IdKey.CHANNEL_ID.getValue());
    }
}
