package net.study.messagesystem.repository;

import jakarta.persistence.LockModeType;
import net.study.messagesystem.dto.projection.UsernameProjection;
import net.study.messagesystem.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByConnectionInviteCode(String connectionInviteCode);

    Optional<UsernameProjection> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserEntity> findForUpdateByUserId(Long userId);
}
