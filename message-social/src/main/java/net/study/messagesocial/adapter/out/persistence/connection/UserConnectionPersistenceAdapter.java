package net.study.messagesocial.adapter.out.persistence.connection;

import lombok.RequiredArgsConstructor;
import net.study.messagesocial.application.port.out.LoadUserConnectionPort;
import net.study.messagesocial.application.port.out.SaveFriendPort;
import net.study.messagesocial.domain.userconnection.UserConnection;
import net.study.messagesocial.domain.userconnection.UserConnectionStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserConnectionPersistenceAdapter implements SaveFriendPort, LoadUserConnectionPort {

    private final UserConnectionJpaRepository userConnectionJpaRepository;

    @Override
    @Transactional
    public void save(UserConnection userConnection) {
        UserConnectionJpaEntity entity = userConnectionJpaRepository
                .findByInviterIdAndInviteeId(userConnection.getInviterId(), userConnection.getInviteeId())
                .orElseGet(() -> UserConnectionJpaEntity.create(
                        userConnection.getInviterId(),
                        userConnection.getInviteeId(),
                        userConnection.getStatus()));

        entity.changeStatus(userConnection.getStatus());
        userConnectionJpaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserConnection> getUserConnection(Long inviterId, Long inviteeId) {
        return userConnectionJpaRepository
                .findByInviterIdAndInviteeId(inviterId, inviteeId)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserConnection> findByUserIdAndStatus(Long userId, UserConnectionStatus status) {
        return userConnectionJpaRepository
                .findByUserIdAndStatus(userId, status)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByUserIdAndPartnerIdsAndStatus(Long userId, Collection<Long> partnerIds, UserConnectionStatus status) {
        return userConnectionJpaRepository.countByUserIdAndPartnerIdsAndStatus(userId, partnerIds, status);
    }

    private UserConnection toDomain(UserConnectionJpaEntity entity) {
        return UserConnection.builder()
                .inviterId(entity.getInviterId())
                .inviteeId(entity.getInviteeId())
                .status(entity.getStatus())
                .build();
    }
}
