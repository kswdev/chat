package net.study.videocall.service;

import net.study.videocall.application.port.out.RoomIdGenerator;
import net.study.videocall.application.port.out.SaveRoom;
import net.study.videocall.application.service.RoomManager;
import net.study.videocall.domain.model.Peer;
import net.study.videocall.domain.model.Room;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.answer;
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

    @DisplayName("방을 만들면 방을 저장하고 방을 반환한다.")
    @Test
    void when_create_room_then_save_room_and_return_id() {
        // given
        String roomId = "generated-id";
        String roomName = "test-room-name";
        Peer creator = createPeer(0L, "creator");

        given(generator.create()).willReturn(roomId);
        given(saveRoom.save(any())).willAnswer(answer(Mono::just));

        // when
        Mono<Room> result = manager.create(roomName, creator);

        // then
        StepVerifier.create(result)
                .assertNext(room -> {
                    assertThat(room.getId()).isEqualTo(roomId);
                    assertThat(room.getName()).isEqualTo(roomName);
                })
                .verifyComplete();

        then(saveRoom).should().save(any(Room.class));
        then(saveRoom).shouldHaveNoMoreInteractions();
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

    @DisplayName("방을 만든 사람은 첫 참여자가 된다.")
    @Test
    void when_create_room_then_creator_should_be_participant() {
        // given
        String roomName = "test-room-name";
        Peer creator = createPeer(1L, "test=user");

        // when
        Mono<Room> result = manager.create(roomName, creator);

        // then
    }

    private Peer createPeer(Long id, String name) {
        return Peer.create(id, name);
    }
}