package net.study.messageconnection.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.dto.kafka.InviteResponseRecord;
import net.study.messageconnection.dto.websocket.outbound.InviteResponse;
import net.study.messageconnection.service.ClientNotificationService;
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
