package net.study.messagesocial.adapter.in.web.exception;

import lombok.Getter;
import net.study.messagesocial.adapter.in.web.dto.error.ErrorCode;

public abstract class FriendException extends RuntimeException {

    @Getter
    private final ErrorCode errorCode;

    protected FriendException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
