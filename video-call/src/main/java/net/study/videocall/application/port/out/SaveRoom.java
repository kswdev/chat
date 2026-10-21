package net.study.videocall.application.port.out;

import net.study.videocall.domain.model.Room;
import reactor.core.publisher.Mono;

public interface SaveRoom {
    Mono<String> save(Room room);
}
