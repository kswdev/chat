package net.study.messagesystem.handler.websocket;

import com.mysema.commons.lang.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.IdKey;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.ResultType;
import net.study.messagesystem.domain.channel.Channel;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.websocket.inbound.CreateRequest;
import net.study.messagesystem.dto.websocket.outbound.CreateResponse;
import net.study.messagesystem.dto.websocket.outbound.ErrorResponse;
import net.study.messagesystem.dto.websocket.outbound.JoinNotification;
import net.study.messagesystem.service.ChannelService;
import net.study.messagesystem.service.ClientNotificationService;
import net.study.messagesystem.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateRequestHandler implements BaseRequestHandler<CreateRequest> {

    private final UserService userService;
    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRequest(WebSocketSession senderSession, CreateRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        List<UserId> participantIds = userService.getUserIds(request.getParticipantUsernames());

        if (participantIds.isEmpty()) {
            clientNotificationService.sendMessage(
                    senderSession,
                    senderUserId,
                    new ErrorResponse(ResultType.NOT_FOUND.getMessage(), MessageType.CREATE_REQUEST));

            return;
        }

        Pair<Optional<Channel>, ResultType> result;

        try {
            result = channelService.create(senderUserId, participantIds, request.getTitle());
        } catch (Exception e) {
            clientNotificationService.sendMessage(senderSession, senderUserId, new ErrorResponse(e.getMessage(), MessageType.CREATE_REQUEST));
            return;
        }

        result.getFirst()
              .ifPresentOrElse(channel -> {
                  clientNotificationService.sendMessage(senderSession, senderUserId, new CreateResponse(channel.channelId(), channel.title()));

                  participantIds.forEach((participantId) ->
                          CompletableFuture.runAsync(() ->
                                  clientNotificationService.sendMessage(participantId, new JoinNotification(channel.channelId(), channel.title()))
                          )
                  );

              }, () -> {
                  String errorMessage = result.getSecond().getMessage();
                  clientNotificationService.sendMessage(senderSession, senderUserId, new ErrorResponse(errorMessage, MessageType.CREATE_REQUEST));
              });

    }

    @Override
    public Class<CreateRequest> getRequestType() {
        return CreateRequest.class;
    }
}
