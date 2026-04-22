package net.study.messageconnectionflux.adpter.in.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnectionflux.application.dto.kafka.FetchMessagesResponseRecord;
import net.study.messageconnectionflux.application.dto.websocket.outbound.FetchMessagesResponse;
import net.study.messageconnectionflux.application.port.out.ClientNotificationService;
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
