package net.study.messagesystem.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.AcceptNotificationRecord;
import net.study.messagesystem.dto.kafka.AcceptRequestRecord;
import net.study.messagesystem.dto.kafka.AcceptResponseRecord;
import net.study.messagesystem.dto.kafka.ErrorResponseRecord;
import net.study.messagesystem.dto.websocket.outbound.AcceptNotification;
import net.study.messagesystem.dto.websocket.outbound.AcceptResponse;
import net.study.messagesystem.dto.websocket.outbound.ErrorResponse;
import net.study.messagesystem.service.ClientNotificationService;
import net.study.messagesystem.service.UserConnectionService;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcceptRequestRecordHandler implements BaseRecordHandler<AcceptRequestRecord> {

    private final UserConnectionService userConnectionService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRecord(AcceptRequestRecord record) {
        UserId accepterUserId = record.userId();
        Pair<Optional<UserId>, String> result = userConnectionService.accept(accepterUserId, record.username());

        result.getFirst()
                .ifPresentOrElse(inviterUserId -> {
                    String accepterUsername = result.getSecond();
                    clientNotificationService.sendMessage(accepterUserId, new AcceptResponseRecord(accepterUserId, record.username()));
                    clientNotificationService.sendMessage(inviterUserId, new AcceptNotificationRecord(inviterUserId, accepterUsername));
                }, () -> {
                    String errorMessage = result.getSecond();
                    clientNotificationService.sendError(new ErrorResponseRecord(accepterUserId, errorMessage, MessageType.ACCEPT_REQUEST));
                });
    }

    @Override
    public Class<AcceptRequestRecord> getRequestType() {
        return AcceptRequestRecord.class;
    }
}
