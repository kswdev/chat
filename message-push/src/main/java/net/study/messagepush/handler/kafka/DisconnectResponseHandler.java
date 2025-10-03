package net.study.messagepush.handler.kafka;

import lombok.extern.slf4j.Slf4j;
import net.study.messagepush.dto.kafka.inbound.DisconnectResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DisconnectResponseHandler implements BaseRecordHandler<DisconnectResponse> {

    @Override
    public void handleRequest(DisconnectResponse request) {
        log.info("{} to offline user: {}", request, request.userId());
    }

    @Override
    public Class<DisconnectResponse> getRequestType() {
        return DisconnectResponse.class;
    }
}
