package net.study.messagesystem.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.FetchChannelsRequestRecord;
import net.study.messagesystem.dto.kafka.FetchChannelsResponseRecord;
import net.study.messagesystem.service.ChannelService;
import net.study.messagesystem.service.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchChannelsRequestRecordHandler implements BaseRecordHandler<FetchChannelsRequestRecord> {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRecord(FetchChannelsRequestRecord record) {
        UserId requestUserId = record.userId();

        clientNotificationService.sendMessage(requestUserId, new FetchChannelsResponseRecord(requestUserId, channelService.getChannels(requestUserId)));
    }
    @Override
    public Class<FetchChannelsRequestRecord> getRequestType() {
        return FetchChannelsRequestRecord.class;
    }
}
