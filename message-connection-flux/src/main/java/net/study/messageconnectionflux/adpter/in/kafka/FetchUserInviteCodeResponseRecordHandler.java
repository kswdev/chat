package net.study.messageconnectionflux.adpter.in.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnectionflux.application.dto.kafka.FetchUserInviteCodeResponseRecord;
import net.study.messageconnectionflux.application.dto.websocket.outbound.FetchUserInviteCodeResponse;
import net.study.messageconnectionflux.application.port.out.ClientNotificationService;
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
