package net.study.videocall.application.port.out;

import net.study.videocall.domain.model.Room;
import reactor.core.publisher.Mono;

public interface DeleteRoom {
    Mono<Void> delete(String roomId);
}
