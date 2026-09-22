package net.study.messagesocial.adapter.in.web.exception;

import net.study.messagesocial.adapter.in.web.dto.error.ErrorCode;

public class InvalidConnectionStatusException extends FriendException {

    public InvalidConnectionStatusException(ErrorCode errorCode) {
        super(errorCode);
    }
}
