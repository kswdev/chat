package net.study.messageconnectionflux.adpter.in.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnectionflux.application.dto.kafka.JoinResponseRecord;
import net.study.messageconnectionflux.application.dto.websocket.outbound.JoinNotification;
import net.study.messageconnectionflux.application.port.out.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JoinResponseRecordHandler implements BaseRecordHandler<JoinResponseRecord> {

    private final ClientNotificationService clientNotificationService;

    public void handleRecord(JoinResponseRecord record) {
        clientNotificationService.sendMessage(
                record.userId(),
                new JoinNotification(record.channelId(), record.title()),
                record);
    }

    @Override
    public Class<JoinResponseRecord> getRequestType() {
        return JoinResponseRecord.class;
    }
}
