package net.study.messagepush.handler.kafka;

import lombok.extern.slf4j.Slf4j;
import net.study.messagepush.dto.kafka.inbound.QuitResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class QuitResponseHandler implements BaseRecordHandler<QuitResponse> {

    @Override
    public void handleRequest(QuitResponse request) {
        log.info("{} to offline user: {}", request, request.userId());
    }

    @Override
    public Class<QuitResponse> getRequestType() {
        return QuitResponse.class;
    }
}
