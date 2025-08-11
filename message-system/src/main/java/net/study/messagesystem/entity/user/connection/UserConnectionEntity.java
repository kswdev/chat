package net.study.messagesystem.entity.user.connection;

import jakarta.persistence.*;
import lombok.*;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.entity.BaseEntity;
import net.study.messagesystem.entity.user.UserEntity;
import org.springframework.data.util.Pair;

import java.util.Arrays;

import static net.study.messagesystem.constant.UserConnectionStatus.*;

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

    @Setter
    @Transient
    public int LIMIT_CONNECTIONS = 1_0;

    public UserConnectionEntity(UserEntity partnerAUser, UserEntity partnerBUser, UserConnectionStatus status, Long inviterUserId) {
        this.partnerAUser = partnerAUser;
        this.partnerBUser = partnerBUser;
        this.status = status;
        this.inviterUserId = inviterUserId;
    }

    public static UserConnectionEntity create(UserEntity partnerAUser, UserEntity partnerBUser, Long inviterUserId) {
        Pair<UserEntity, UserEntity> result = compareUsersById(partnerAUser, partnerBUser);

        return new UserConnectionEntity(
                result.getFirst(),
                result.getSecond(),
                PENDING,
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
        checkIfStatus(PENDING);
        checkIfConnectionReachedLimit();
        increaseConnectionCount();
        status = ACCEPTED;
    }

    public void disconnect() {
        checkIfConnectionReachedZero();
        checkIfStatus(ACCEPTED, REJECTED);
        decreaseConnectionCount();
        status = DISCONNECTED;
    }

    private static Pair<UserEntity, UserEntity> compareUsersById(UserEntity partnerAUser, UserEntity partnerBUser) {
        if (partnerAUser.getUserId() < partnerBUser.getUserId()) {
            return Pair.of(partnerAUser, partnerBUser);
        } else {
            return Pair.of(partnerBUser, partnerAUser);
        }
    }

    private void checkIfStatus(UserConnectionStatus... status) {
        if (Arrays.stream(status).noneMatch(s -> s.equals(this.status)))
            throw new IllegalStateException("Connection status should "+ Arrays.toString(status) +" but : " + this.status);
    }

    private void checkIfConnectionReachedLimit() {
        if (partnerAUser.getConnectionCount() >= LIMIT_CONNECTIONS)
            throw new IllegalStateException(getErrorMessage(partnerAUser.getUserId()));

        if (partnerBUser.getConnectionCount() >= LIMIT_CONNECTIONS)
            throw new IllegalStateException(getErrorMessage(partnerBUser.getUserId()));
    }

    private void checkIfConnectionReachedZero() {
        if (partnerAUser.getConnectionCount() <= 0)
            throw new IllegalStateException("Connection limit reached already zero: " + partnerAUser.getUserId());

        if (partnerBUser.getConnectionCount() <= 0)
            throw new IllegalStateException("Connection limit reached already zero: " + partnerBUser.getUserId());
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

    private void decreaseConnectionCount() {
        partnerAUser.setConnectionCount(partnerAUser.getConnectionCount() - 1);
        partnerBUser.setConnectionCount(partnerBUser.getConnectionCount() - 1);
    }

    public void reject() {
        checkIfStatus(PENDING);
        status = REJECTED;
    }
}
