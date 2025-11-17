package net.study.messagesystem.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.ErrorResponseRecord;
import net.study.messagesystem.dto.kafka.RejectRequestRecord;
import net.study.messagesystem.dto.kafka.RejectResponseRecord;
import net.study.messagesystem.service.ClientNotificationService;
import net.study.messagesystem.service.UserConnectionService;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RejectRequestRecordHandler implements BaseRecordHandler<RejectRequestRecord> {

    private final UserConnectionService userConnectionService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRecord(RejectRequestRecord record) {
        UserId rejecterUserId = record.userId();
        Pair<Boolean, String> result = userConnectionService.reject(rejecterUserId, record.username());

        if (result.getFirst()) {
            String inviterUsername = result.getSecond();
            clientNotificationService.sendMessage(rejecterUserId, new RejectResponseRecord(rejecterUserId, inviterUsername, UserConnectionStatus.REJECTED));
        } else {
            String errorMessage = result.getSecond();
            clientNotificationService.sendError(new ErrorResponseRecord(rejecterUserId, errorMessage, MessageType.REJECT_REQUEST));
        }
    }

    @Override
    public Class<RejectRequestRecord> getRequestType() {
        return RejectRequestRecord.class;
    }
}
