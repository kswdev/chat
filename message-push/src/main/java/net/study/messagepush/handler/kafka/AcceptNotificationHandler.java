package net.study.messagepush.handler.kafka;

import lombok.extern.slf4j.Slf4j;
import net.study.messagepush.dto.kafka.inbound.AcceptNotification;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AcceptNotificationHandler implements BaseRecordHandler<AcceptNotification>{

    @Override
    public void handleRequest(AcceptNotification request) {
        log.info("{} to offline user: {}", request, request.userId());
    }

    @Override
    public Class<AcceptNotification> getRequestType() {
        return AcceptNotification.class;
    }

}
