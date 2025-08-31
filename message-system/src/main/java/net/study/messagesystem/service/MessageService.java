package net.study.messagesystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.dto.domain.channel.ChannelId;
import net.study.messagesystem.dto.domain.user.UserId;
import net.study.messagesystem.entity.messae.MessageEntity;
import net.study.messagesystem.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

import static java.util.function.Predicate.not;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final ChannelService channelService;
    private final MessageRepository messageRepository;

    public void sendMessage(UserId senderUserId, String content, ChannelId channelId, Consumer<UserId> messageSender) {

        try {
            messageRepository.save(new MessageEntity(senderUserId.id(), content));
        } catch (Exception e) {
            log.error("Send message failed. cause: {}", e.getMessage());
            return;
        }

        List<UserId> participantsUserIds = channelService.getParticipantsUserIds(channelId);
        participantsUserIds.stream()
                        .filter(not(userId -> userId.equals(senderUserId)))
                        .filter(userId -> channelService.isOnline(channelId, userId))
                        .forEach(messageSender);
    }
}
