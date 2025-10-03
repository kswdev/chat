package net.study.messagepush.handler.kafka;

import lombok.extern.slf4j.Slf4j;
import net.study.messagepush.dto.kafka.inbound.MessageNotification;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageNotificationHandler implements BaseRecordHandler<MessageNotification> {

    @Override
    public void handleRequest(MessageNotification request) {
        log.info("{} to offline user: {}", request, request.userId());
    }

    @Override
    public Class<MessageNotification> getRequestType() {
        return MessageNotification.class;
    }
}
