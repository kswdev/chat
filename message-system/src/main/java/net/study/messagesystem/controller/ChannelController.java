package net.study.messagesystem.controller;

import com.mysema.commons.lang.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.IdKey;
import net.study.messagesystem.constant.ResultType;
import net.study.messagesystem.domain.channel.Channel;
import net.study.messagesystem.domain.channel.ChannelEntry;
import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.user.InviteCode;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.JoinNotificationRecord;
import net.study.messagesystem.dto.rest.channel.ChannelEntryResponse;
import net.study.messagesystem.dto.rest.channel.ChannelErrorResponse;
import net.study.messagesystem.dto.rest.channel.ChannelInviteCodeResponse;
import net.study.messagesystem.dto.rest.channel.ChannelResponse;
import net.study.messagesystem.dto.rest.channel.ChannelsResponse;
import net.study.messagesystem.dto.rest.channel.CreateChannelRequest;
import net.study.messagesystem.service.ChannelService;
import net.study.messagesystem.service.ClientNotificationService;
import net.study.messagesystem.service.MessageUserClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/channel")
public class ChannelController {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;
    private final MessageUserClient messageUserClient;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateChannelRequest request, @RequestHeader HttpHeaders headers) {
        UserId senderUserId = currentUserId(headers);
        List<UserId> participantIds = messageUserClient.getUserIds(request.participantUsernames());

        if (participantIds.isEmpty()) {
            return error(HttpStatus.NOT_FOUND, ResultType.NOT_FOUND.getMessage());
        }

        Pair<Optional<Channel>, ResultType> result;
        try {
            result = channelService.create(senderUserId, participantIds, request.title());
        } catch (Exception e) {
            log.error("Failed to create channel. senderUserId: {}, cause: {}", senderUserId, e.getMessage(), e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ResultType.FAILED.getMessage());
        }

        return result.getFirst()
                .<ResponseEntity<?>>map(channel -> {
                    notifyParticipants(participantIds, channel);
                    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(channel));
                })
                .orElseGet(() -> error(toStatus(result.getSecond()), result.getSecond().getMessage()));
    }

    @PostMapping("/join/{inviteCode}")
    public ResponseEntity<?> join(@PathVariable String inviteCode, @RequestHeader HttpHeaders headers) {
        UserId userId = currentUserId(headers);

        Pair<Optional<Channel>, ResultType> result;
        try {
            result = channelService.join(new InviteCode(inviteCode), userId);
        } catch (Exception e) {
            log.error("Failed to join channel. userId: {}, cause: {}", userId, e.getMessage(), e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ResultType.FAILED.getMessage());
        }

        return result.getFirst()
                .<ResponseEntity<?>>map(channel -> ResponseEntity.ok(toResponse(channel)))
                .orElseGet(() -> error(toStatus(result.getSecond()), result.getSecond().getMessage()));
    }

    @PostMapping("/{channelId}/quit")
    public ResponseEntity<?> quit(@PathVariable Long channelId, @RequestHeader HttpHeaders headers) {
        UserId userId = currentUserId(headers);

        ResultType result;
        try {
            result = channelService.quit(new ChannelId(channelId), userId);
        } catch (Exception e) {
            log.error("Failed to quit channel. userId: {}, channelId: {}, cause: {}", userId, channelId, e.getMessage(), e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ResultType.FAILED.getMessage());
        }

        return result == ResultType.SUCCESS
                ? ResponseEntity.noContent().build()
                : error(toStatus(result), result.getMessage());
    }

    @PostMapping("/{channelId}/enter")
    public ResponseEntity<?> enter(@PathVariable Long channelId, @RequestHeader HttpHeaders headers) {
        UserId userId = currentUserId(headers);

        Pair<Optional<ChannelEntry>, ResultType> result = channelService.enter(new ChannelId(channelId), userId);

        return result.getFirst()
                .<ResponseEntity<?>>map(entry -> ResponseEntity.ok(new ChannelEntryResponse(
                        new ChannelId(channelId), entry.title(), entry.lastReadMessageSeqId(), entry.lastChannelMessageSeqId())))
                .orElseGet(() -> error(toStatus(result.getSecond()), result.getSecond().getMessage()));
    }

    @PostMapping("/leave")
    public ResponseEntity<?> leave(@RequestHeader HttpHeaders headers) {
        UserId userId = currentUserId(headers);

        return channelService.leave(userId)
                ? ResponseEntity.noContent().build()
                : error(HttpStatus.INTERNAL_SERVER_ERROR, ResultType.FAILED.getMessage());
    }

    @GetMapping
    public ResponseEntity<ChannelsResponse> fetchChannels(@RequestHeader HttpHeaders headers) {
        UserId userId = currentUserId(headers);
        return ResponseEntity.ok(new ChannelsResponse(channelService.getChannels(userId)));
    }

    @GetMapping("/{channelId}/invite-code")
    public ResponseEntity<?> fetchInviteCode(@PathVariable Long channelId, @RequestHeader HttpHeaders headers) {
        UserId userId = currentUserId(headers);
        ChannelId id = new ChannelId(channelId);

        if (!channelService.isJoined(userId, id)) {
            return error(HttpStatus.CONFLICT, "Not joined channel.");
        }

        return channelService.getInviteCode(id)
                .<ResponseEntity<?>>map(inviteCode -> ResponseEntity.ok(new ChannelInviteCodeResponse(id, inviteCode)))
                .orElseGet(() -> error(HttpStatus.INTERNAL_SERVER_ERROR, "Fetch channel invite code failed."));
    }

    private void notifyParticipants(List<UserId> participantIds, Channel channel) {
        participantIds.forEach(participantId ->
                CompletableFuture.runAsync(() ->
                        clientNotificationService.sendMessage(
                                participantId, new JoinNotificationRecord(participantId, channel.channelId(), channel.title()))));
    }

    private ChannelResponse toResponse(Channel channel) {
        return new ChannelResponse(channel.channelId(), channel.title());
    }

    private ResponseEntity<ChannelErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ChannelErrorResponse(message));
    }

    private HttpStatus toStatus(ResultType resultType) {
        return switch (resultType) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INVALID_ARGS -> HttpStatus.BAD_REQUEST;
            case NOT_ALLOWED -> HttpStatus.FORBIDDEN;
            case NOT_JOINED, OVER_LIMIT -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private UserId currentUserId(HttpHeaders headers) {
        return new UserId(Long.valueOf(headers.getFirst(IdKey.USER_ID.getValue())));
    }
}
