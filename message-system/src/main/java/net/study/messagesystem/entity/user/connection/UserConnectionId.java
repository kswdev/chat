package net.study.messagesystem.entity.user.connection;

import lombok.*;

import java.io.Serializable;

@Getter
@ToString
@EqualsAndHashCode(of = {"partnerAUser", "partnerBUser"})
@NoArgsConstructor
@AllArgsConstructor
public class UserConnectionId implements Serializable {

    private Long partnerAUser;
    private Long partnerBUser;
}
