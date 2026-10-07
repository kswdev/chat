package net.study.videocall.application.service;

import lombok.RequiredArgsConstructor;
import net.study.videocall.application.port.in.CreateRoomUseCase;
import net.study.videocall.application.port.in.EnterRoomUseCase;
import net.study.videocall.application.port.out.RoomIdGenerator;
import net.study.videocall.application.port.out.SaveRoom;
import net.study.videocall.domain.Peer;
import net.study.videocall.domain.Room;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RoomManager implements
        CreateRoomUseCase,
        EnterRoomUseCase {

    private final RoomIdGenerator generator;
    private final SaveRoom saveRoom;

    public Mono<String> create(String roomName) {
        Room room = Room.create(generator.create(), roomName);
        return saveRoom.save(room);
    }

    public Mono<Boolean> enter(String roomId, Peer alice) {
        return Mono.just(true);
    }
}
