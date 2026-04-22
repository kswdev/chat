package net.study.messageconnectionflux.adpter.in.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnectionflux.application.dto.kafka.FetchChannelInviteCodeResponseRecord;
import net.study.messageconnectionflux.application.dto.websocket.outbound.FetchChannelInviteCodeResponse;
import net.study.messageconnectionflux.application.port.out.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchChannelInviteCodeResponseRecordHandler implements BaseRecordHandler<FetchChannelInviteCodeResponseRecord> {

    private final ClientNotificationService clientNotificationService;

    public void handleRecord(FetchChannelInviteCodeResponseRecord record) {
        clientNotificationService.sendMessage(
                record.userId(),
                new FetchChannelInviteCodeResponse(record.channelId(), record.inviteCode()),
                record);
    }

    @Override
    public Class<FetchChannelInviteCodeResponseRecord> getRequestType() {
        return FetchChannelInviteCodeResponseRecord.class;
    }
}
