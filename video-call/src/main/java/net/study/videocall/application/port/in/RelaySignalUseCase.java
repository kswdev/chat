package net.study.videocall.application.port.in;

import com.fasterxml.jackson.databind.JsonNode;
import net.study.videocall.domain.model.Peer;
import net.study.videocall.domain.model.SignalType;
import reactor.core.publisher.Mono;

public interface RelaySignalUseCase {
    Mono<Void> relay(String roomId, Peer sender, SignalType type, JsonNode payload);
}
