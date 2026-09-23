package net.study.messagesystem.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.MessageType;
import net.study.messagecommon.constant.UserConnectionStatus;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.DisconnectRequestRecord;
import net.study.messagesystem.dto.kafka.DisconnectResponseRecord;
import net.study.messagesystem.dto.kafka.ErrorResponseRecord;
import net.study.messagesystem.service.ClientNotificationService;
import net.study.messagesystem.service.UserConnectionService;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

/**
 * 주의: message-social로 이관된 기능의 구버전이지만 아직 삭제 금지.
 * message-front(ChatContext.tsx)/message-client(CommandHandler.java)가 여전히
 * WebSocket 경로(INVITE_REQUEST 등)로 이 코드를 호출하는 유일하게 살아있는 경로다.
 * message-social의 HTTP API로 클라이언트 마이그레이션(MESSAGE_SYSTEM_MSA_PLAN.md Phase 2f)이
 * 끝나기 전까지는 지우지 말 것.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DisconnectRequestRecordHandler implements BaseRecordHandler<DisconnectRequestRecord> {

    private final UserConnectionService userConnectionService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRecord(DisconnectRequestRecord record) {
        UserId senderUserId = record.userId();
        Pair<Boolean, String> result = userConnectionService.disconnect(senderUserId, record.username());

        if (result.getFirst()) {
            String partnerUsername = result.getSecond();
            clientNotificationService.sendMessage(senderUserId, new DisconnectResponseRecord(senderUserId, partnerUsername, UserConnectionStatus.DISCONNECTED));
        } else {
            String errorMessage = result.getSecond();
            clientNotificationService.sendError(new ErrorResponseRecord(senderUserId, errorMessage, MessageType.DISCONNECT_REQUEST));
        }
    }

    @Override
    public Class<DisconnectRequestRecord> getRequestType() {
        return DisconnectRequestRecord.class;
    }
}
