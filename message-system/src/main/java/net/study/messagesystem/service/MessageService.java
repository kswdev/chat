package net.study.messagesystem.service;

import com.mysema.commons.lang.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.ResultType;
import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.message.Message;
import net.study.messagesystem.domain.message.MessageSeqId;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.projection.MessageInfoProjection;
import net.study.messagesystem.dto.websocket.outbound.BaseMessage;
import net.study.messagesystem.dto.websocket.outbound.WriteMessageAck;
import net.study.messagesystem.entity.messae.MessageEntity;
import net.study.messagesystem.repository.MessageRepository;
import net.study.messagesystem.repository.channel.UserChannelRepository;
import net.study.messagesystem.session.WebSocketSessionManager;
import net.study.messagesystem.util.JsonUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final JsonUtil jsonUtil;
    private final PushService pushService;
    private final UserService userService;
    private final ChannelService channelService;
    private final WebSocketSessionManager sessionManager;
    private final MessageRepository messageRepository;
    private final UserChannelRepository userChannelRepository;

    @Transactional
    public void sendMessage(
            UserId senderUserId,
            String content,
            ChannelId channelId,
            MessageSeqId messageSeqId,
            Long serial,
            BaseMessage message
    ) {
        String payload = convertToJson(message);

        saveMessage(channelId, senderUserId, messageSeqId, content);
        sendMessageToParticipants(channelId, senderUserId, messageSeqId, serial, payload);
    }

    @Transactional(readOnly = true)
    public Pair<List<Message>, ResultType> getMessages(
            ChannelId channelId,
            MessageSeqId startMessageSeqId,
            MessageSeqId endMessageSeqId
    ) {
        List<MessageInfoProjection> messageInfos = messageRepository
                .findByChannelIdAndMessageSequenceBetween(channelId.id(), startMessageSeqId.id(), endMessageSeqId.id());

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

    private String convertToJson(BaseMessage message) {
        Optional<String> json = jsonUtil.toJson(message);
        if (json.isEmpty()) {
            log.error("Failed to send message. messageType: {}", message.getType());
            return null;
        }
        return json.get();
    }

    private void saveMessage(ChannelId channelId, UserId senderUserId, MessageSeqId messageSeqId, String content) {
        messageRepository.save(
                new MessageEntity(channelId.id(), messageSeqId.id(), senderUserId.id(), content));
    }

    private void sendMessageToParticipants(ChannelId channelId, UserId senderUserId, MessageSeqId messageSeqId, Long serial, String payload) {
        List<UserId> allParticipantsUserIds = channelService.getParticipantsUserIds(channelId);
        List<UserId> onlineParticipantsUserIds = channelService.getOnlineParticipantsUserIds(channelId, allParticipantsUserIds);

        allParticipantsUserIds.forEach(participantId -> {
            if (senderUserId.equals(participantId)) {
                handleSenderMessage(senderUserId, channelId, messageSeqId, serial);
            } else {
                handleRecipientMessage(participantId, onlineParticipantsUserIds, payload);
            }
        });
    }

    private void handleSenderMessage(UserId senderUserId, ChannelId channelId, MessageSeqId messageSeqId, Long serial) {
        updateLastReadMsgSeq(senderUserId, channelId, messageSeqId);
        sendWriteAckMessage(senderUserId, new WriteMessageAck(messageSeqId, serial));
    }

    private void handleRecipientMessage(UserId participantId, List<UserId> onlineParticipantsUserIds, String payload) {
        if (onlineParticipantsUserIds.contains(participantId)) {
            sendWebSocketMessage(participantId, payload);
        } else {
            sendPushMessage(participantId, payload);
        }
    }

    private void sendWebSocketMessage(UserId participantId, String payload) {
        CompletableFuture.runAsync(() -> {
            try {
                WebSocketSession session = sessionManager.getSession(participantId);
                if (session != null) {
                    sessionManager.sendMessage(session, payload);
                } else {
                    sendPushMessage(participantId, payload);
                }
            } catch (IOException e) {
                log.warn("WebSocket message failed for user {}, falling back to push notification", participantId, e);
                sendPushMessage(participantId, payload);
            }
        });
    }

    private void sendPushMessage(UserId participantId, String payload) {
        pushService.pushMessage(participantId, MessageType.NOTIFY_MESSAGE, payload);
    }

    private void sendWriteAckMessage(UserId userId, WriteMessageAck message) {
        jsonUtil.toJson(message)
                .ifPresent(writeMsgAck -> sendWebSocketMessage(userId, writeMsgAck));
    }
}
