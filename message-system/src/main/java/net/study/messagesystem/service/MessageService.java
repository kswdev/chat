package net.study.messagesystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.dto.domain.channel.ChannelId;
import net.study.messagesystem.dto.domain.user.UserId;
import net.study.messagesystem.entity.messae.MessageEntity;
import net.study.messagesystem.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static java.util.function.Predicate.not;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private static final int THREAD_POOL_SIZE = 10;

    private final ChannelService channelService;
    private final MessageRepository messageRepository;
    private final ExecutorService senderThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public void sendMessage(UserId senderUserId, String content, ChannelId channelId, Consumer<UserId> messageSender) {

        try {
            messageRepository.save(new MessageEntity(senderUserId.id(), content));
        } catch (Exception e) {
            log.error("Send message failed. cause: {}", e.getMessage());
            return;
        }

        List<UserId> participantsUserIds = channelService.getOnlineParticipantsUserIds(channelId);
        participantsUserIds.stream()
                        .filter(not(userId -> userId.equals(senderUserId)))
                        .forEach(participantsUserId ->
                                CompletableFuture.runAsync(() -> messageSender.accept(participantsUserId), senderThreadPool)
                        );
    }
}
