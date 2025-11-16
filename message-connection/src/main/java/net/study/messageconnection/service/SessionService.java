package net.study.messageconnection.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.constant.KeyPrefix;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.kafka.ListenTopicCreator;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository<? extends Session> httpSessionRepository;
    private final ListenTopicCreator listenTopicCreator;
    private final CacheService cacheService;
    private final long TTL = 300;

    public void setOnline(UserId userId, boolean status) {
        String key = buildUserLocationKey(userId);
        if (status)
            cacheService.set(key, listenTopicCreator.getConsumeTopic(), TTL);
        else
            cacheService.delete(key);
    }

    public boolean deActiveChannel(UserId userId) {
        String channelIdKey = buildChannelIdKey(userId);
        return cacheService.delete(channelIdKey);
    }

    public void refreshTTL(UserId userId, String httpSessionId) {
        try {
            Session httpSession = httpSessionRepository.findById(httpSessionId);
            if (httpSession != null) {
                httpSession.setLastAccessedTime(Instant.now());
                cacheService.expire(buildChannelIdKey(userId), TTL);
                cacheService.expire(buildUserLocationKey(userId), TTL);
            }
        } catch (Exception e) {
            log.error("Redis find failed. id: {}, cause:{}", httpSessionId, e.getMessage());
        }
    }

    private String buildChannelIdKey(UserId userId) {
        return cacheService.buildKey(KeyPrefix.USER, userId.id().toString(), IdKey.CHANNEL_ID.getValue());
    }

    private String buildUserLocationKey(UserId userId) {
        return cacheService.buildKey(KeyPrefix.USER_SESSION, userId.id().toString());
    }
}
