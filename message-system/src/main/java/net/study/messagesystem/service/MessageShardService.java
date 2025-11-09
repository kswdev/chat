package net.study.messagesystem.service;

import lombok.RequiredArgsConstructor;
import net.study.messagesystem.database.ShardContext;
import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.message.MessageSeqId;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.projection.MessageInfoProjection;
import net.study.messagesystem.entity.messae.MessageEntity;
import net.study.messagesystem.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageShardService {

    private final MessageRepository messageRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<MessageInfoProjection> findByChannelIdAndMessageSequenceBetween(ChannelId channelId, MessageSeqId startMessageSeqId, MessageSeqId endMessageSeqId) {
        try(ShardContext.ShardContextScope ignored = new ShardContext.ShardContextScope(channelId.id())) {
            return messageRepository.findByChannelIdAndMessageSequenceBetween(channelId.id(), startMessageSeqId.id(), endMessageSeqId.id());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public MessageSeqId findLastMessageSequenceByChannelId(ChannelId channelId) {
        try(ShardContext.ShardContextScope ignored = new ShardContext.ShardContextScope(channelId.id())) {
            return messageRepository.findLastMessageSequenceByChannelId(channelId.id())
                    .map(MessageSeqId::new)
                    .orElseGet(() -> new MessageSeqId(0L));
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(ChannelId channelId, MessageSeqId messageSeqId, UserId senderUserId, String content) {
        try(ShardContext.ShardContextScope ignored = new ShardContext.ShardContextScope(channelId.id())) {
            messageRepository.save(
                    new MessageEntity(channelId.id(), messageSeqId.id(), senderUserId.id(), content));
        }
    }
}
