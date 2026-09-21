package net.study.messagesocial.domain.userconnection;

import lombok.Builder;
import lombok.Getter;

public class UserConnection {
    private final int LIMIT = 10;

    @Getter private final Long inviterId;
    @Getter private final Long inviteeId;

    @Getter private UserConnectionStatus status;

    @Builder
    public UserConnection(Long inviterId, Long inviteeId, UserConnectionStatus status) {
        this.inviterId = inviterId;
        this.inviteeId = inviteeId;
        this.status = status;
    }

    public void changeStatus(UserConnectionStatus userConnectionStatus) {
        this.status = userConnectionStatus;
    }
}
