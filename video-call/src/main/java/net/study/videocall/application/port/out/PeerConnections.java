package net.study.videocall.application.port.out;

import net.study.videocall.domain.model.Signal;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

public interface PeerConnections {
    Mono<Void> register(Long peerId, Consumer<String> connection);
    Mono<Void> remove(Long peerId);
    Mono<Boolean> isConnected(Long peerId);
    Mono<Void> send(Long peerId, Signal signal);
}
