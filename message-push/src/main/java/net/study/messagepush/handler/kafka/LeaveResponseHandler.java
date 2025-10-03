package net.study.messagepush.handler.kafka;

import lombok.extern.slf4j.Slf4j;
import net.study.messagepush.dto.kafka.inbound.LeaveResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LeaveResponseHandler implements BaseRecordHandler<LeaveResponse> {

    @Override
    public void handleRequest(LeaveResponse request) {
        log.info("{} to offline user: {}", request, request.userId());
    }

    @Override
    public Class<LeaveResponse> getRequestType() {
        return LeaveResponse.class;
    }
}
