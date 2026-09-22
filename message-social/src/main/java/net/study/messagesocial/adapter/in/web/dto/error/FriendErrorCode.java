package net.study.messagesocial.adapter.in.web.dto.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FriendErrorCode implements ErrorCode {
    INVITE_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "INVITE_CODE_NOT_FOUND", "초대 코드를 찾을 수 없습니다."),
    ALREADY_INVITED(HttpStatus.BAD_REQUEST, "ALREADY_INVITED", "이미 초대된 사용자입니다."),
    ALREADY_FRIEND(HttpStatus.BAD_REQUEST, "ALREADY_FRIEND", "이미 친구인 사용자입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    CONNECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "CONNECTION_NOT_FOUND", "연결 정보를 찾을 수 없습니다."),
    INVALID_CONNECTION_STATUS(HttpStatus.BAD_REQUEST, "INVALID_CONNECTION_STATUS", "현재 상태에서는 처리할 수 없는 요청입니다."),
    CONNECTION_LIMIT_REACHED(HttpStatus.BAD_REQUEST, "CONNECTION_LIMIT_REACHED", "연결 가능한 친구 수를 초과했습니다."),
    CONNECTION_LIMIT_REACHED_BY_PARTNER(HttpStatus.BAD_REQUEST, "CONNECTION_LIMIT_REACHED_BY_PARTNER", "상대방의 연결 가능한 친구 수를 초과했습니다."),
    SELF_INVITE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "SELF_INVITE_NOT_ALLOWED", "자기 자신을 초대할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
