package net.study.messageconnectionflux.adpter.in.websocket.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.IdKey;
import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.application.dto.kafka.QuitRequestRecord;
import net.study.messageconnectionflux.application.dto.websocket.inbound.QuitRequest;
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
public class QuitRequestHandler implements BaseRequestHandler<QuitRequest> {

    private final EventProducer eventProducer;
    private final ClientNotificationService clientNotificationService;

    @Override
    public Mono<Void> handleRequest(WebSocketSession senderSession, QuitRequest request) {
        UserId senderUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());
        return eventProducer.sendRequest(
                new QuitRequestRecord(senderUserId, request.getChannelId()),
                () -> clientNotificationService.sendError(senderUserId, new ErrorResponse(MessageType.QUIT_REQUEST, "Quit failed.")));
    }

    @Override
    public Class<QuitRequest> getRequestType() {
        return QuitRequest.class;
    }
}
