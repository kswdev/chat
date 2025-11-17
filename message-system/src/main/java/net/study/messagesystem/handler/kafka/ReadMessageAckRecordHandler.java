package net.study.messagesystem.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.ReadMessageAckRecord;
import net.study.messagesystem.service.MessageService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReadMessageAckRecordHandler implements BaseRecordHandler<ReadMessageAckRecord> {

    private final MessageService messageService;

    @Override
    public void handleRecord(ReadMessageAckRecord record) {
        UserId senderUserId = record.userId();
        ChannelId channelId = record.channelId();
        messageService.updateLastReadMsgSeq(senderUserId, channelId, record.messageSeqId());
    }

    @Override
    public Class<ReadMessageAckRecord> getRequestType() {
        return ReadMessageAckRecord.class;
    }
}
