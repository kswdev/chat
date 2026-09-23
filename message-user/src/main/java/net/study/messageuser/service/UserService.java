package net.study.messageuser.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.KeyPrefix;
import net.study.messageuser.dto.domain.user.UserId;
import net.study.messageuser.entity.user.UserEntity;
import net.study.messageuser.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final long TTL = 3600;

    @Transactional(readOnly = true)
    public Optional<UserEntity> getByUsername(String username) {
        String key = cacheService.buildKey(KeyPrefix.USER_ID, username);
        return cacheService.get(key)
                .map(Long::valueOf)
                .flatMap(userRepository::findById)
                .or(() -> userRepository.findByUsername(username)
                        .map(user -> {
                            cacheService.set(key, user.getUserId().toString(), TTL);
                            return user;
                        }));
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> getByInviteCode(String inviteCode) {
        String key = cacheService.buildKey(KeyPrefix.INVITECODE_USER_ID, inviteCode);
        return cacheService.get(key)
                .map(Long::valueOf)
                .flatMap(userRepository::findById)
                .or(() -> userRepository.findByInviteCode(inviteCode)
                        .map(user -> {
                            cacheService.set(key, user.getUserId().toString(), TTL);
                            return user;
                        }));
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> getById(Long userId) {
        return userRepository.findById(userId);
    }

    @Transactional(readOnly = true)
    public List<UserEntity> getByIds(List<Long> userIds) {
        return userRepository.findAllById(userIds);
    }

    @Transactional(readOnly = true)
    public List<UserEntity> getByUsernames(List<String> usernames) {
        return userRepository.findByUsernameIn(usernames);
    }

    @Transactional
    public UserId addUser(String username, String password) {
        UserEntity savedUser = userRepository.save(new UserEntity(username, passwordEncoder.encode(password)));
        log.info("User registered. UserId: {}, Username: {}", savedUser.getUserId(), savedUser.getUsername());
        return new UserId(savedUser.getUserId());
    }

    @Transactional
    public void removeUser(String userId) {
        UserEntity user = userRepository.findById(Long.parseLong(userId)).orElseThrow();

        userRepository.deleteById(user.getUserId());
        cacheService.delete(
                List.of(
                        cacheService.buildKey(KeyPrefix.USER_ID, user.getUsername()),
                        cacheService.buildKey(KeyPrefix.INVITECODE_USER_ID, user.getInviteCode())));

        log.info("User unRegistered. UserId: {}, Username: {}", user.getUserId(), user.getUsername());
    }
}
