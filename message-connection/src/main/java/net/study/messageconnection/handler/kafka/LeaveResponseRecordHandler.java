package net.study.messageconnection.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.dto.kafka.LeaveResponseRecord;
import net.study.messageconnection.dto.websocket.outbound.LeaveResponse;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveResponseRecordHandler implements BaseRecordHandler<LeaveResponseRecord> {

    private final ClientNotificationService clientNotificationService;

    public void handleRecord(LeaveResponseRecord record) {
        clientNotificationService.sendMessage(
                record.userId(), new LeaveResponse(), record);
    }

    @Override
    public Class<LeaveResponseRecord> getRequestType() {
        return LeaveResponseRecord.class;
    }
}
