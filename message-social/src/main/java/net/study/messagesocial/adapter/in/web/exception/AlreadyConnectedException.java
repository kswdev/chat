package net.study.messagesocial.adapter.in.web.exception;

import lombok.Getter;
import net.study.messagesocial.adapter.in.web.dto.error.ErrorCode;

public class AlreadyConnectedException extends RuntimeException {

    @Getter
    private final ErrorCode errorCode;

    public AlreadyConnectedException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
