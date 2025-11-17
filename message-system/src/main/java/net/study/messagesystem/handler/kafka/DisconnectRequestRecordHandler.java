package net.study.messagesystem.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.DisconnectRequestRecord;
import net.study.messagesystem.dto.kafka.DisconnectResponseRecord;
import net.study.messagesystem.dto.kafka.ErrorResponseRecord;
import net.study.messagesystem.service.ClientNotificationService;
import net.study.messagesystem.service.UserConnectionService;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

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
