package net.study.messageconnectionflux.adpter.in.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnectionflux.application.dto.kafka.CreateResponseRecord;
import net.study.messageconnectionflux.application.dto.websocket.outbound.CreateResponse;
import net.study.messageconnectionflux.application.port.out.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateResponseRecordHandler implements BaseRecordHandler<CreateResponseRecord> {

    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRecord(CreateResponseRecord record) {
        clientNotificationService.sendMessage(record.userId(), new CreateResponse(record.channelId(), record.title()), record);
    }

    @Override
    public Class<CreateResponseRecord> getRequestType() {
        return CreateResponseRecord.class;
    }
}
