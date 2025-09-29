package net.study.messagesystem.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.study.messagesystem.constant.KeyPrefix;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.dto.domain.user.UserId;
import net.study.messagesystem.entity.user.connection.UserConnectionEntity;
import net.study.messagesystem.repository.connection.UserConnectionRepository;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserConnectionLimitService {

    private final CacheService cacheService;
    private final UserConnectionRepository userConnectionRepository;

    @Transactional
    public void connect(UserId accepterUserId, UserId inviterUserId) {
        Pair<Long, Long> userIdAscending = getUserIdAscending(accepterUserId, inviterUserId);
        Long firstUserId = userIdAscending.getFirst();
        Long secondUserId = userIdAscending.getSecond();
        UserConnectionEntity connectionEntity =  getConnectionEntity(firstUserId, secondUserId, UserConnectionStatus.PENDING);
        UserConnectionStatus status = connectionEntity.connect();

        deleteConnectionStatusCache(firstUserId, secondUserId, status);
    }

    @Transactional
    public void reject(UserId rejecterUserId, UserId inviterUserId) {
        Pair<Long, Long> userIdAscending = getUserIdAscending(rejecterUserId, inviterUserId);
        Long firstUserId = userIdAscending.getFirst();
        Long secondUserId = userIdAscending.getSecond();
        UserConnectionEntity connectionEntity =  getConnectionEntity(firstUserId, secondUserId, UserConnectionStatus.PENDING);
        UserConnectionStatus status = connectionEntity.reject();

        deleteConnectionStatusCache(firstUserId, secondUserId, status);
    }

    @Transactional
    public void disconnect(UserId senderUserId, UserId partnerUserId) {
        Pair<Long, Long> userIdAscending = getUserIdAscending(senderUserId, partnerUserId);
        Long firstUserId = userIdAscending.getFirst();
        Long secondUserId = userIdAscending.getSecond();
        UserConnectionEntity connectionEntity =  getConnectionEntity(firstUserId, secondUserId, UserConnectionStatus.ACCEPTED);
        UserConnectionStatus status = connectionEntity.disconnect();

        deleteConnectionStatusCache(firstUserId, secondUserId, status);
    }

    private UserConnectionEntity getConnectionEntity(Long firstUserId, Long secondUserId, UserConnectionStatus status) {
        return userConnectionRepository
                .findByPartnerAUser_userIdAndPartnerBUser_userIdAndStatus(firstUserId, secondUserId, status)
                .orElseThrow(() -> new EntityNotFoundException("Invalid status"));
    }

    private Pair<Long, Long> getUserIdAscending(UserId userAId, UserId userBId) {
        return Pair.of(Math.min(userAId.id(), userBId.id()),
                Math.max(userAId.id(), userBId.id()));
    }

    private void deleteConnectionStatusCache(Long firstUserId, Long secondUserId, UserConnectionStatus status) {
        String connectionStatusKeyByTwoParticipant = cacheService.buildKey(KeyPrefix.CONNECTION_STATUS, firstUserId.toString(), secondUserId.toString());
        String connectionStatusKeyByParticipantA = cacheService.buildKey(KeyPrefix.CONNECTION_STATUS, firstUserId.toString(), status.name());
        String connectionStatusKeyByParticipantB = cacheService.buildKey(KeyPrefix.CONNECTION_STATUS, secondUserId.toString(), status.name());
        cacheService.delete(List.of(connectionStatusKeyByTwoParticipant, connectionStatusKeyByParticipantA, connectionStatusKeyByParticipantB));
    }
}
