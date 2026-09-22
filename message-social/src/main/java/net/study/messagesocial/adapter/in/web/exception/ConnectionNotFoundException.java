package net.study.messagesocial.adapter.in.web.exception;

import net.study.messagesocial.adapter.in.web.dto.error.ErrorCode;

public class ConnectionNotFoundException extends FriendException {

    public ConnectionNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
