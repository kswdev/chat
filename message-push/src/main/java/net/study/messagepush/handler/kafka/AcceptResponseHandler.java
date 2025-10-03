package net.study.messagepush.handler.kafka;

import lombok.extern.slf4j.Slf4j;
import net.study.messagepush.dto.kafka.inbound.AcceptResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AcceptResponseHandler implements BaseRecordHandler<AcceptResponse> {

    @Override
    public void handleRequest(AcceptResponse request) {
        log.info("{} to offline user: {}", request, request.userId());
    }

    @Override
    public Class<AcceptResponse> getRequestType() {
        return AcceptResponse.class;
    }
}
