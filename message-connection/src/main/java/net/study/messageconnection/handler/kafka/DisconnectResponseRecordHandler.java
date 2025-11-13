package net.study.messageconnection.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.dto.kafka.DisconnectResponseRecord;
import net.study.messageconnection.dto.websocket.outbound.DisconnectResponse;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisconnectResponseRecordHandler implements BaseRecordHandler<DisconnectResponseRecord> {

    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRecord(DisconnectResponseRecord record) {
        clientNotificationService.sendMessage(record.userId(), new DisconnectResponse(record.username(), record.status()), record);
    }

    @Override
    public Class<DisconnectResponseRecord> getRequestType() {
        return DisconnectResponseRecord.class;
    }
}
