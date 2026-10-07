package net.study.videocall.application.port.in;

import reactor.core.publisher.Mono;

public interface CreateRoomUseCase {
    Mono<String> create(String roomName);
}
