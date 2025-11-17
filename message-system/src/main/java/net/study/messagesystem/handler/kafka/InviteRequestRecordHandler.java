package net.study.messagesystem.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.ErrorResponseRecord;
import net.study.messagesystem.dto.kafka.InviteNotificationRecord;
import net.study.messagesystem.dto.kafka.InviteRequestRecord;
import net.study.messagesystem.dto.kafka.InviteResponseRecord;
import net.study.messagesystem.service.ClientNotificationService;
import net.study.messagesystem.service.UserConnectionService;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InviteRequestRecordHandler implements BaseRecordHandler<InviteRequestRecord> {

    private final UserConnectionService userConnectionService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRecord(InviteRequestRecord record) {
        UserId inviterUserId = record.userId();
        Pair<Optional<UserId>, String> result = userConnectionService.invite(inviterUserId, record.userInviteCode());

        result.getFirst()
                .ifPresentOrElse(partnerUserId -> {
                    String inviterUsername = result.getSecond();
                    clientNotificationService.sendMessage(inviterUserId, new InviteResponseRecord(inviterUserId, record.userInviteCode(), UserConnectionStatus.PENDING));
                    clientNotificationService.sendMessage(partnerUserId, new InviteNotificationRecord(partnerUserId, inviterUsername));
                }, () -> {
                    String errorMessage = result.getSecond();
                    clientNotificationService.sendError(new ErrorResponseRecord(inviterUserId, errorMessage, MessageType.INVITE_REQUEST));
                });
    }

    @Override
    public Class<InviteRequestRecord> getRequestType() {
        return InviteRequestRecord.class;
    }
}
