package net.study.messagesocial.adapter.out.persistence.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * message-social 자체 도메인 상태 — 이 유저가 이 서비스 안에서 몇 명과 연결돼 있는지.
 * username/inviteCode(message-user 소유 데이터)는 여기 없다 — 그건 message-user REST API를
 * 동기 호출해서 가져온다(MessageUserPersistenceAdapter 참고).
 */
@Entity
@Table(name = "user_connection_count")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserConnectionCountJpaEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "connection_count", nullable = false)
    private int connectionCount;

    private UserConnectionCountJpaEntity(Long userId, int connectionCount) {
        this.userId = userId;
        this.connectionCount = connectionCount;
    }

    public static UserConnectionCountJpaEntity create(Long userId, int connectionCount) {
        return new UserConnectionCountJpaEntity(userId, connectionCount);
    }

    public void updateConnectionCount(int connectionCount) {
        this.connectionCount = connectionCount;
    }
}
