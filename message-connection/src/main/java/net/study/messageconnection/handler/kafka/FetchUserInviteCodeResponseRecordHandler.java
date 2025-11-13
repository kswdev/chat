package net.study.messageconnection.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.dto.kafka.FetchUserInviteCodeResponseRecord;
import net.study.messageconnection.dto.websocket.outbound.FetchUserInviteCodeResponse;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchUserInviteCodeResponseRecordHandler implements BaseRecordHandler<FetchUserInviteCodeResponseRecord> {

    private final ClientNotificationService clientNotificationService;

    public void handleRecord(FetchUserInviteCodeResponseRecord record) {
        clientNotificationService.sendMessage(
                record.userId(),
                new FetchUserInviteCodeResponse(record.inviteCode()),
                record);
    }

    @Override
    public Class<FetchUserInviteCodeResponseRecord> getRequestType() {
        return FetchUserInviteCodeResponseRecord.class;
    }
}
