package net.study.messagesocial.adapter.in.web.api;

import jakarta.servlet.http.HttpServletRequest;
import net.study.messagesocial.adapter.in.web.dto.error.ErrorCode;
import net.study.messagesocial.adapter.in.web.dto.response.ErrorResponse;
import net.study.messagesocial.adapter.in.web.exception.FriendException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = FriendErrorController.class)
public class FriendErrorController {

    @ExceptionHandler(FriendException.class)
    public ResponseEntity<ErrorResponse> handle(
            FriendException ex,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ex.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.from(errorCode, request.getRequestURI()));
    }
}
