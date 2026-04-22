package net.study.messageconnectionflux.adpter.in.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnectionflux.application.dto.kafka.FetchUserConnectionsResponseRecord;
import net.study.messageconnectionflux.application.dto.websocket.outbound.FetchUserConnectionsResponse;
import net.study.messageconnectionflux.application.port.out.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchUserConnectionsResponseRecordHandler implements BaseRecordHandler<FetchUserConnectionsResponseRecord> {

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
