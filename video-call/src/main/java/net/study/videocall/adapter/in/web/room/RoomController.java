package net.study.videocall.adapter.in.web.room;

import lombok.RequiredArgsConstructor;
import net.study.videocall.adapter.in.security.PeerIdentityResolver;
import net.study.videocall.adapter.out.web.room.Created;
import net.study.videocall.adapter.out.web.room.RoomSummary;
import net.study.videocall.application.port.in.CreateRoomUseCase;
import net.study.videocall.application.port.in.ListRoomsUseCase;
import net.study.videocall.domain.model.Peer;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/video-call/room")
public class RoomController {

    private final CreateRoomUseCase createRoom;
    private final ListRoomsUseCase listRooms;
    private final PeerIdentityResolver peerIdentityResolver;

    @PostMapping
    public Mono<ResponseEntity<Created>> create(@RequestBody CreateRoomCommand command, ServerHttpRequest request) {
        Peer creator = peerIdentityResolver.resolve(request.getHeaders());

        return createRoom.create(command.name(), creator)
                .map(room -> ResponseEntity.ok(new Created(room.getId())));
    }

    @GetMapping
    public Flux<RoomSummary> list() {
        return listRooms.list().map(RoomSummary::from);
    }
}
