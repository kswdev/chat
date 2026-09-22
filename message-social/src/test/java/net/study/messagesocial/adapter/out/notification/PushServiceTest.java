package net.study.messagesocial.adapter.out.notification;

import net.study.messagesocial.adapter.out.notification.dto.AcceptNotificationRecord;
import net.study.messagesocial.adapter.out.notification.dto.InviteNotificationRecord;
import net.study.messagesocial.adapter.out.notification.dto.RecordInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PushServiceTest {

    @InjectMocks
    private PushService pushService;

    @Mock
    private KafkaProducer kafkaProducer;

    @Test
    void pushMessage_forwards_invite_notification_to_kafka() {
        InviteNotificationRecord record = new InviteNotificationRecord(1L, "inviter");

        pushService.pushMessage(record);

        then(kafkaProducer).should().sendPushNotification(record);
    }

    @Test
    void pushMessage_forwards_accept_notification_to_kafka() {
        AcceptNotificationRecord record = new AcceptNotificationRecord(2L, "accepter");

        pushService.pushMessage(record);

        then(kafkaProducer).should().sendPushNotification(record);
    }

    @Test
    void pushMessage_ignores_unsupported_record_types() {
        RecordInterface unsupported = () -> "UNSUPPORTED";

        pushService.pushMessage(unsupported);

        then(kafkaProducer).should(never()).sendPushNotification(unsupported);
    }
}
