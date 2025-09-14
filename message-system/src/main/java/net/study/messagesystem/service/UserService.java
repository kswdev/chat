package net.study.messagesystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.dto.projection.ConnectionCountProjection;
import net.study.messagesystem.dto.projection.InviteCodeProjection;
import net.study.messagesystem.dto.projection.UserIdProjection;
import net.study.messagesystem.dto.projection.UsernameProjection;
import net.study.messagesystem.dto.domain.user.InviteCode;
import net.study.messagesystem.dto.domain.user.User;
import net.study.messagesystem.dto.domain.user.UserId;
import net.study.messagesystem.entity.user.UserEntity;
import net.study.messagesystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    public Optional<Long> getConnectionCount(UserId userId) {
        return userRepository.findCountByUserId(userId.id())
                .map(ConnectionCountProjection::getConnectionCount);
    }

    public Optional<InviteCode> getInviteCode(UserId userId) {
        return userRepository.findInviteCodeByUserId(userId.id())
                .map(InviteCodeProjection::getConnectionInviteCode)
                .map(InviteCode::new);
    }

    public Optional<User> getUserIdName(InviteCode inviteCode) {
        return userRepository.findByConnectionInviteCode(inviteCode.code())
                .map(user -> new User(new UserId(user.getUserId()), user.getUsername()));
    }

    public Optional<UserId> getUserId(String username) {
        return userRepository.findByUsername(username)
                .map(user -> new UserId(user.getUserId()));
    }

    public List<UserId> getUserIds(List<String> usernames) {
        return userRepository.findUserIdByUsernameIn(usernames).stream()
                .map(UserIdProjection::getUserId)
                .map(UserId::new)
                .toList();
    }

    public UserEntity getUserReference(UserId userId) {
        return userRepository.getReferenceById(userId.id());
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
