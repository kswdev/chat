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

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final CacheService cacheService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
                        cacheService.buildKey(KeyPrefix.USERNAME, userId),
                        cacheService.buildKey(KeyPrefix.USER, userId),
                        cacheService.buildKey(KeyPrefix.USER_INVITECODE, userId)));

        log.info("User unRegistered. UserId: {}, Username: {}", user.getUserId(), user.getUsername());
    }
}
