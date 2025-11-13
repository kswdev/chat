package net.study.messageconnection.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.dto.kafka.AcceptNotificationRecord;
import net.study.messageconnection.dto.websocket.outbound.AcceptNotification;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcceptNotificationRecordHandler implements BaseRecordHandler<AcceptNotificationRecord> {

    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRecord(AcceptNotificationRecord record) {
        clientNotificationService.sendMessage(record.userId(), new AcceptNotification(record.username()), record);
    }

    @Override
    public Class<AcceptNotificationRecord> getRequestType() {
        return AcceptNotificationRecord.class;
    }
}
