package net.study.messageconnection.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.dto.kafka.FetchUserConnectionsResponseRecord;
import net.study.messageconnection.dto.websocket.outbound.FetchUserConnectionsResponse;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchConnectionsResponseRecordHandler implements BaseRecordHandler<FetchUserConnectionsResponseRecord> {

    private final ClientNotificationService clientNotificationService;

    public void handleRecord(FetchUserConnectionsResponseRecord record) {
        clientNotificationService.sendMessage(
                record.userId(),
                new FetchUserConnectionsResponse(record.connections()),
                record);
    }

    @Override
    public Class<FetchUserConnectionsResponseRecord> getRequestType() {
        return FetchUserConnectionsResponseRecord.class;
    }
}
