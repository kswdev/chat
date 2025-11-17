package net.study.messagesystem.handler.kafka;

import com.mysema.commons.lang.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.ResultType;
import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.message.Message;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.ErrorResponseRecord;
import net.study.messagesystem.dto.kafka.FetchMessagesRequestRecord;
import net.study.messagesystem.dto.kafka.FetchMessagesResponseRecord;
import net.study.messagesystem.service.ClientNotificationService;
import net.study.messagesystem.service.MessageService;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchMessageRequestRecordHandler implements BaseRecordHandler<FetchMessagesRequestRecord> {

    private final MessageService messageService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRecord(FetchMessagesRequestRecord record) {
        UserId senderUserId = record.userId();
        ChannelId channelId = record.channelId();

        Pair<List<Message>, ResultType> result = messageService.getMessages(channelId, record.startMessageSeqId(), record.endMessageSeqId());

        if (result.getSecond().equals(ResultType.SUCCESS)) {
            List<Message> messages = result.getFirst();
            clientNotificationService.sendMessage(senderUserId, new FetchMessagesResponseRecord(senderUserId, channelId, messages));
        } else {
            clientNotificationService.sendError(new ErrorResponseRecord(senderUserId, result.getSecond().getMessage(), MessageType.FETCH_MESSAGES_REQUEST));
        }
    }

    @Override
    public Class<FetchMessagesRequestRecord> getRequestType() {
        return FetchMessagesRequestRecord.class;
    }
}
