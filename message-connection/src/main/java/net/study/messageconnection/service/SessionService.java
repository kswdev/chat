package net.study.messageconnection.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.IdKey;
import net.study.messageconnection.constant.KeyPrefix;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.kafka.ListenTopicCreator;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

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

    public boolean deActiveChannel(UserId userId) {
        String channelIdKey = buildChannelIdKey(userId);
        return cacheService.delete(channelIdKey);
    }

    public void refreshTTL(UserId userId) {
        cacheService.expire(buildChannelIdKey(userId), TTL);
        cacheService.expire(buildUserLocationKey(userId), TTL);
    }

    private String buildChannelIdKey(UserId userId) {
        return cacheService.buildKey(KeyPrefix.USER, userId.id().toString(), IdKey.CHANNEL_ID.getValue());
    }

    private String buildUserLocationKey(UserId userId) {
        return cacheService.buildKey(KeyPrefix.USER_SESSION, userId.id().toString());
    }
}
