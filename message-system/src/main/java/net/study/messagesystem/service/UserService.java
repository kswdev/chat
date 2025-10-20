package net.study.messagesystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.KeyPrefix;
import net.study.messagesystem.dto.projection.ConnectionCountProjection;
import net.study.messagesystem.dto.projection.InviteCodeProjection;
import net.study.messagesystem.dto.projection.UserIdProjection;
import net.study.messagesystem.dto.projection.UsernameProjection;
import net.study.messagesystem.dto.domain.user.InviteCode;
import net.study.messagesystem.dto.domain.user.User;
import net.study.messagesystem.dto.domain.user.UserId;
import net.study.messagesystem.entity.user.UserEntity;
import net.study.messagesystem.repository.UserRepository;
import net.study.messagesystem.util.JsonUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final CacheService cacheService;
    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final JsonUtil jsonUtil;
    private final long TTL = 3600;

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
}
