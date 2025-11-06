package net.study.messagesystem.service;

import com.mysema.commons.lang.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.KeyPrefix;
import net.study.messagesystem.constant.ResultType;
import net.study.messagesystem.domain.user.InviteCode;
import net.study.messagesystem.domain.user.User;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.projection.*;
import net.study.messagesystem.entity.user.UserEntity;
import net.study.messagesystem.repository.UserRepository;
import net.study.messagesystem.util.JsonUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final CacheService cacheService;
    private final UserRepository userRepository;

    private final JsonUtil jsonUtil;
    private final long TTL = 3600;
    private final long LIMIT_FIND_COUNT = 100;

    @Transactional(readOnly = true)
    public Optional<String> getUsername(UserId userId) {
        String key = cacheService.buildKey(KeyPrefix.USERNAME, userId.id().toString());
        return cacheService.get(key)
                .or(() -> userRepository.findByUserId(userId.id())
                        .map(UsernameProjection::getUsername)
                        .map(username -> {
                                    cacheService.set(key, username, TTL);
                                    return username;
                                }
                        ));
    }

    @Transactional(readOnly = true)
    public Pair<Map<UserId, String>, ResultType> getUsernames(Set<UserId> userIds) {
        if (userIds.size() > LIMIT_FIND_COUNT) {
            return Pair.of(Collections.emptyMap(), ResultType.OVER_LIMIT);
        }

        Map<UserId, String> cachedUsernames = getCachedUsernames(userIds);
        Map<UserId, String> allUsernames = fetchMissingUsernames(userIds, cachedUsernames);

        return Pair.of(allUsernames, ResultType.SUCCESS);
    }

    @Transactional(readOnly = true)
    public Optional<Long> getConnectionCount(UserId userId) {
        return userRepository.findCountByUserId(userId.id()).map(ConnectionCountProjection::getConnectionCount);
    }

    @Transactional(readOnly = true)
    public Optional<InviteCode> getInviteCode(UserId userId) {
        return userRepository.findInviteCodeByUserId(userId.id()).map(InviteCodeProjection::getInviteCode).map(InviteCode::new);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserIdName(InviteCode inviteCode) {
        String key = cacheService.buildKey(KeyPrefix.USER, inviteCode.code());
        return cacheService.get(key)
                .flatMap(user -> jsonUtil.fromJson(user, User.class))
                .or(() -> userRepository.findByInviteCode(inviteCode.code())
                        .map(user -> {
                            User u = new User(new UserId(user.getUserId()), user.getUsername());
                            jsonUtil.toJson(u).ifPresent(json -> cacheService.set(key, json, TTL));
                            return u;
                        })
                );
    }

    @Transactional(readOnly = true)
    public Optional<UserId> getUserId(String username) {
        String key = cacheService.buildKey(KeyPrefix.USER_ID, username);
        return cacheService.get(key)
                .map(Long::valueOf)
                .map(UserId::new)
                .or(() -> userRepository.findUserIdByUsername(username)
                        .map(UserIdProjection::getUserId)
                        .map(userId -> {
                            cacheService.set(key, userId.toString(), TTL);
                            return new UserId(userId);
                        }));
    }

    @Transactional(readOnly = true)
    public List<UserId> getUserIds(List<String> usernames) {
        return userRepository.findUserIdByUsernameIn(usernames).stream().map(UserIdProjection::getUserId).map(UserId::new).toList();
    }

    public UserEntity getUserReference(UserId userId) {
        return userRepository.getReferenceById(userId.id());
    }

    private Map<UserId, String> getCachedUsernames(Set<UserId> userIds) {
        List<UserId> userIdList = new ArrayList<>(userIds);
        List<String> cacheKeys = userIdList.stream()
                .map(userId -> cacheService.buildKey(KeyPrefix.USERNAME, userId.id().toString()))
                .toList();

        List<String> cachedValues = cacheService.get(cacheKeys);

        return IntStream.range(0, userIdList.size())
                .filter(i -> cachedValues.get(i) != null)
                .boxed()
                .collect(Collectors.toMap(
                        userIdList::get,
                        cachedValues::get
                ));
    }

    private Map<UserId, String> fetchMissingUsernames(Set<UserId> allUserIds, Map<UserId, String> cachedUsernames) {
        Set<UserId> missingUserIds = allUserIds.stream()
                .filter(userId -> !cachedUsernames.containsKey(userId))
                .collect(Collectors.toSet());

        if (missingUserIds.isEmpty()) {
            return cachedUsernames;
        }

        Map<UserId, String> dbUsernames = userRepository
                .findUserIdAndUsernameByUserIdIn(missingUserIds.stream().map(UserId::id).toList())
                .stream()
                .collect(Collectors.toMap(
                        proj -> new UserId(proj.getUserId()),
                        UserIdUsernameProjection::getUsername
                ));

        // 캐시 저장
        cacheService.set(
                dbUsernames.entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> cacheService.buildKey(KeyPrefix.USERNAME, entry.getKey().id().toString()),
                                Map.Entry::getValue
                        )),
                TTL
        );

        // 모든 결과 병합
        return Stream.of(cachedUsernames, dbUsernames)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
