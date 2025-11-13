package net.study.messageconnection.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.dto.kafka.AcceptNotificationRecord;
import net.study.messageconnection.dto.kafka.CreateResponseRecord;
import net.study.messageconnection.dto.websocket.outbound.AcceptNotification;
import net.study.messageconnection.dto.websocket.outbound.CreateResponse;
import net.study.messageconnection.service.ClientNotificationService;
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
