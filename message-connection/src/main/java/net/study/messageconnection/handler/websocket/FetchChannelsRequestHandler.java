package net.study.messageconnection.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.kafka.FetchChannelsRequestRecord;
import net.study.messageconnection.dto.websocket.inbound.FetchChannelsRequest;
import net.study.messageconnection.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnection.kafka.KafkaProducer;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchChannelsRequestHandler implements BaseRequestHandler<FetchChannelsRequest> {

    private final KafkaProducer kafkaProducer;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRequest(WebSocketSession senderSession, FetchChannelsRequest request) {
        UserId requestUserId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        kafkaProducer.sendRequest(
                new FetchChannelsRequestRecord(requestUserId),
                () -> clientNotificationService.sendError(senderSession, new ErrorResponse(MessageType.FETCH_CHANNELS_REQUEST, "fetch channels failed.")));
    }
    @Override
    public Class<FetchChannelsRequest> getRequestType() {
        return FetchChannelsRequest.class;
    }
}
