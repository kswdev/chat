package net.study.messageconnectionflux.handler.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.IdKey;
import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.domain.user.UserId;
import net.study.messageconnectionflux.dto.kafka.AcceptRequestRecord;
import net.study.messageconnectionflux.dto.websocket.inbound.AcceptRequest;
import net.study.messageconnectionflux.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnectionflux.kafka.KafkaProducer;
import net.study.messageconnectionflux.service.ClientNotificationService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcceptRequestHandler implements BaseRequestHandler<AcceptRequest> {

    private final KafkaProducer kafkaProducer;
    private final ClientNotificationService clientNotificationService;

    @Override
    public Mono<Void> handleRequest(WebSocketSession senderSession, AcceptRequest request) {
        UserId accepterUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        return kafkaProducer.sendRequest(
                new AcceptRequestRecord(accepterUserId, request.getUsername()),
                () -> clientNotificationService.sendError(accepterUserId, new ErrorResponse(MessageType.ACCEPT_REQUEST, "accept request failed.")));
    }

    @Override
    public Class<AcceptRequest> getRequestType() {
        return AcceptRequest.class;
    }
}
