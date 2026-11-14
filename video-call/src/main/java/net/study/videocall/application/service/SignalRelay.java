package net.study.videocall.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import net.study.videocall.application.port.in.RelaySignalUseCase;
import net.study.videocall.application.port.out.LoadRoom;
import net.study.videocall.application.port.out.PeerConnections;
import net.study.videocall.domain.model.Peer;
import net.study.videocall.domain.model.Signal;
import net.study.videocall.domain.model.SignalType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SignalRelay implements RelaySignalUseCase {

    private final LoadRoom loadRoom;
    private final PeerConnections peerConnections;

    @Override
    public Mono<Void> relay(String roomId, Peer sender, SignalType type, JsonNode payload) {
        return loadRoom.loadById(roomId)
                .flatMapMany(room -> Flux.fromIterable(room.otherThan(sender)))
                .flatMap(other -> peerConnections.send(other.getId(), new Signal(sender.getId(), type, payload)))
                .then();
    }
}
