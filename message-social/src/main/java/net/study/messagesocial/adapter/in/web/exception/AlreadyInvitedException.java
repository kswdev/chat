package net.study.messagesocial.adapter.in.web.exception;

import net.study.messagesocial.adapter.in.web.dto.error.ErrorCode;

public class AlreadyInvitedException extends FriendException {

    public AlreadyInvitedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
