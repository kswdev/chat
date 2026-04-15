package net.study.messageconnectionflux.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.IdKey;
import net.study.messagecommon.constant.KeyPrefix;
import net.study.messageconnectionflux.domain.user.UserId;
import net.study.messageconnectionflux.kafka.ListenTopicCreator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final ListenTopicCreator listenTopicCreator;
    private final CacheService cacheService;
    private final long TTL = 300;

    public void setOnline(UserId userId, boolean status) {
        String key = buildUserLocationKey(userId);
        if (status)
            cacheService.set(key, listenTopicCreator.getListenTopic(), TTL);
        else
            cacheService.delete(key);
    }

    public Mono<Long> deActiveChannel(UserId userId) {
        String channelIdKey = buildChannelIdKey(userId);
        return cacheService.delete(channelIdKey);
    }

    public void refreshTTL(UserId userId) {
        cacheService.expire(buildChannelIdKey(userId), TTL);
        cacheService.expire(buildUserLocationKey(userId), TTL);
    }

    private String buildChannelIdKey(UserId userId) {
        return buildKey(KeyPrefix.USER, userId.id().toString(), IdKey.CHANNEL_ID.getValue());
    }

    private String buildUserLocationKey(UserId userId) {
        return buildKey(KeyPrefix.USER_SESSION, userId.id().toString());
    }

    private String buildKey(String prefix, String key) {
        return "%s:%s".formatted(prefix, key);
    }

    private String buildKey(String prefix, String firstKey, String secondKey) {
        return "%s:%s:%s".formatted(prefix, firstKey, secondKey);
    }
}
