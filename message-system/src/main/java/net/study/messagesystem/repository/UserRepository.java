package net.study.messagesystem.repository;

import jakarta.persistence.LockModeType;
import net.study.messagesystem.dto.projection.ConnectionCountProjection;
import net.study.messagesystem.dto.projection.InviteCodeProjection;
import net.study.messagesystem.dto.projection.UserIdProjection;
import net.study.messagesystem.dto.projection.UsernameProjection;
import net.study.messagesystem.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByInviteCode(String inviteCode);

    List<UserIdProjection> findUserIdByUsernameIn(Collection<String> usernames);

    Optional<UserIdProjection> findUserIdByUsername(String username);

    Optional<UsernameProjection> findByUserId(Long userId);

    @Transactional(readOnly = true)
    Optional<InviteCodeProjection> findInviteCodeByUserId(Long userId);

    Optional<ConnectionCountProjection> findCountByUserId(Long userId);
}
