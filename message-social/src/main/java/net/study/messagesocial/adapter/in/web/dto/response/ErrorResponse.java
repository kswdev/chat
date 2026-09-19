package net.study.messagesocial.adapter.in.web.dto.response;

import lombok.Builder;
import lombok.Getter;
import net.study.messagesocial.adapter.in.web.dto.error.ErrorCode;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private int status;
    private String code;
    private String message;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    private String path;

    public static ErrorResponse from(ErrorCode errorCode, String path) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .path(path)
                .build();
    }
}