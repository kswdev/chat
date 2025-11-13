package net.study.messageconnection.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.dto.kafka.RecordInterface;
import net.study.messageconnection.kafka.KafkaProducer;
import net.study.messageconnection.util.JsonUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushService {

    private final KafkaProducer producerService;
    private final HashMap<String, Class<? extends RecordInterface>> pushMessageTypes = new HashMap<>();

    public void registerPushMessageType(String pushMessage, Class<? extends RecordInterface> clazz) {
        this.pushMessageTypes.put(pushMessage, clazz);
    }

    public void pushMessage(RecordInterface recordInterface) {
        String messageType = recordInterface.type();
        if (pushMessageTypes.containsKey(messageType)) {
            producerService.sendPushNotification(recordInterface);
        } else {
            log.error("Invalid push message type: {}", messageType);
        }
    }
}
