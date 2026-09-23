package net.study.messagesystem.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.MessageType;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.ErrorResponseRecord;
import net.study.messagesystem.dto.kafka.FetchUserInviteCodeRequestRecord;
import net.study.messagesystem.dto.kafka.FetchUserInviteCodeResponseRecord;
import net.study.messagesystem.service.ClientNotificationService;
import net.study.messagesystem.service.UserService;
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
