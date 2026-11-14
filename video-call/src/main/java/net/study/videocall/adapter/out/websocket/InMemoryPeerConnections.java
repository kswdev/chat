package net.study.videocall.adapter.out.websocket;

import lombok.RequiredArgsConstructor;
import net.study.videocall.application.port.out.PeerConnections;
import net.study.videocall.domain.model.Signal;
import net.study.videocall.util.JsonUtil;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class InMemoryPeerConnections implements PeerConnections {

    private final JsonUtil jsonUtil;

    private final Map<Long, Consumer<String>> connections = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> register(Long peerId, Consumer<String> connection) {
        return Mono.fromRunnable(() -> connections.put(peerId, connection));
    }

    @Override
    public Mono<Void> remove(Long peerId) {
        return Mono.fromRunnable(() -> connections.remove(peerId));
    }

    @Override
    public Mono<Boolean> isConnected(Long peerId) {
        return Mono.fromCallable(() -> connections.containsKey(peerId));
    }

    @Override
    public Mono<Void> send(Long peerId, Signal signal) {
        return Mono.fromRunnable(() -> {
            Consumer<String> connection = connections.get(peerId);
            if (connection == null) {
                return;
            }

            jsonUtil.toJson(signal)
                    .ifPresent(connection);
        });
    }
}
