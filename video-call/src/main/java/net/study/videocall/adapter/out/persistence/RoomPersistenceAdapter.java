package net.study.videocall.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import net.study.videocall.adapter.out.persistence.repository.RoomRepository;
import net.study.videocall.application.port.out.DeleteRoom;
import net.study.videocall.application.port.out.LoadRoom;
import net.study.videocall.application.port.out.SaveRoom;
import net.study.videocall.domain.model.Room;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RoomPersistenceAdapter implements
        LoadRoom,
        SaveRoom,
        DeleteRoom
{

    private final RoomRepository roomRepository;

    @Override
    public Mono<Void> delete(String roomId) {
        return roomRepository.delete(roomId);
    }

    @Override
    public Mono<Room> loadById(String id) {
        return roomRepository.loadById(id);
    }

    @Override
    public Mono<Room> save(Room room) {
        return roomRepository.save(room);
    }
}
