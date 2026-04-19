package net.study.messageconnectionflux.adpter.in.websocket.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.IdKey;
import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.application.dto.kafka.FetchMessagesRequestRecord;
import net.study.messageconnectionflux.application.dto.websocket.inbound.FetchMessagesRequest;
import net.study.messageconnectionflux.application.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnectionflux.application.port.out.ClientNotificationService;
import net.study.messageconnectionflux.application.port.out.EventProducer;
import net.study.messageconnectionflux.domain.user.UserId;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchMessageRequestHandler implements BaseRequestHandler<FetchMessagesRequest> {

    private final EventProducer eventProducer;
    private final ClientNotificationService clientNotificationService;

    @Override
    public Mono<Void> handleRequest(WebSocketSession senderSession, FetchMessagesRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        return eventProducer.sendMessageUsingPartitionKey(
                request.getChannelId(), senderUserId,
                new FetchMessagesRequestRecord(senderUserId, request.getChannelId(), request.getStartMessageSeqId(), request.getEndMessageSeqId()),
                () -> clientNotificationService.sendError(senderUserId, new ErrorResponse(MessageType.FETCH_MESSAGES_REQUEST, "fetch messages request failed")));
    }

    @Override
    public Class<FetchMessagesRequest> getRequestType() {
        return FetchMessagesRequest.class;
    }
}
