package net.study.videocall.adapter.out.persistence.repository;

import net.study.videocall.domain.model.Room;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RoomRepository {
    Mono<Room> loadById(String roomId);
    Mono<Room> save(Room room);
    Mono<Void> delete(String roomId);
    Flux<Room> findAll();
}
