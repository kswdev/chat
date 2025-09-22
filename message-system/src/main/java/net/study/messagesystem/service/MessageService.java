package net.study.messagesystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.domain.channel.ChannelId;
import net.study.messagesystem.dto.domain.user.UserId;
import net.study.messagesystem.dto.websocket.outbound.BaseMessage;
import net.study.messagesystem.entity.messae.MessageEntity;
import net.study.messagesystem.repository.MessageRepository;
import net.study.messagesystem.session.WebSocketSessionManager;
import net.study.messagesystem.util.JsonUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private static final int THREAD_POOL_SIZE = 10;

    private final JsonUtil jsonUtil;
    private final PushService pushService;
    private final ChannelService channelService;
    private final WebSocketSessionManager sessionManager;
    private final MessageRepository messageRepository;

    public void sendMessage(UserId senderUserId, String content, ChannelId channelId, BaseMessage message) {

        Optional<String> json = jsonUtil.toJson(message);

        if (json.isEmpty())
            log.error("Failed to send message. messageType: {}", message.getType());

        messageRepository.save(new MessageEntity(senderUserId.id(), content));

        String payload = json.get();

        List<UserId> allParticipantsIds = channelService.getParticipantsUserIds(channelId);
        List<UserId> onlineParticipantsUserIds = channelService.getOnlineParticipantsUserIds(channelId, allParticipantsIds);
        log.info("Participants: {}", onlineParticipantsUserIds);

        for (int idx = 0; idx < onlineParticipantsUserIds.size(); idx++) {
            UserId participantsUserId = allParticipantsIds.get(idx);
            if (senderUserId.equals(participantsUserId)) {
                continue;
            }

            if (onlineParticipantsUserIds.get(idx) != null) {
                CompletableFuture.runAsync(() -> {
                    try {
                        WebSocketSession session = sessionManager.getSession(participantsUserId);
                        if (session != null)
                            sessionManager.sendMessage(session, payload);
                        else
                            pushService.pushMessage(participantsUserId, MessageType.NOTIFY_MESSAGE, payload);
                    } catch (IOException e) {
                        pushService.pushMessage(participantsUserId, MessageType.NOTIFY_MESSAGE, payload);
                    }
                });
            } else {
                pushService.pushMessage(participantsUserId, MessageType.NOTIFY_MESSAGE, payload);
            }
        }
    }
}
