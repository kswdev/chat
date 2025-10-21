package net.study.messagesystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.outbound.RecordInterface;
import net.study.messagesystem.util.JsonUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushService {

    private final JsonUtil jsonUtil;
    private final KafkaProducerService producerService;
    private final HashMap<String, Class<? extends RecordInterface>> pushMessageTypes = new HashMap<>();

    public void registerPushMessageType(String pushMessage, Class<? extends RecordInterface> clazz) {
        this.pushMessageTypes.put(pushMessage, clazz);
    }

    public void pushMessage(UserId userId, String messageType, String message) {
        Class<? extends RecordInterface> recordInterface = pushMessageTypes.get(messageType);
        try {
            if (recordInterface != null) {
                jsonUtil.addValue(message, "userId", userId.id().toString())
                        .flatMap(json -> jsonUtil.fromJson(json, recordInterface))
                        .ifPresent(producerService::sendPushNotification);
                log.info("Push message: {} to user: {}", message, userId);
            } else {
                log.error("Invalid push message type: {}", messageType);
            }
        } catch (Exception e) {
            log.error("cause: {}", e.getMessage());
        }
    }
}
