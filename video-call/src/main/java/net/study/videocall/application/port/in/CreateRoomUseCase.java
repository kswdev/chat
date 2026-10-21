package net.study.videocall.application.port.in;

import net.study.videocall.domain.model.Peer;
import net.study.videocall.domain.model.Room;
import reactor.core.publisher.Mono;

public interface CreateRoomUseCase {
    Mono<Room> create(String roomName, Peer creator);
}
