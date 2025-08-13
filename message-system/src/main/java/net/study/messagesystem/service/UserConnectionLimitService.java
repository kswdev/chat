package net.study.messagesystem.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.dto.domain.user.UserId;
import net.study.messagesystem.entity.user.connection.UserConnectionEntity;
import net.study.messagesystem.repository.connection.UserConnectionRepository;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserConnectionLimitService {

    private final UserConnectionRepository userConnectionRepository;

    @Transactional
    public void connect(UserId accepterUserId, UserId inviterUserId) {
        UserConnectionEntity connectionEntity =  getConnectionEntity(accepterUserId, inviterUserId, UserConnectionStatus.PENDING);
        connectionEntity.connect();
    }

    @Transactional
    public void reject(UserId rejecterUserId, UserId inviterUserId) {
        UserConnectionEntity connectionEntity =  getConnectionEntity(rejecterUserId, inviterUserId, UserConnectionStatus.PENDING);
        connectionEntity.reject();
    }

    @Transactional
    public void disconnect(UserId senderUserId, UserId partnerUserId) {
        UserConnectionEntity connectionEntity =  getConnectionEntity(senderUserId, partnerUserId, UserConnectionStatus.ACCEPTED);
        connectionEntity.disconnect();
    }

    private UserConnectionEntity getConnectionEntity(UserId accepterUserId, UserId inviterUserId, UserConnectionStatus status) {
        Pair<Long, Long> userIdAscending = getUserIdAscending(accepterUserId, inviterUserId);
        Long firstUserId = userIdAscending.getFirst();
        Long secondUserId = userIdAscending.getSecond();

        return userConnectionRepository
                .findByPartnerAUser_userIdAndPartnerBUser_userIdAndStatus(firstUserId, secondUserId, status)
                .orElseThrow(() -> new EntityNotFoundException("Invalid status"));
    }

    private Pair<Long, Long> getUserIdAscending(UserId userAId, UserId userBId) {
        return Pair.of(Math.min(userAId.id(), userBId.id()),
                Math.max(userAId.id(), userBId.id()));
    }
}
