package net.study.messagesocial.adapter.out.persistence.connection;

import net.study.messagesocial.domain.userconnection.UserConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserConnectionJpaRepository extends JpaRepository<UserConnectionJpaEntity, Long> {

    Optional<UserConnectionJpaEntity> findByInviterIdAndInviteeId(Long inviterId, Long inviteeId);

    @Query("""
            SELECT c FROM UserConnectionJpaEntity c
            WHERE (c.userIdA = :userId OR c.userIdB = :userId)
            AND c.status = :status
            """)
    List<UserConnectionJpaEntity> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") UserConnectionStatus status);

    @Query("""
            SELECT COUNT(c) FROM UserConnectionJpaEntity c
            WHERE ((c.userIdA = :userId AND c.userIdB IN :partnerIds) OR (c.userIdB = :userId AND c.userIdA IN :partnerIds))
            AND c.status = :status
            """)
    long countByUserIdAndPartnerIdsAndStatus(@Param("userId") Long userId, @Param("partnerIds") Collection<Long> partnerIds, @Param("status") UserConnectionStatus status);
}
