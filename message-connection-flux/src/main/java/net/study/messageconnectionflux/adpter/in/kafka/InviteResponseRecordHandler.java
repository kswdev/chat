package net.study.messageconnectionflux.adpter.in.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnectionflux.application.dto.kafka.InviteResponseRecord;
import net.study.messageconnectionflux.application.dto.websocket.outbound.InviteResponse;
import net.study.messageconnectionflux.application.port.out.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InviteResponseRecordHandler implements BaseRecordHandler<InviteResponseRecord> {

    private final ClientNotificationService clientNotificationService;

    public void handleRecord(InviteResponseRecord record) {
        clientNotificationService.sendMessage(
                record.userId(),
                new InviteResponse(record.inviteCode(), record.status()),
                record);
    }

    @Override
    public Class<InviteResponseRecord> getRequestType() {
        return InviteResponseRecord.class;
    }
}
