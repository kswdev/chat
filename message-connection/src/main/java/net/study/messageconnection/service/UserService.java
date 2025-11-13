package net.study.messageconnection.service;

import com.mysema.commons.lang.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.KeyPrefix;
import net.study.messageconnection.constant.ResultType;
import net.study.messageconnection.domain.user.InviteCode;
import net.study.messageconnection.domain.user.User;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.entity.user.UserEntity;
import net.study.messageconnection.repository.UserRepository;
import net.study.messageconnection.util.JsonUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

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
        return cacheService
                .get(key)
                .or(() ->
                        userRepository.findByUserId(userId.id())
                                .map(UsernameProjection::getUsername)
                                .map(username -> {
                                    cacheService.set(key, username, TTL);
                                    return username;
                                })
                );
    }

    @Transactional(readOnly = true)
    public Pair<Map<UserId, String>, ResultType> getUsernames(Set<UserId> userIds) {
        return Optional.of(userIds)
                .filter(ids -> ids.size() <= LIMIT_FIND_COUNT)
                .map(this::processUsernames)
                .orElse(Pair.of(Collections.emptyMap(), ResultType.OVER_LIMIT));
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

    private Pair<Map<UserId, String>, ResultType> processUsernames(Set<UserId> userIds) {
        Map<UserId, String> cachedUsernames = getCachedUsernames(userIds);

        return findMissingUserIds(userIds, cachedUsernames)
                .map(missingIds -> fetchAndCacheUsernames(missingIds, cachedUsernames))
                .orElse(Pair.of(cachedUsernames, ResultType.SUCCESS));
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

    private Optional<Set<UserId>> findMissingUserIds(Set<UserId> userIds, Map<UserId, String> cachedUsernames) {
        Set<UserId> missingIds = userIds.stream()
                .filter(not(cachedUsernames::containsKey))
                .collect(Collectors.toUnmodifiableSet());

        return missingIds.isEmpty() ? Optional.empty() : Optional.of(missingIds);
    }

    private Pair<Map<UserId, String>, ResultType> fetchAndCacheUsernames(Set<UserId> missingUserIds, Map<UserId, String> cachedUsernames) {
        return Optional.of(missingUserIds)
                .map(this::fetchMissingUsernames)
                .map(missingUsernames -> {
                    cacheMissingUsernames(missingUsernames);
                    return mergeUsernames(cachedUsernames, missingUsernames);
                })
                .map(allUsernames -> Pair.of(allUsernames, ResultType.SUCCESS))
                .orElse(Pair.of(cachedUsernames, ResultType.SUCCESS));
    }

    private void cacheMissingUsernames(Map<UserId, String> missingUsernames) {
        Optional.of(missingUsernames)
                .map(this::buildCacheEntries)
                .ifPresent(entries -> cacheService.set(entries, TTL));
    }

    private Map<String, String> buildCacheEntries(Map<UserId, String> usernames) {
        return usernames.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> cacheService.buildKey(KeyPrefix.USERNAME, entry.getKey().id().toString()),
                        Map.Entry::getValue
                ));
    }

    private Map<UserId, String> mergeUsernames(Map<UserId, String> cachedUsernames, Map<UserId, String> missingUsernames) {
        return Stream.concat(cachedUsernames.entrySet().stream(), missingUsernames.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    private Map<UserId, String> fetchMissingUsernames(Set<UserId> missingUserIds) {
        return userRepository
                .findUserIdAndUsernameByUserIdIn(
                        missingUserIds.stream()
                                .map(UserId::id)
                                .toList())
                .stream()
                .collect(Collectors.toMap(
                        proj -> new UserId(proj.getUserId()),
                        UserIdUsernameProjection::getUsername
                ));
    }
}
