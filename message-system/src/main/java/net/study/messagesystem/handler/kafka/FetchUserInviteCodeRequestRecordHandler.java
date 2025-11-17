package net.study.messagesystem.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.ErrorResponseRecord;
import net.study.messagesystem.dto.kafka.FetchUserInviteCodeRequestRecord;
import net.study.messagesystem.dto.kafka.FetchUserInviteCodeResponseRecord;
import net.study.messagesystem.service.ClientNotificationService;
import net.study.messagesystem.service.UserService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchUserInviteCodeRequestRecordHandler implements BaseRecordHandler<FetchUserInviteCodeRequestRecord> {

    private final UserService userService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRecord(FetchUserInviteCodeRequestRecord record) {
        UserId requestUserId = record.userId();

        userService.getInviteCode(requestUserId)
                   .ifPresentOrElse(
                           inviteCode ->
                                   clientNotificationService.sendMessage(requestUserId, new FetchUserInviteCodeResponseRecord(requestUserId, inviteCode))
                           ,() ->
                                   clientNotificationService.sendError(new ErrorResponseRecord(requestUserId, MessageType.FETCH_USER_INVITE_CODE_REQUEST, "fetch user invite code failed.")));
    }
    @Override
    public Class<FetchUserInviteCodeRequestRecord> getRequestType() {
        return FetchUserInviteCodeRequestRecord.class;
    }
}
