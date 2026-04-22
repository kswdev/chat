package net.study.messageconnectionflux.adpter.in.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnectionflux.application.dto.kafka.InviteNotificationRecord;
import net.study.messageconnectionflux.application.dto.websocket.outbound.InviteNotification;
import net.study.messageconnectionflux.application.port.out.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InviteNotificationRecordHandler implements BaseRecordHandler<InviteNotificationRecord> {

    private final ClientNotificationService clientNotificationService;

    public void handleRecord(InviteNotificationRecord record) {
        clientNotificationService.sendMessage(
                record.userId(),
                new InviteNotification(record.username()),
                record);
    }

    @Override
    public Class<InviteNotificationRecord> getRequestType() {
        return InviteNotificationRecord.class;
    }
}
