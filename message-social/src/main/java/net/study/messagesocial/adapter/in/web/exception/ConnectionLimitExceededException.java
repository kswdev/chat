package net.study.messagesocial.adapter.in.web.exception;

import net.study.messagesocial.adapter.in.web.dto.error.ErrorCode;

public class ConnectionLimitExceededException extends FriendException {

    public ConnectionLimitExceededException(ErrorCode errorCode) {
        super(errorCode);
    }
}
