package net.study.videocall.adapter.in.web.room;

import lombok.RequiredArgsConstructor;
import net.study.messagecommon.constant.IdKey;
import net.study.videocall.adapter.out.web.room.Created;
import net.study.videocall.application.port.in.CreateRoomUseCase;
import net.study.videocall.domain.model.Peer;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/video/room")
public class RoomController {

    private final CreateRoomUseCase createRoom;

    @PostMapping
    public Mono<ResponseEntity<Created>> create(@RequestBody CreateRoomCommand command, ServerHttpRequest request) {
        Long userId = Long.valueOf(request.getHeaders().getFirst(IdKey.USER_ID.getValue()));
        String username = request.getHeaders().getFirst(IdKey.USERNAME.getValue());
        Peer creator = Peer.create(userId, username);

        return createRoom.create(command.name(), creator)
                .map(room -> ResponseEntity.ok(new Created(room.getId())));
    }
}
