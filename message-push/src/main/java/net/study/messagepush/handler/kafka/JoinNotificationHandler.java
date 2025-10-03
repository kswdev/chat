package net.study.messagepush.handler.kafka;

import lombok.extern.slf4j.Slf4j;
import net.study.messagepush.dto.kafka.inbound.JoinNotification;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JoinNotificationHandler implements BaseRecordHandler<JoinNotification> {

    @Override
    public void handleRequest(JoinNotification request) {
        log.info("{} to offline user: {}", request, request.userId());
    }

    @Override
    public Class<JoinNotification> getRequestType() {
        return JoinNotification.class;
    }
}
