package net.study.messagepush.handler.kafka;

import lombok.extern.slf4j.Slf4j;
import net.study.messagepush.dto.kafka.inbound.CreateResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CreateResponseHandler implements BaseRecordHandler<CreateResponse> {

    @Override
    public void handleRequest(CreateResponse request) {
        log.info("{} to offline user: {}", request, request.userId());
    }

    @Override
    public Class<CreateResponse> getRequestType() {
        return CreateResponse.class;
    }
}
