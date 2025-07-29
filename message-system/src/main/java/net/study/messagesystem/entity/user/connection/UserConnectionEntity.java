package net.study.messagesystem.entity.user.connection;

import jakarta.persistence.*;
import lombok.*;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.entity.BaseEntity;
import net.study.messagesystem.entity.user.UserEntity;
import org.springframework.data.util.Pair;

@NamedEntityGraph(name = "UserConnectionEntity.withAll", attributeNodes = {
        @NamedAttributeNode("partnerAUser"),
        @NamedAttributeNode("partnerBUser")
})

@Getter
@Entity @Table(name = "user_connection")
@IdClass(UserConnectionId.class)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"partnerAUser", "partnerBUser"}, callSuper = false)
public class UserConnectionEntity extends BaseEntity {

    @Id
    @JoinColumn(name = "partner_a_user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity partnerAUser;

    @Id
    @JoinColumn(name = "partner_b_user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity partnerBUser;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserConnectionStatus status;

    @Column(name = "inviter_user_id", nullable = false)
    private Long inviterUserId;

    @Transient
    public final int LIMIT_CONNECTIONS = 1_000;

    public static UserConnectionEntity create(UserEntity partnerAUser, UserEntity partnerBUser, Long inviterUserId) {
        Pair<UserEntity, UserEntity> result = compareUsersById(partnerAUser, partnerBUser);

        return new UserConnectionEntity(
                result.getFirst(),
                result.getSecond(),
                UserConnectionStatus.PENDING,
                inviterUserId
        );
    }

    public static UserConnectionEntity testConnection(UserEntity partnerAUser, UserEntity partnerBUser, UserConnectionStatus userConnectionStatus, Long inviterUserId) {
        Pair<UserEntity, UserEntity> result = compareUsersById(partnerAUser, partnerBUser);
        return new UserConnectionEntity(
                result.getFirst(),
                result.getSecond(),
                userConnectionStatus,
                inviterUserId
        );
    }

    public void connect() {
        checkIfConnectionReachedLimit();
        increaseConnectionCount();
        status = UserConnectionStatus.ACCEPTED;
    }

    private static Pair<UserEntity, UserEntity> compareUsersById(UserEntity partnerAUser, UserEntity partnerBUser) {
        if (partnerAUser.getUserId() < partnerBUser.getUserId()) {
            return Pair.of(partnerAUser, partnerBUser);
        } else {
            return Pair.of(partnerBUser, partnerAUser);
        }
    }

    private void checkIfConnectionReachedLimit() {
        if (partnerAUser.getConnectionCount() >= LIMIT_CONNECTIONS)
            throw new IllegalStateException(getErrorMessage(partnerAUser.getUserId()));

        if (partnerBUser.getConnectionCount() >= LIMIT_CONNECTIONS)
            throw new IllegalStateException(getErrorMessage(partnerBUser.getUserId()));
    }

    private String getErrorMessage(Long userId) {
        return userId.equals(inviterUserId)
                ? "Connection limit reached by other user"
                : "Connection limit reached";
    }

    private void increaseConnectionCount() {
        partnerAUser.setConnectionCount(partnerAUser.getConnectionCount() + 1);
        partnerBUser.setConnectionCount(partnerBUser.getConnectionCount() + 1);
    }
}
