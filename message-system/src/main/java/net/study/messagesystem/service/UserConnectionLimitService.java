package net.study.messagesystem.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.dto.user.UserId;
import net.study.messagesystem.entity.user.UserEntity;
import net.study.messagesystem.entity.user.connection.UserConnectionEntity;
import net.study.messagesystem.repository.UserConnectionRepository;
import net.study.messagesystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class UserConnectionLimitService {

    private final UserRepository userRepository;
    private final UserConnectionRepository userConnectionRepository;

    @Getter @Setter
    private int limitConnections = 1_000;

    @Transactional
    public void connect(UserId accepterUserId, UserId inviterUserId) {
        Long firstUserId = Math.min(accepterUserId.id(), inviterUserId.id());
        Long secondUserId = Math.max(accepterUserId.id(), inviterUserId.id());

        UserEntity firstUserEntity = userRepository
                .findForUpdateByUserId(firstUserId)
                .orElseThrow(() -> new EntityNotFoundException("Invalid user id: " + firstUserId));

        UserEntity secondUserEntity = userRepository
                .findForUpdateByUserId(secondUserId)
                .orElseThrow(() -> new EntityNotFoundException("Invalid user id: " + secondUserId));

        UserConnectionEntity connectionEntity = userConnectionRepository
                .findByPartnerAUserIdAndPartnerBUserIdAndStatus(firstUserId, secondUserId, UserConnectionStatus.PENDING)
                .orElseThrow(() -> new EntityNotFoundException("Invalid status"));

        Function<Long, String> getErrorMessage = userId ->
                userId.equals(accepterUserId.id())
                        ? "Connection limit reached"
                        : "Connection limit reached by other user";

        int firstConnectionCount = firstUserEntity.getConnectionCount();
        if (firstConnectionCount >= limitConnections)
            throw new IllegalStateException(getErrorMessage.apply(firstUserId));

        int secondConnectionCount = secondUserEntity.getConnectionCount();
        if (secondConnectionCount >= limitConnections)
            throw new IllegalStateException(getErrorMessage.apply(secondUserId));

        firstUserEntity.setConnectionCount(firstConnectionCount + 1);
        secondUserEntity.setConnectionCount(secondConnectionCount + 1);
        connectionEntity.setStatus(UserConnectionStatus.ACCEPTED);
    }
}
