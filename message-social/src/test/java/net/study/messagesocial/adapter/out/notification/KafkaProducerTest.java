package net.study.messagesocial.adapter.out.notification;

import net.study.messagesocial.adapter.out.notification.dto.InviteNotificationRecord;
import net.study.messagesocial.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class KafkaProducerTest {

    @InjectMocks
    private KafkaProducer kafkaProducer;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private JsonUtil jsonUtil;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(kafkaProducer, "pushTopic", "push-notification");
    }

    @Test
    void sendPushNotification_sends_serialized_payload_to_push_topic() {
        InviteNotificationRecord record = new InviteNotificationRecord(1L, "inviter");
        String payload = "{\"userId\":1,\"username\":\"inviter\"}";

        given(jsonUtil.toJson(record)).willReturn(Optional.of(payload));
        given(kafkaTemplate.send(eq("push-notification"), eq(payload)))
                .willReturn(new CompletableFuture<>());

        kafkaProducer.sendPushNotification(record);

        then(kafkaTemplate).should().send("push-notification", payload);
    }

    @Test
    void sendPushNotification_does_nothing_when_serialization_fails() {
        InviteNotificationRecord record = new InviteNotificationRecord(1L, "inviter");
        given(jsonUtil.toJson(record)).willReturn(Optional.empty());

        kafkaProducer.sendPushNotification(record);

        then(kafkaTemplate).should(never()).send(any(), any());
    }
}
