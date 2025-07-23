package net.study.messagesystem.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.dto.projection.UsernameProjection;
import net.study.messagesystem.dto.user.InviteCode;
import net.study.messagesystem.dto.user.User;
import net.study.messagesystem.dto.user.UserId;
import net.study.messagesystem.entity.user.UserEntity;
import net.study.messagesystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<String> getUsername(UserId userId) {
        return userRepository.findByUserId(userId.id())
                .map(UsernameProjection::getUsername);
    }

    public Optional<User> getUserIdName(InviteCode inviteCode) {
        return userRepository.findByConnectionInviteCode(inviteCode.code())
                .map(user -> new User(new UserId(user.getUserId()), user.getUsername()));
    }

    public Optional<UserId> getUserId(String username) {
        return userRepository.findByUsername(username)
                .map(user -> new UserId(user.getUserId()));
    }

    @Transactional
    public UserId addUser(String username, String password) {
        UserEntity savedUser = userRepository.save(new UserEntity(username, passwordEncoder.encode(password)));
        log.info("User registered. UserId: {}, Username: {}", savedUser.getUserId(), savedUser.getUsername());
        return new UserId(savedUser.getUserId());
    }

    @Transactional
    public void removeUser() {
        String username = sessionService.getUsername();
        UserEntity user = userRepository.findByUsername(username).orElseThrow();
        userRepository.deleteById(user.getUserId());
        log.info("User unRegistered. UserId: {}, Username: {}", user.getUserId(), user.getUsername());
    }
}
