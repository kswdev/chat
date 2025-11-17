package net.study.messagesystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.IdKey;
import net.study.messagesystem.constant.KeyPrefix;
import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.user.UserId;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final CacheService cacheService;
    private final long TTL = 300;

    public Optional<String> getListenTopic(UserId userId) {
        return cacheService.get(buildUserLocationKey(userId));
    }

    public Map<String, List<UserId>> getListenTopics(Collection<UserId> userIds) {
        List<UserId> userIdList = new ArrayList<>(userIds);

        List<String> keys = userIdList.stream()
                .map(this::buildUserLocationKey)
                .toList();

        List<String> listenTopics = cacheService.get(keys);

        return IntStream.range(0, userIdList.size())
                .mapToObj(i -> Map.entry(listenTopics.get(i), userIdList.get(i)))
                .filter(entry -> entry.getKey() != null)
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
    }

    public List<UserId> getOnlineParticipantUserIds(ChannelId channelId, List<UserId> userIds) {
        List<String> channelIdKeys = userIds.stream()
                .map(this::buildChannelIdKey)
                .toList();

        List<String> channelIds = cacheService.get(channelIdKeys);

        if (channelIds == null || channelIds.isEmpty()) {
            return List.of();
        }

        List<UserId> onlineParticipantUserIds = new ArrayList<>(userIds.size());

        for (int i = 0; i < userIds.size(); i++) {
            String value = channelIds.get(i);
            onlineParticipantUserIds.add(
                    value != null && value.equals(channelId.id().toString())
                            ? userIds.get(i)
                            : null
            );
        }

        return onlineParticipantUserIds;
    }

    public boolean deActiveChannel(UserId userId) {
        String channelIdKey = buildChannelIdKey(userId);
        return cacheService.delete(channelIdKey);
    }

    public boolean setActiveChannel(UserId userId, ChannelId channelId) {
        String channelIdKey = buildChannelIdKey(userId);
        return cacheService.set(channelIdKey, channelId.id().toString(), TTL);
    }

    private String buildChannelIdKey(UserId userId) {
        return cacheService.buildKey(KeyPrefix.USER, userId.id().toString(), IdKey.CHANNEL_ID.getValue());
    }

    private String buildUserLocationKey(UserId userId) {
        return cacheService.buildKey(KeyPrefix.USER_SESSION, userId.id().toString());
    }
}
