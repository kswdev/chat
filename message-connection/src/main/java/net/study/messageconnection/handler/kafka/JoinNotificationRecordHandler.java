package net.study.messageconnection.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.dto.kafka.JoinNotificationRecord;
import net.study.messageconnection.dto.websocket.outbound.JoinNotification;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JoinNotificationRecordHandler implements BaseRecordHandler<JoinNotificationRecord> {

    private final ClientNotificationService clientNotificationService;

    public void handleRecord(JoinNotificationRecord record) {
        clientNotificationService.sendMessage(
                record.userId(),
                new JoinNotification(record.channelId(), record.title()),
                record);
    }

    @Override
    public Class<JoinNotificationRecord> getRequestType() {
        return JoinNotificationRecord.class;
    }
}
