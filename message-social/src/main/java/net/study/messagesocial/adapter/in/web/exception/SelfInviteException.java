package net.study.messagesocial.adapter.in.web.exception;

import net.study.messagesocial.adapter.in.web.dto.error.ErrorCode;

public class SelfInviteException extends FriendException {

    public SelfInviteException(ErrorCode errorCode) {
        super(errorCode);
    }
}
