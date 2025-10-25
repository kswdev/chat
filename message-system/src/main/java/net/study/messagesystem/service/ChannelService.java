package net.study.messagesystem.service;

import com.mysema.commons.lang.Pair;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.KeyPrefix;
import net.study.messagesystem.constant.ResultType;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.domain.channel.Channel;
import net.study.messagesystem.domain.channel.ChannelEntry;
import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.message.MessageSeqId;
import net.study.messagesystem.domain.user.InviteCode;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.projection.ChannelTitleProjection;
import net.study.messagesystem.dto.projection.InviteCodeProjection;
import net.study.messagesystem.dto.projection.LastReadMsgSeqProjection;
import net.study.messagesystem.dto.projection.UserIdProjection;
import net.study.messagesystem.entity.channel.ChannelEntity;
import net.study.messagesystem.entity.channel.UserChannelEntity;
import net.study.messagesystem.repository.MessageRepository;
import net.study.messagesystem.repository.channel.ChannelRepository;
import net.study.messagesystem.repository.channel.UserChannelRepository;
import net.study.messagesystem.util.JsonUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelService {

    private final JsonUtil jsonUtil;
    private final CacheService cacheService;
    private final SessionService sessionService;
    private final UserConnectionService userConnectionService;
    private final UserChannelRepository userChannelRepository;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;

    @Transactional(readOnly = true)
    public Optional<Channel> getChannel(InviteCode inviteCode) {
        String key = cacheService.buildKey(KeyPrefix.CHANNEL, inviteCode.code());

        return cacheService.get(key)
                .flatMap(json -> jsonUtil.fromJson(json, Channel.class))
                .or(() -> channelRepository.findChannelByInviteCode(inviteCode.code())
                        .map(channel -> {
                            Channel ch = new Channel(new ChannelId(channel.getChannelId()), channel.getTitle(), channel.getHeadCount());
                            jsonUtil.toJson(ch).ifPresent(json -> cacheService.set(key, json, 3600));
                            return ch;
                        }));
    }

    @Transactional(readOnly = true)
    public List<Channel> getChannels(UserId userId) {
        String key = cacheService.buildKey(KeyPrefix.CHANNELS, userId.id().toString());

        return cacheService.get(key)
                .map(channelJsonList -> jsonUtil.fromJsonToList(channelJsonList, Channel.class))
                .orElseGet(() -> {
                    List<Channel> channels = userChannelRepository.findChannelsByUserId(userId.id()).stream()
                            .map(channel -> new Channel(new ChannelId(channel.getChannelId()), channel.getTitle(), channel.getHeadCount()))
                            .toList();
                    jsonUtil.toJson(channels).ifPresent(json -> cacheService.set(key, json, 3600));
                    return channels;
                });
    }

    @Transactional(readOnly = true)
    public Optional<InviteCode> getInviteCode(ChannelId channelId) {
        String key = cacheService.buildKey(KeyPrefix.CHANNEL_INVITECODE, channelId.id().toString());

        return cacheService.get(key)
                .map(InviteCode::new)
                .or(() -> channelRepository.findInviteCodeByChannelId(channelId.id())
                        .map(InviteCodeProjection::getInviteCode)
                        .map(inviteCode -> {
                            cacheService.set(key, inviteCode, 3600);
                            return new InviteCode(inviteCode);
                        })
                        .or(() -> {
                            log.warn("InviteCode does not exists. channelId: {}", channelId);
                            return Optional.empty();
                        })
                );
    }

    @Transactional(readOnly = true)
    public boolean isJoined(UserId userId, ChannelId channelId) {
        String key = cacheService.buildKey(KeyPrefix.JOINED_CHANNEL, channelId.id().toString(), userId.id().toString());

        Optional<String> cachedChannel = cacheService.get(key);
        if (cachedChannel.isPresent()) {
            return true;
        }

        boolean fromDb = userChannelRepository.existsByUserIdAndChannelId(userId.id(), channelId.id());
        if (fromDb) {
            cacheService.set(key, "1", 3600);
        }
        return fromDb;
    }

    @Transactional(readOnly = true)
    public List<UserId> getParticipantsUserIds(ChannelId channelId) {
        String key = cacheService.buildKey(KeyPrefix.PARTICIPANT_IDS, channelId.id().toString());

        return cacheService.get(key)
                .map(userIds -> jsonUtil.fromJsonToList(userIds, String.class))
                .map(userIds -> userIds.stream()
                        .map(Long::valueOf)
                        .map(UserId::new)
                        .toList())
                .orElseGet(() -> {
                    List<UserId> userIds = userChannelRepository.findUserIdsByChannelId(channelId.id()).stream()
                            .map(UserIdProjection::getUserId)
                            .map(UserId::new)
                            .toList();
                    jsonUtil.toJson(userIds.stream().map(UserId::id).toList()).ifPresent(json -> cacheService.set(key, json, 3600));
                    return userIds;
                });
    }

    public List<UserId> getOnlineParticipantsUserIds(ChannelId channelId, List<UserId> userIds) {
        return sessionService.getOnlineParticipantUserIds(channelId, userIds);
    }

    public boolean leave(UserId userId) {
        return sessionService.deActiveChannel(userId);
    }

    @Transactional
    public ResultType quit(ChannelId channelId, UserId userId) {

        if (!isJoined(userId, channelId))
            return ResultType.NOT_JOINED;

        try {
            channelRepository
                    .findForUpdateByChannelId(channelId.id())
                    .ifPresentOrElse(entity -> {
                        entity.decreaseHeadCount();
                        userChannelRepository.deleteByUserIdAndChannelId(userId.id(), channelId.id());
                        cacheService.delete(cacheService.buildKey(KeyPrefix.CHANNEL, entity.getInviteCode()));
                        cacheService.delete(cacheService.buildKey(KeyPrefix.CHANNELS, userId.id().toString()));
                    }, () -> {
                        throw new EntityNotFoundException("Channel does not exists. channelId: " + channelId);
                    });

            return ResultType.SUCCESS;
        } catch (IllegalArgumentException e) {
            log.error("Quit channel on error. channelId: {}, cause: {}", channelId.id(), e.getMessage());
            userChannelRepository.deleteByUserIdAndChannelId(userId.id(), channelId.id());
            return ResultType.SUCCESS;
        }
    }

    @Transactional
    public Pair<Optional<Channel>, ResultType> join(InviteCode inviteCode, UserId userId) {
        Optional<Channel> ch = getChannel(inviteCode);

        if (ch.isEmpty())
            return Pair.of(Optional.empty(), ResultType.NOT_FOUND);

        Channel channel = ch.get();

        if (isJoined(userId, channel.channelId()))
            return Pair.of(Optional.of(channel), ResultType.ALREADY_JOINED);

        try {
            channelRepository
                    .findForUpdateByChannelId(channel.channelId().id())
                    .ifPresentOrElse(entity -> {
                        entity.increaseHeadCount();
                        userChannelRepository.save(new UserChannelEntity(userId.id(), entity.getChannelId(), 0L));
                        cacheService.delete(cacheService.buildKey(KeyPrefix.CHANNEL, entity.getInviteCode()));
                        cacheService.delete(cacheService.buildKey(KeyPrefix.CHANNELS, userId.id().toString()));
                    }, () -> {
                        throw new EntityNotFoundException("Channel does not exists. channelId: " + channel.channelId());
                    });

            return Pair.of(Optional.of(channel), ResultType.SUCCESS);
        } catch (IllegalArgumentException e) {
            return Pair.of(Optional.empty(), ResultType.OVER_LIMIT);
        }
    }

    @Transactional
    public Pair<Optional<Channel>, ResultType> create(UserId senderUserId, List<UserId> participantUserIds, String title) {
        return validateCreateRequest(title)
                .map(validationResult -> validateUserConnections(senderUserId, participantUserIds))
                .orElse(Pair.of(Optional.empty(), ResultType.INVALID_ARGS))
                .getFirst()
                .map(unused -> executeChannelCreation(senderUserId, participantUserIds, title))
                .orElse(Pair.of(Optional.empty(), ResultType.NOT_ALLOWED));
    }

    @Transactional(readOnly = true)
    public Pair<Optional<ChannelEntry>, ResultType> enter(ChannelId channelId, UserId userId) {
        if (!isJoined(userId, channelId)) {
            log.warn("Enter channel failed. User not joined channel. userId: {}, channelId: {}", userId, channelId);
            return Pair.of(Optional.empty(), ResultType.NOT_JOINED);
        }

        return channelRepository.findChannelTitleByChannelId(channelId.id())
                .map(ChannelTitleProjection::getTitle)
                .map(title -> buildChannelEntry(channelId, userId, title))
                .orElse(Pair.of(Optional.empty(), ResultType.NOT_FOUND));
    }

    private Optional<ResultType> validateCreateRequest(String title) {
        if (title != null && title.isEmpty()) {
            log.warn("Invalid args: title is empty");
            return Optional.empty();
        }
        return Optional.of(ResultType.SUCCESS);
    }

    private Pair<Optional<ResultType>, ResultType> validateUserConnections(UserId senderUserId, List<UserId> participantUserIds) {
        boolean allAccepted = userConnectionService.countConnectionStatus(senderUserId, participantUserIds, UserConnectionStatus.ACCEPTED)
                == participantUserIds.size();

        if (!allAccepted) {
            log.warn("user connection status not accepted. participantId: {}", participantUserIds);
            return Pair.of(Optional.empty(), ResultType.NOT_ALLOWED);
        }

        return Pair.of(Optional.of(ResultType.SUCCESS), ResultType.SUCCESS);
    }

    private Pair<Optional<Channel>, ResultType> executeChannelCreation(UserId senderUserId, List<UserId> participantUserIds, String title) {
        try {
            int headCount = participantUserIds.size() + 1;
            ChannelEntity channelEntity = channelRepository.save(ChannelEntity.create(title, headCount));

            createUserChannelEntries(senderUserId, participantUserIds, channelEntity.getChannelId());

            Channel channel = new Channel(new ChannelId(channelEntity.getChannelId()), title, headCount);
            return Pair.of(Optional.of(channel), ResultType.SUCCESS);

        } catch (IllegalArgumentException iae) {
            log.warn("Over limit of channel. participantIds count={}, title={}", participantUserIds.size(), title);
            return Pair.of(Optional.empty(), ResultType.OVER_LIMIT);
        } catch (Exception e) {
            log.error("Failed to create channel. cause: {}", e.getMessage());
            throw e;
        }
    }

    private void createUserChannelEntries(UserId senderUserId, List<UserId> participantUserIds, Long channelId) {
        List<UserChannelEntity> channelEntries = participantUserIds.stream()
                .map(userId -> new UserChannelEntity(userId.id(), channelId, 0L))
                .collect(Collectors.toList());

        channelEntries.add(new UserChannelEntity(senderUserId.id(), channelId, 0L));
        userChannelRepository.saveAll(channelEntries);
    }

    private Pair<Optional<ChannelEntry>, ResultType> buildChannelEntry(ChannelId channelId, UserId userId, String title) {
        return userChannelRepository.findLastReadMsgSeqByUserIdAndChannelId(userId.id(), channelId.id())
                .map(LastReadMsgSeqProjection::getLastReadMsgSeq)
                .map(MessageSeqId::new)
                .map(lastReadMsgSeq -> createChannelEntry(channelId, userId, title, lastReadMsgSeq))
                .orElseGet(() -> {
                    log.error("Enter channel failed. no record is found. userId: {}, channelId: {}", userId, channelId);
                    return Pair.of(Optional.empty(), ResultType.NOT_FOUND);
                });
    }

    private Pair<Optional<ChannelEntry>, ResultType> createChannelEntry(ChannelId channelId, UserId userId, String title, MessageSeqId lastReadMsgSeq) {
        MessageSeqId lastChannelMessageSeqId = messageRepository.findLastMessageSequenceByChannelId(channelId.id())
                .map(MessageSeqId::new)
                .orElseGet(() -> new MessageSeqId(0L));

        return sessionService.setActiveChannel(userId, channelId)
                ? Pair.of(Optional.of(new ChannelEntry(title, lastReadMsgSeq, lastChannelMessageSeqId)), ResultType.SUCCESS)
                : logErrorAndReturnFailure(channelId, userId);
    }

    private Pair<Optional<ChannelEntry>, ResultType> logErrorAndReturnFailure(ChannelId channelId, UserId userId) {
        log.error("Enter channel failed. channelId: {}, userId: {}", channelId, userId);
        return Pair.of(Optional.empty(), ResultType.FAILED);
    }
}
