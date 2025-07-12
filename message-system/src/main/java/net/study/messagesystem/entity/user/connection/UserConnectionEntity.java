package net.study.messagesystem.entity.user.connection;

import jakarta.persistence.*;
import lombok.*;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.entity.BaseEntity;

@Getter
@Entity @Table(name = "user_connection")
@IdClass(UserConnectionId.class)
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = {"partnerAUserId", "partnerBUserId"})
public class UserConnectionEntity extends BaseEntity {

    @Id
    @Column(name = "partner_a_user_id")
    private Long partnerAUserId;

    @Id
    @Column(name = "partner_b_user_id")
    private Long partnerBUserId;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserConnectionStatus status;

    @Column(name = "inviter_user_id", nullable = false)
    private Long inviterUserId;
}
