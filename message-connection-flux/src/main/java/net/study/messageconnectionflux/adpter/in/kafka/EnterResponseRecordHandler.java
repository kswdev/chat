package net.study.messageconnectionflux.adpter.in.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.study.messageconnectionflux.application.dto.kafka.EnterResponseRecord;
import net.study.messageconnectionflux.application.dto.websocket.outbound.EnterResponse;
import net.study.messageconnectionflux.application.port.out.ClientNotificationService;
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
