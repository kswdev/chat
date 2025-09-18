package net.study.messagesystem.service;

import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.dto.domain.user.UserId;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class PushService {

    private final Set<String> pushMessageType = new HashSet<>();

    public void registerPushMessageType(String pushMessage) {
        this.pushMessageType.add(pushMessage);
    }

    public void pushMessage(UserId userId, String messageType, String message) {
        if (pushMessageType.contains(messageType)) {
            log.info("Push message: {} to user: {}", message, userId);
        }
    }
}
