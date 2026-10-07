package net.study.videocall.service;

import net.study.videocall.application.port.out.RoomIdGenerator;
import net.study.videocall.application.port.out.SaveRoom;
import net.study.videocall.application.service.RoomManager;
import net.study.videocall.domain.Peer;
import net.study.videocall.domain.Room;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class RoomManagerTest {

    @Mock
    RoomIdGenerator generator;

    @Mock
    SaveRoom saveRoom;

    @InjectMocks
    RoomManager manager;

    @Captor
    ArgumentCaptor<Room> roomCaptor;

    @Test
    void when_create_room_then_save_room_and_return_id() {
        // given
        String roomName = "test-room-name";
        String generatedId = "generated-id";

        given(generator.create()).willReturn(generatedId);
        given(saveRoom.save(any())).willReturn(Mono.just(generatedId));

        // when
        Mono<String> result = manager.create(roomName);

        // then
        StepVerifier.create(result)
                .expectNext(generatedId)
                .verifyComplete();

        then(saveRoom).should().save(roomCaptor.capture());
        then(saveRoom).shouldHaveNoMoreInteractions();

        Room saved = roomCaptor.getValue();
        assertThat(saved.getId()).isEqualTo(generatedId);
        assertThat(saved.getName()).isEqualTo(roomName);
    }

    @DisplayName("방 아이디로 방에 입장할 수 있다.")
    @Test
    void when_enter_room_does_not_exists_then_create_room() {
        // given
        Peer alice = createPeer(1L, "alice");
        String roomId = "test-id";

        // when
        Mono<Boolean> joined = manager.enter(roomId, alice);

        // then
        StepVerifier.create(joined)
                .expectNext(true)
                .verifyComplete();
    }

    private Peer createPeer(Long id, String name) {
        return Peer.create(id, name);
    }
}