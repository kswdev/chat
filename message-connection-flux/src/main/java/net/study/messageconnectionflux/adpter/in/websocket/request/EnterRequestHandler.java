package net.study.messageconnectionflux.adpter.in.websocket.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.IdKey;
import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.application.dto.kafka.EnterRequestRecord;
import net.study.messageconnectionflux.application.dto.websocket.inbound.EnterRequest;
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
public class EnterRequestHandler implements BaseRequestHandler<EnterRequest> {

    private final EventProducer eventProducer;
    private final ClientNotificationService clientNotificationService;

    @Override
    public Mono<Void> handleRequest(WebSocketSession senderSession, EnterRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        return eventProducer.sendRequest(
                new EnterRequestRecord(senderUserId, request.getChannelId()),
                () -> clientNotificationService.sendError(senderUserId, new ErrorResponse(MessageType.ENTER_REQUEST, "enter channel failed.")));
    }

    @Override
    public Class<EnterRequest> getRequestType() {
        return EnterRequest.class;
    }
}
