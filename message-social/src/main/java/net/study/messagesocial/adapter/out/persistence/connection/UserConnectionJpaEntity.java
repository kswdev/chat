package net.study.messagesocial.adapter.out.persistence.connection;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.study.messagesocial.domain.userconnection.UserConnectionStatus;

@Entity
@Table(name = "user_connection",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_connection_pair", columnNames = {"inviter_id", "invitee_id"})
        },
        indexes = {
                @Index(name = "idx_user_connection_a_status", columnList = "user_id_a, status"),
                @Index(name = "idx_user_connection_b_status", columnList = "user_id_b, status")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserConnectionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inviter_id", nullable = false)
    private Long inviterId;

    @Column(name = "invitee_id", nullable = false)
    private Long inviteeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserConnectionStatus status;

    @Column(name = "user_id_a", nullable = false)
    private Long userIdA;

    @Column(name = "user_id_b", nullable = false)
    private Long userIdB;

    public static UserConnectionJpaEntity create(Long inviterId, Long inviteeId, UserConnectionStatus status) {
        Long userIdA = Math.min(inviterId, inviteeId);
        Long userIdB = Math.max(inviterId, inviteeId);
        return new UserConnectionJpaEntity(null, inviterId, inviteeId, status, userIdA, userIdB);
    }

    public void changeStatus(UserConnectionStatus status) {
        this.status = status;
    }
}
