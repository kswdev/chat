package net.study.messagesystem.service;

import com.mysema.commons.lang.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.ResultType;
import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.message.Message;
import net.study.messagesystem.domain.message.MessageSeqId;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.MessageNotificationRecord;
import net.study.messagesystem.dto.kafka.WriteMessageAckRecord;
import net.study.messagesystem.dto.kafka.WriteMessageRecord;
import net.study.messagesystem.dto.projection.MessageInfoProjection;
import net.study.messagesystem.kafka.KafkaProducer;
import net.study.messagesystem.repository.channel.UserChannelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final PushService pushService;
    private final UserService userService;
    private final KafkaProducer kafkaProducer;
    private final SessionService sessionService;
    private final ChannelService channelService;
    private final MessageShardService messageShardService;
    private final UserChannelRepository userChannelRepository;

    @Transactional
    public void sendMessage(WriteMessageRecord record) {
        saveMessage(record);
        sendMessageToParticipants(record);
    }

    @Transactional(readOnly = true)
    public Pair<List<Message>, ResultType> getMessages(
            ChannelId channelId,
            MessageSeqId startMessageSeqId,
            MessageSeqId endMessageSeqId
    ) {
        List<MessageInfoProjection> messageInfos = messageShardService.findByChannelIdAndMessageSequenceBetween(channelId, startMessageSeqId, endMessageSeqId);

        if (messageInfos.isEmpty()) {
            return Pair.of(List.of(), ResultType.SUCCESS);
        }

        Set<UserId> userIds = messageInfos.stream()
                .map(proj -> new UserId(proj.getUserId()))
                .collect(Collectors.toSet());

        Pair<Map<UserId, String>, ResultType> usernameResult = userService.getUsernames(userIds);

        return usernameResult.getSecond() == ResultType.SUCCESS
                ? Pair.of(buildMessages(messageInfos, channelId, usernameResult.getFirst()), ResultType.SUCCESS)
                : Pair.of(List.of(), usernameResult.getSecond());
    }

    private List<Message> buildMessages(List<MessageInfoProjection> messageInfos, ChannelId channelId, Map<UserId, String> usernameMap) {
        return messageInfos.stream()
                .map(proj -> new Message(
                        channelId,
                        new MessageSeqId(proj.getMessageSequence()),
                        usernameMap.getOrDefault(new UserId(proj.getUserId()), "unknown"),
                        proj.getContent()
                ))
                .toList();
    }

    @Transactional
    public void updateLastReadMsgSeq(UserId userId, ChannelId channelId, MessageSeqId messageSeqId) {
        if (userChannelRepository.updateLastReadMsgSeqByUserIdAndChannelId(userId.id(), channelId.id(), messageSeqId.id()) == 0) {
            log.error("Failed to updateLastReadMsgSeq. userId: {}, channelId: {}", userId, channelId);
        }
    }

    private void saveMessage(WriteMessageRecord record) {
        messageShardService.save(record.channelId(), record.messageSeqId(), record.userId(), record.content());
    }

    private void sendMessageToParticipants(WriteMessageRecord record) {
        UserId senderUserId = record.userId();
        ChannelId channelId = record.channelId();
        MessageSeqId messageSeqId = record.messageSeqId();
        String senderUsername = userService.getUsername(senderUserId).orElse("unknown");
        String content = record.content();
        Long serial = record.serial();

        List<UserId> allParticipantsUserIds = channelService.getParticipantsUserIds(channelId);
        List<UserId> onlineParticipantsUserIds = sessionService.getOnlineParticipantUserIds(channelId, allParticipantsUserIds);
        Map<String, List<UserId>> listenTopics = sessionService.getListenTopics(allParticipantsUserIds);

        allParticipantsUserIds.removeAll(onlineParticipantsUserIds);

        listenTopics.forEach((topic, participantIds) -> {
            if (participantIds.contains(senderUserId)) {
                handleSenderMessage(topic, senderUserId, channelId, messageSeqId, serial);
                allParticipantsUserIds.remove(senderUserId);
            }

            kafkaProducer.sendMessageUsingPartitionKey(
                    topic, channelId, senderUserId,
                    new MessageNotificationRecord(senderUserId, channelId, messageSeqId, senderUsername, content, participantIds));
        });

        if (!allParticipantsUserIds.isEmpty()) {
            pushService.pushMessage(new MessageNotificationRecord(senderUserId, channelId, messageSeqId, senderUsername, content, allParticipantsUserIds));
        }
    }

    private void handleSenderMessage(String topic, UserId senderUserId, ChannelId channelId, MessageSeqId messageSeqId, Long serial) {
        updateLastReadMsgSeq(senderUserId, channelId, messageSeqId);
        sendWriteAckMessage(topic, channelId, senderUserId, new WriteMessageAckRecord(senderUserId, serial, messageSeqId));
    }

    private void sendWriteAckMessage(String topic, ChannelId channelId, UserId userId, WriteMessageAckRecord record) {
        kafkaProducer.sendMessageUsingPartitionKey(topic, channelId, userId, record);
    }
}
