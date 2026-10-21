package net.study.videocall.application.service;

import lombok.RequiredArgsConstructor;
import net.study.videocall.application.port.in.CreateRoomUseCase;
import net.study.videocall.application.port.in.EnterRoomUseCase;
import net.study.videocall.application.port.out.DeleteRoom;
import net.study.videocall.application.port.out.LoadRoom;
import net.study.videocall.application.port.out.RoomIdGenerator;
import net.study.videocall.application.port.out.SaveRoom;
import net.study.videocall.domain.model.Peer;
import net.study.videocall.domain.model.Room;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RoomManager implements
        CreateRoomUseCase,
        EnterRoomUseCase {

    private final RoomIdGenerator generator;
    private final SaveRoom saveRoom;
    private final LoadRoom loadRoom;
    private final DeleteRoom deleteRoom;

    public Mono<Room> create(String roomName, Peer creator) {
        Room room = Room.create(generator.create(), roomName, creator);
        return saveRoom.save(room);
    }

    public Mono<Room> enter(String roomId, Peer peer) {
        return loadRoom.loadById(roomId)
                .map(room -> room.join(peer))
                .flatMap(saveRoom::save);
    }

    public Mono<Room> quit(String roomId, Peer peer) {
        return loadRoom.loadById(roomId)
                .map(room -> room.quit(peer))
                .flatMap(room -> room.isEmpty()
                        ? deleteRoom.delete(room.getId()).thenReturn(room)
                        : saveRoom.save(room));
    }
}
