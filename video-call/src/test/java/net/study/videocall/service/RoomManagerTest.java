package net.study.videocall.service;

import net.study.videocall.application.port.out.DeleteRoom;
import net.study.videocall.application.port.out.LoadRoom;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Mock
    LoadRoom loadRoom;

    @Mock
    DeleteRoom deleteRoom;

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

        then(saveRoom).should().save(any());
    }

    @DisplayName("방 아이디로 방에 입장할 수 있다.")
    @Test
    void when_enter_room_then_enter_success() {
        // given
        Peer creator = createPeer(0L, "creator");
        Peer alice = createPeer(1L, "alice");
        String roomId = "test-id";
        Room expect = Room.create(roomId, "test-name", creator);
        given(loadRoom.loadById(roomId)).willReturn(Mono.just(expect));
        given(saveRoom.save(any())).willAnswer(answer(Mono::just));

        // when
        Mono<Room> joined = manager.enter(roomId, alice);

        // then
        StepVerifier.create(joined)
                .assertNext(room -> assertTrue(room.isJoined(alice)))
                .verifyComplete();

        then(loadRoom).should().loadById(roomId);
        then(saveRoom).should().save(any());
    }

    @DisplayName("방 아이디로 방에서 퇴장할 수 있다.")
    @Test
    void when_quit_room_then_quit_success() {
        // given
        Peer creator = createPeer(0L, "creator");
        Peer alice = createPeer(1L, "alice");
        String roomId = "test-id";
        Room created = Room.create(roomId, "test-name", creator);
        created.join(alice);

        given(loadRoom.loadById(roomId)).willReturn(Mono.just(created));
        given(saveRoom.save(any())).willAnswer(answer(Mono::just));

        // when
        Mono<Room> joined = manager.quit(roomId, alice);

        // then
        StepVerifier.create(joined)
                .assertNext(room -> assertFalse(room.isJoined(alice)))
                .verifyComplete();

        then(loadRoom).should().loadById(roomId);
        then(saveRoom).should().save(any());
    }

    @DisplayName("마지막 사용자가 방에서 퇴장하면 사용자는 퇴장에 성공하고 방은 삭제된다..")
    @Test
    void when_a_last_peer_quit_room_then_quit_success_and_room_is_deleted() {
        // given
        Peer creator = createPeer(0L, "creator");
        String roomId = "test-id";
        Room created = Room.create(roomId, "test-name", creator);

        given(loadRoom.loadById(roomId)).willReturn(Mono.just(created));
        given(deleteRoom.delete(roomId)).willReturn(Mono.empty());
        // when
        Mono<Room> joined = manager.quit(roomId, creator);

        // then
        StepVerifier.create(joined)
                .assertNext(room -> assertFalse(room.isJoined(creator)))
                .verifyComplete();

        then(loadRoom).should().loadById(roomId);
        then(deleteRoom).should().delete(any());
    }

    private Peer createPeer(Long id, String name) {
        return Peer.create(id, name);
    }
}