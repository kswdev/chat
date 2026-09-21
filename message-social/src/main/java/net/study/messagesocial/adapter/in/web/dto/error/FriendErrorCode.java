package net.study.messagesocial.adapter.in.web.dto.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FriendErrorCode implements ErrorCode {
    INVITE_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "INVITE_CODE_NOT_FOUND", "초대 코드를 찾을 수 없습니다."),
    ALREADY_INVITED(HttpStatus.BAD_REQUEST, "ALREADY_INVITED", "이미 초대된 사용자입니다."),
    ALREADY_FRIEND(HttpStatus.BAD_REQUEST, "ALREADY_FRIEND", "이미 친구인 사용자입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
