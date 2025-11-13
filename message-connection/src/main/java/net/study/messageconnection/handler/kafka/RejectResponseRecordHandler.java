package net.study.messageconnection.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.dto.kafka.RejectResponseRecord;
import net.study.messageconnection.dto.websocket.outbound.RejectResponse;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RejectResponseRecordHandler implements BaseRecordHandler<RejectResponseRecord> {

    private final ClientNotificationService clientNotificationService;

    public void handleRecord(RejectResponseRecord record) {
        clientNotificationService.sendMessage(
                record.userId(),
                new RejectResponse(record.username(), record.status()),
                record);
    }

    @Override
    public Class<RejectResponseRecord> getRequestType() {
        return RejectResponseRecord.class;
    }
}
