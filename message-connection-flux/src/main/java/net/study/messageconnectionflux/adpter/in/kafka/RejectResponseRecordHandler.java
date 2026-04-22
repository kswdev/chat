package net.study.messageconnectionflux.adpter.in.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnectionflux.application.dto.kafka.RejectResponseRecord;
import net.study.messageconnectionflux.application.dto.websocket.outbound.RejectResponse;
import net.study.messageconnectionflux.application.port.out.ClientNotificationService;
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
