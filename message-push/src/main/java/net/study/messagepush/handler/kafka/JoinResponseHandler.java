package net.study.messagepush.handler.kafka;

import lombok.extern.slf4j.Slf4j;
import net.study.messagepush.dto.kafka.inbound.JoinResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JoinResponseHandler implements BaseRecordHandler<JoinResponse> {

    @Override
    public void handleRequest(JoinResponse request) {
        log.info("{} to offline user: {}", request, request.userId());
    }

    @Override
    public Class<JoinResponse> getRequestType() {
        return JoinResponse.class;
    }
}
