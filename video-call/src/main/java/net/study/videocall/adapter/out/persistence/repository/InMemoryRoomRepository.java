package net.study.videocall.adapter.out.persistence.repository;

import net.study.videocall.domain.model.Room;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRoomRepository implements RoomRepository {

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    @Override
    public Mono<Room> save(Room room) {
        return Mono.fromCallable(() -> {
            rooms.put(room.getId(), room);
            return room;
        });
    }

    @Override
    public Mono<Room> loadById(String roomId) {
        return Mono.fromCallable(() -> rooms.get(roomId));
    }

    @Override
    public Mono<Void> delete(String roomId) {
        return Mono.fromRunnable(() -> rooms.remove(roomId));
    }
}
