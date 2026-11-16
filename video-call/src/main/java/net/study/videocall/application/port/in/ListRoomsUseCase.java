package net.study.videocall.application.port.in;

import net.study.videocall.domain.model.Room;
import reactor.core.publisher.Flux;

public interface ListRoomsUseCase {
    Flux<Room> list();
}
