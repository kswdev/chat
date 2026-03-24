package net.study.messageconnection.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.dto.kafka.FetchChannelInviteCodeResponseRecord;
import net.study.messageconnection.dto.kafka.FetchChannelsResponseRecord;
import net.study.messageconnection.dto.websocket.outbound.FetchChannelInviteCodeResponse;
import net.study.messageconnection.dto.websocket.outbound.FetchChannelsResponse;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchChannelsResponseRecordHandler implements BaseRecordHandler<FetchChannelsResponseRecord> {

    private final ClientNotificationService clientNotificationService;

    public void handleRecord(FetchChannelsResponseRecord record) {
        clientNotificationService.sendMessage(
                record.userId(),
                new FetchChannelsResponse(record.channels()),
                record);
    }

    @Override
    public Class<FetchChannelsResponseRecord> getRequestType() {
        return FetchChannelsResponseRecord.class;
    }
}
