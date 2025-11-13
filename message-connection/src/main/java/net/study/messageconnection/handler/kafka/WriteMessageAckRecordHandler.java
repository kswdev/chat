package net.study.messageconnection.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.dto.kafka.WriteMessageAckRecord;
import net.study.messageconnection.dto.websocket.outbound.WriteMessageAck;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WriteMessageAckRecordHandler implements BaseRecordHandler<WriteMessageAckRecord> {

    private final ClientNotificationService clientNotificationService;

    public void handleRecord(WriteMessageAckRecord record) {
        clientNotificationService.sendMessage(
                record.userId(),
                new WriteMessageAck(record.messageSeqId(), record.serial()),
                record);
    }

    @Override
    public Class<WriteMessageAckRecord> getRequestType() {
        return WriteMessageAckRecord.class;
    }
}
