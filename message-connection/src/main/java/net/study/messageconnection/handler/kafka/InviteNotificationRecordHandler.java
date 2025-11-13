package net.study.messageconnection.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.dto.kafka.InviteNotificationRecord;
import net.study.messageconnection.dto.websocket.outbound.InviteNotification;
import net.study.messageconnection.service.ClientNotificationService;
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
