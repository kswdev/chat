package net.study.messagesystem.service;

import com.mysema.commons.lang.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.ResultType;
import net.study.messagesystem.dto.domain.channel.Channel;
import net.study.messagesystem.dto.domain.channel.ChannelId;
import net.study.messagesystem.dto.domain.user.UserId;
import net.study.messagesystem.dto.projection.ChannelTitleProjection;
import net.study.messagesystem.entity.channel.ChannelEntity;
import net.study.messagesystem.entity.channel.UserChannelEntity;
import net.study.messagesystem.repository.channel.ChannelRepository;
import net.study.messagesystem.repository.channel.UserChannelRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelService {

    private final SessionService sessionService;
    private final UserChannelRepository userChannelRepository;
    private final ChannelRepository channelRepository;

    public boolean isJoined(UserId userId, ChannelId channelId) {
        return userChannelRepository.existsByUserIdAndChannelId(userId.id(), channelId.id());
    }

    public Pair<Optional<Channel>, ResultType> create(UserId senderUserId, UserId participantId, String title) {
        if (title != null && title.isEmpty()) {
            log.warn("Invalid args: title is empty");
            return Pair.of(Optional.empty(), ResultType.INVALID_ARGS);
        }

        try {
            final int HEAD_COUNT = 2;

            ChannelEntity channelEntity = channelRepository.save(new ChannelEntity(title, HEAD_COUNT));
            Long channelId = channelEntity.getChannelId();
            List<UserChannelEntity> userChannelList = List.of(new UserChannelEntity(senderUserId.id(), channelId, 0L),
                                                              new UserChannelEntity(participantId.id(), channelId, 0L));

            userChannelRepository.saveAll(userChannelList);

            Channel channel = new Channel(new ChannelId(channelId), title, HEAD_COUNT);
            return Pair.of(Optional.of(channel), ResultType.SUCCESS);
        } catch (Exception e) {
            log.error("Failed to create channel. cause: {}", e.getMessage());
            throw e;
        }
    }

    public Pair<Optional<String>, ResultType> enter(ChannelId channelId, UserId userId) {
        if (!isJoined(userId, channelId)) {
            log.warn("Enter channel failed. User not joined channel. userId: {}, channelId: {}", userId, channelId);
            return Pair.of(Optional.empty(), ResultType.NOT_JOINED);
        }

        Optional<String> title = channelRepository.findChannelTitleByChannelId(channelId.id())
                .map(ChannelTitleProjection::getTitle);

        if (title.isEmpty()) {
            log.warn("Enter channel failed. channel does not exists channelId: {}, userId: {}", channelId, userId);
            return Pair.of(Optional.empty(), ResultType.NOT_FOUND);
        }

        if (sessionService.setActiveChannel(userId, channelId)) {
            return Pair.of(title, ResultType.SUCCESS);
        }

        log.error("Enter channel failed. channelId: {}, userId: {}", channelId, userId);
        return Pair.of(Optional.empty(), ResultType.FAILED);
    }
}
