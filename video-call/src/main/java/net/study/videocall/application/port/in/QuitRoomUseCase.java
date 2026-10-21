package net.study.videocall.application.port.in;

import net.study.videocall.domain.model.Peer;
import net.study.videocall.domain.model.Room;
import reactor.core.publisher.Mono;

public interface QuitRoomUseCase {
    Mono<Room> quit(String roomId, Peer quitter);
}
