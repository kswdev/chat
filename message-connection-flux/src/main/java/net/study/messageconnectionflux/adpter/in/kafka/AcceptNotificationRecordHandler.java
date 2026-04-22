package net.study.messageconnectionflux.adpter.in.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnectionflux.application.dto.kafka.AcceptNotificationRecord;
import net.study.messageconnectionflux.application.dto.websocket.outbound.AcceptNotification;
import net.study.messageconnectionflux.application.port.out.ClientNotificationService;
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
