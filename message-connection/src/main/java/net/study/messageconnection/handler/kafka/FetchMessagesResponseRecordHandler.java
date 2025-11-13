package net.study.messageconnection.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.dto.kafka.FetchMessagesResponseRecord;
import net.study.messageconnection.dto.websocket.outbound.FetchMessagesResponse;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchMessagesResponseRecordHandler implements BaseRecordHandler<FetchMessagesResponseRecord> {

    private final ClientNotificationService clientNotificationService;

    public void handleRecord(FetchMessagesResponseRecord record) {
        clientNotificationService.sendMessage(
                record.userId(),
                new FetchMessagesResponse(
                        record.channelId(), record.messages()),
                record);
    }

    @Override
    public Class<FetchMessagesResponseRecord> getRequestType() {
        return FetchMessagesResponseRecord.class;
    }
}
