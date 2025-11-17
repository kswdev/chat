package net.study.messagesystem.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.ErrorResponseRecord;
import net.study.messagesystem.dto.kafka.FetchChannelInviteCodeRequestRecord;
import net.study.messagesystem.dto.kafka.FetchChannelInviteCodeResponseRecord;
import net.study.messagesystem.service.ChannelService;
import net.study.messagesystem.service.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchChannelInviteCodeRequestRecordHandler implements BaseRecordHandler<FetchChannelInviteCodeRequestRecord> {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRecord(FetchChannelInviteCodeRequestRecord record) {
        UserId senderUserId = record.userId();
        boolean isJoined = channelService.isJoined(senderUserId, record.channelId());

        if (!isJoined) {
            clientNotificationService.sendError(new ErrorResponseRecord(senderUserId, "Not joined channel.", MessageType.FETCH_CHANNEL_INVITE_CODE_REQUEST));
            return;
        }

        channelService
                .getInviteCode(record.channelId())
                .ifPresentOrElse(inviteCode ->
                      clientNotificationService.sendMessage(senderUserId, new FetchChannelInviteCodeResponseRecord(senderUserId, record.channelId(), inviteCode)),
                () ->
                      clientNotificationService.sendError(new ErrorResponseRecord(senderUserId, "Fetch channel invite code failed.", MessageType.FETCH_CHANNEL_INVITE_CODE_REQUEST)));

    }

    @Override
    public Class<FetchChannelInviteCodeRequestRecord> getRequestType() {
        return FetchChannelInviteCodeRequestRecord.class;
    }
}
