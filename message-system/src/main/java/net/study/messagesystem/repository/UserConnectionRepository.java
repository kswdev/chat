package net.study.messagesystem.repository;

import jakarta.persistence.LockModeType;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.dto.projection.InviterUserIdProjection;
import net.study.messagesystem.dto.projection.UserConnectionStatusProjection;
import net.study.messagesystem.dto.projection.UserIdUsernameProjection;
import net.study.messagesystem.entity.user.connection.UserConnectionEntity;
import net.study.messagesystem.entity.user.connection.UserConnectionId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserConnectionRepository extends JpaRepository<UserConnectionEntity, UserConnectionId> {

    Optional<UserConnectionStatusProjection> findUserConnectionStatusByPartnerAUser_userIdAndPartnerBUser_userId(Long userIdA, Long userIdB);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph("UserConnectionEntity.withAll")
    Optional<UserConnectionEntity> findByPartnerAUser_userIdAndPartnerBUser_userIdAndStatus(Long userIdA, Long userIdB, UserConnectionStatus status);

    Optional<InviterUserIdProjection> findInviterUserIdByPartnerAUser_userIdAndPartnerBUser_userId(Long partnerAUserId, Long partnerAUserId1);

    @Query("""
                SELECT uc.partnerBUser.userId AS userId,
                       u.username AS username
                  FROM UserConnectionEntity uc
            INNER JOIN UserEntity u ON uc.partnerBUser.userId = u.userId
                 WHERE uc.partnerAUser.userId = :userId AND uc.status = :status
           """)
    List<UserIdUsernameProjection> findByPartnerAUser_userIdAndStatus(Long userId, UserConnectionStatus status);

    @Query("""
                SELECT uc.partnerAUser.userId AS userId,
                       u.username AS username
                  FROM UserConnectionEntity uc
            INNER JOIN UserEntity u ON uc.partnerAUser.userId = u.userId
                 WHERE uc.partnerBUser.userId = :userId AND uc.status = :status
           """)
    List<UserIdUsernameProjection> findByPartnerBUser_userIdAndStatus(Long userId, UserConnectionStatus status);
}
