package net.study.messagesocial.adapter.in.web.exception;

import net.study.messagesocial.adapter.in.web.dto.error.ErrorCode;

public class UserNotFoundException extends FriendException {

    public UserNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
