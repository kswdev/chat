package net.study.messagesocial.adapter.out.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesocial.adapter.out.notification.dto.AcceptNotificationRecord;
import net.study.messagesocial.adapter.out.notification.dto.InviteNotificationRecord;
import net.study.messagesocial.adapter.out.notification.dto.RecordInterface;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushService {

    private final KafkaProducer kafkaProducer;

    public void pushMessage(RecordInterface recordInterface) {
        if (recordInterface instanceof InviteNotificationRecord || recordInterface instanceof AcceptNotificationRecord) {
            kafkaProducer.sendPushNotification(recordInterface);
        } else {
            log.error("Invalid push message type: {}", recordInterface.type());
        }
    }
}
