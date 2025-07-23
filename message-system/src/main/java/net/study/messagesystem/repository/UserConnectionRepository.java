package net.study.messagesystem.repository;

import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.dto.projection.InviterUserIdProjection;
import net.study.messagesystem.dto.projection.UserConnectionStatusProjection;
import net.study.messagesystem.entity.user.connection.UserConnectionEntity;
import net.study.messagesystem.entity.user.connection.UserConnectionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserConnectionRepository extends JpaRepository<UserConnectionEntity, UserConnectionId> {

    Optional<UserConnectionStatusProjection> findUserConnectionStatusByPartnerAUserIdAndPartnerBUserId(Long userIdA, Long userIdB);
    Optional<UserConnectionEntity> findByPartnerAUserIdAndPartnerBUserIdAndStatus(Long userIdA, Long userIdB, UserConnectionStatus status);

    Optional<InviterUserIdProjection> findInviterUserIdByPartnerAUserIdAndPartnerAUserId(Long partnerAUserId, Long partnerAUserId1);
}
