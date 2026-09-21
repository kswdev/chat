package net.study.messagesocial.adapter.in.web.exception;

import lombok.Getter;
import net.study.messagesocial.adapter.in.web.dto.error.ErrorCode;

public class InvalidInviteCodeException extends RuntimeException {

    @Getter
    private final ErrorCode errorCode;

    public InvalidInviteCodeException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
