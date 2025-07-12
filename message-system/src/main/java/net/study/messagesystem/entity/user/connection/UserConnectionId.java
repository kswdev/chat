package net.study.messagesystem.entity.user.connection;

import lombok.*;

import java.io.Serializable;

@Getter
@ToString
@EqualsAndHashCode(of = {"partnerAUserId", "partnerBUserId"})
@NoArgsConstructor
@AllArgsConstructor
public class UserConnectionId implements Serializable {

    private Long partnerAUserId;
    private Long partnerBUserId;
}
