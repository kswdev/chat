package net.study.messagepush.dto.kafka.inbound;

import net.study.messagecommon.constant.MessageType;
import net.study.messagecommon.constant.UserConnectionStatus;
import net.study.messagepush.dto.user.InviteCode;
import net.study.messagepush.dto.user.UserId;

public record InviteResponse(UserId userId, InviteCode inviteCode, UserConnectionStatus status) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.INVITE_RESPONSE;
    }
}
