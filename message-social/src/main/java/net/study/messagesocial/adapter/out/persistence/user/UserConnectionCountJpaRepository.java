package net.study.messagesocial.adapter.out.persistence.user;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserConnectionCountJpaRepository extends JpaRepository<UserConnectionCountJpaEntity, Long> {

    /**
     * connection_count를 안전하게 읽고 증감시키기 위한 락 걸린 단건 조회.
     * 동시 accept/disconnect 요청이 같은 유저의 connection_count를 동시에
     * 읽고 쓰는 걸 막아 카운터가 실제보다 더 늘거나(제한 우회) 덜 줄어드는 걸 방지한다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserConnectionCountJpaEntity u WHERE u.userId = :userId")
    Optional<UserConnectionCountJpaEntity> findByIdForUpdate(@Param("userId") Long userId);
}
