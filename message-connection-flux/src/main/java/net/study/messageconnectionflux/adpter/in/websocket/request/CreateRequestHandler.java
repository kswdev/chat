package net.study.messageconnectionflux.adpter.in.websocket.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.IdKey;
import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.application.dto.kafka.CreateRequestRecord;
import net.study.messageconnectionflux.application.dto.websocket.inbound.CreateRequest;
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
public class CreateRequestHandler implements BaseRequestHandler<CreateRequest> {

    private final EventProducer kafkaProducer;
    private final ClientNotificationService clientNotificationService;

    @Override
    public Mono<Void> handleRequest(WebSocketSession senderSession, CreateRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        return kafkaProducer.sendRequest(
                new CreateRequestRecord(senderUserId, request.getTitle(), request.getParticipantUsernames()),
                () -> clientNotificationService.sendError(senderUserId, new ErrorResponse(MessageType.CREATE_REQUEST, "create channel failed.")));
    }

    @Override
    public Class<CreateRequest> getRequestType() {
        return CreateRequest.class;
    }
}
