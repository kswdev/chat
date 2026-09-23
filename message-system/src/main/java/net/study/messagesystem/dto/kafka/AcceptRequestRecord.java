package net.study.messagesystem.dto.kafka;

import net.study.messagecommon.constant.MessageType;
import net.study.messagesystem.domain.user.UserId;

/**
 * username: 수락 대상(inviter)의 username — AcceptRequest에서 그대로 옴.
 * accepterUsername: 요청자 본인(accepter)의 username — JWT 유래.
 */
public record AcceptRequestRecord(UserId userId, String username, String accepterUsername) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.ACCEPT_REQUEST;
    }
}
