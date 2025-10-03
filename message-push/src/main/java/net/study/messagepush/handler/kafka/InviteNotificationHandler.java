package net.study.messagepush.handler.kafka;

import lombok.extern.slf4j.Slf4j;
import net.study.messagepush.dto.kafka.inbound.InviteNotification;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InviteNotificationHandler implements BaseRecordHandler<InviteNotification> {

    @Override
    public void handleRequest(InviteNotification request) {
        log.info("{} to offline user: {}", request, request.userId());
    }

    @Override
    public Class<InviteNotification> getRequestType() {
        return InviteNotification.class;
    }
}
