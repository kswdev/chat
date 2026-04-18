package net.study.messageconnectionflux.adpter.out.persistence.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnectionflux.domain.user.UserId;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketSessionManager {

    private final ConcurrentHashMap<UserId, Many<String>> sinks = new ConcurrentHashMap<>();

    public Many<String> create(UserId userId) {
        Many<String> sink = Sinks.many()
                .unicast()
                .onBackpressureBuffer();

        sinks.put(userId, sink);
        log.info("Sink stored: {}", userId);
        return sink;
    }

    public void remove(UserId userId) {
        sinks.remove(userId);
        log.info("Sink removed: {}", userId);
    }

    public boolean hasActiveSession(UserId userId) {
        return sinks.containsKey(userId);
    }

    public void pushMessage(UserId userId, String message) {
        Many<String> sink = sinks.get(userId);

        if (sink == null) {
            log.warn("No active session for user: {}", userId);
            return;
        }

        Sinks.EmitResult result = sink.tryEmitNext(message);

        if (result.isFailure()) {
            log.warn("Emit failed: {} for user: {}", result, userId);
        } else {
            log.info("Send message: [{}] to {}", message, userId);
        }
    }
}
