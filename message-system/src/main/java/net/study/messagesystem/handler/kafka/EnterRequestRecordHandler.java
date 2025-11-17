package net.study.messagesystem.handler.kafka;

import com.mysema.commons.lang.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.ResultType;
import net.study.messagesystem.domain.channel.ChannelEntry;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.EnterRequestRecord;
import net.study.messagesystem.dto.kafka.EnterResponseRecord;
import net.study.messagesystem.dto.kafka.ErrorResponseRecord;
import net.study.messagesystem.service.ChannelService;
import net.study.messagesystem.service.ClientNotificationService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnterRequestRecordHandler implements BaseRecordHandler<EnterRequestRecord> {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRecord(EnterRequestRecord record) {
        UserId senderUserId = record.userId();

        Pair<Optional<ChannelEntry>, ResultType> result = channelService.enter(record.channelId(), senderUserId);

        result.getFirst()
              .ifPresentOrElse(entry ->
                    clientNotificationService.sendMessage(senderUserId, new EnterResponseRecord(senderUserId, record.channelId(), entry.title(), entry.lastChannelMessageSeqId(), entry.lastReadMessageSeqId()))
                    ,() -> clientNotificationService.sendError(new ErrorResponseRecord(senderUserId, result.getSecond().getMessage(), MessageType.ENTER_REQUEST))
              );
    }

    @Override
    public Class<EnterRequestRecord> getRequestType() {
        return EnterRequestRecord.class;
    }
}
