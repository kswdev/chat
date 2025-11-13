package net.study.messageconnection.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.dto.kafka.EnterResponseRecord;
import net.study.messageconnection.dto.websocket.outbound.EnterResponse;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnterResponseRecordHandler implements BaseRecordHandler<EnterResponseRecord> {

    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRecord(EnterResponseRecord record) {
        clientNotificationService.sendMessage(
                record.userId(),
                new EnterResponse(record.channelId(), record.title(), record.lastReadMessageSeqId(), record.lastChannelMessageSeqId()),
                record);
    }

    @Override
    public Class<EnterResponseRecord> getRequestType() {
        return EnterResponseRecord.class;
    }
}
