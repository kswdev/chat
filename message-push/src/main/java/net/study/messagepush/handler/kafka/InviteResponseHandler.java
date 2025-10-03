package net.study.messagepush.handler.kafka;

import lombok.extern.slf4j.Slf4j;
import net.study.messagepush.dto.kafka.inbound.InviteResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InviteResponseHandler implements BaseRecordHandler<InviteResponse> {

    @Override
    public void handleRequest(InviteResponse request) {
        log.info("{} to offline user: {}", request, request.userId());
    }

    @Override
    public Class<InviteResponse> getRequestType() {
        return InviteResponse.class;
    }
}
