package net.study.messageconnection.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.dto.kafka.AcceptResponseRecord;
import net.study.messageconnection.dto.websocket.outbound.AcceptResponse;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcceptResponseRecordHandler implements BaseRecordHandler<AcceptResponseRecord> {

    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRecord(AcceptResponseRecord record) {
        clientNotificationService.sendMessage(record.userId(), new AcceptResponse(record.username()), record);
    }

    @Override
    public Class<AcceptResponseRecord> getRequestType() {
        return AcceptResponseRecord.class;
    }
}
