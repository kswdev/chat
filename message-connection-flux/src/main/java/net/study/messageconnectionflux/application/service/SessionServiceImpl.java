package net.study.messageconnectionflux.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.IdKey;
import net.study.messagecommon.constant.KeyPrefix;
import net.study.messageconnectionflux.application.port.in.SessionService;
import net.study.messageconnectionflux.application.port.out.CachePort;
import net.study.messageconnectionflux.domain.user.UserId;
import net.study.messageconnectionflux.adpter.kafka.ListenTopicCreator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final ListenTopicCreator listenTopicCreator;
    private final CachePort cachePort;
    private final long TTL = 300;

    public Mono<Boolean> setOnline(UserId userId, boolean status) {
        String key = buildUserLocationKey(userId);
        if (status)
            return cachePort.set(key, listenTopicCreator.getListenTopic(), TTL);
        else {
            cachePort.delete(key);
            return Mono.just(true);
        }
    }

    public Mono<Long> deActiveChannel(UserId userId) {
        String channelIdKey = buildChannelIdKey(userId);
        return cachePort.delete(channelIdKey);
    }

    public Mono<Boolean> refreshTTL(UserId userId) {
        Mono<Boolean> channelIdKey = cachePort.expire(buildChannelIdKey(userId), TTL);
        Mono<Boolean> userLocationKey = cachePort.expire(buildUserLocationKey(userId), TTL);
        return channelIdKey.zipWith(userLocationKey,
                (a, b) -> a && b);
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
