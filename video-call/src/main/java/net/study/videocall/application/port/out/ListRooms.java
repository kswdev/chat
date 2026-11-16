package net.study.videocall.application.port.out;

import net.study.videocall.domain.model.Room;
import reactor.core.publisher.Flux;

public interface ListRooms {
    Flux<Room> list();
}
