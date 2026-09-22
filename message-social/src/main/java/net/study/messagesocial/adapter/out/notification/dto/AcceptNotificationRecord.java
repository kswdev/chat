package net.study.messagesocial.adapter.out.notification.dto;

import net.study.messagecommon.constant.MessageType;

/**
 * userId: 알림을 받을 대상(recipient)의 id. 이벤트를 발생시킨 행위자(actor)의 id가 아니다.
 * message-push가 이 값을 오프라인 대상 사용자로 소비하므로, 필드명은 그 JSON 계약과의
 * 호환을 위해 그대로 유지한다.
 */
public record AcceptNotificationRecord(Long userId, String username) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.NOTIFY_ACCEPT;
    }
}
