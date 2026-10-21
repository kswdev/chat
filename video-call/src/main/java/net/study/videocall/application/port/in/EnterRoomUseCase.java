package net.study.videocall.application.port.in;

import net.study.videocall.domain.model.Peer;
import reactor.core.publisher.Mono;

public interface EnterRoomUseCase {
    Mono<Boolean> enter(String roomId, Peer alice);
}
