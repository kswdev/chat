package net.study.messagesocial.adapter.in.web.exception;

import net.study.messagesocial.adapter.in.web.dto.error.ErrorCode;

public class AlreadyConnectedException extends FriendException {

    public AlreadyConnectedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
