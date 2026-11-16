package net.study.videocall.adapter.in.websocket.signal;

import net.study.videocall.application.port.in.EnterRoomUseCase;
import net.study.videocall.application.port.in.QuitRoomUseCase;
import net.study.videocall.application.port.out.PeerConnections;
import net.study.videocall.domain.model.Peer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class SignalWebSocketHandlerTest {

    @InjectMocks
    private SignalWebSocketHandler handler;

    @Mock
    private EnterRoomUseCase enterRoom;

    @Mock
    private QuitRoomUseCase quitRoom;


    @Mock
    private PeerConnections peerConnections;

    private final SignalWebSocketHandler.Context context =
            new SignalWebSocketHandler.Context("room-1", Peer.create(1L, "peer"));

    @DisplayName("URI 마지막 경로에서 roomId를 추출한다.")
    @Test
    void when_uri_has_room_id_then_extract() {
        URI uri = URI.create("/ws/v1/video-call/room-1");

        assertThat(SignalWebSocketHandler.extractRoomId(uri)).isEqualTo("room-1");
    }

    @DisplayName("경로 끝에 슬래시가 있어도 roomId를 추출한다.")
    @Test
    void when_uri_has_trailing_slash_then_extract() {
        URI uri = URI.create("/ws/v1/video-call/room-1/");

        assertThat(SignalWebSocketHandler.extractRoomId(uri)).isEqualTo("room-1");
    }

    @DisplayName("roomId 세그먼트가 없으면 예외를 던진다.")
    @Test
    void when_uri_has_no_room_id_then_throw() {
        URI uri = URI.create("/ws/v1/video-call/");

        assertThatThrownBy(() -> SignalWebSocketHandler.extractRoomId(uri))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("register와 enter가 모두 성공하면 register 호출 후 enter가 호출된다.")
    @Test
    void when_register_and_enter_succeed_then_register_happens_before_enter() {
        given(peerConnections.register(any(), any())).willReturn(Mono.empty());
        given(enterRoom.enter(any(), any())).willReturn(Mono.empty());

        StepVerifier.create(handler.join(context, s -> {})).verifyComplete();

        InOrder order = inOrder(peerConnections, enterRoom);
        order.verify(peerConnections).register(eq(context.peer().getId()), any());
        order.verify(enterRoom).enter(eq(context.roomId()), eq(context.peer()));
    }

    @DisplayName("enter가 실패하면 등록한 connection을 제거하고 에러를 전파한다.")
    @Test
    void when_enter_fails_then_remove_registered_connection_and_propagate_error() {
        IllegalStateException roomFull = new IllegalStateException("방이 가득 찼습니다.");
        given(peerConnections.register(any(), any())).willReturn(Mono.empty());
        given(enterRoom.enter(any(), any())).willReturn(Mono.error(roomFull));
        given(peerConnections.remove(any())).willReturn(Mono.empty());

        StepVerifier.create(handler.join(context, s -> {}))
                .expectErrorMatches(e -> e == roomFull)
                .verify();

        then(peerConnections).should().remove(eq(context.peer().getId()));
    }

    @DisplayName("정상적으로 connection을 제거하고 방에서 나간다.")
    @Test
    void when_leave_then_remove_connection_before_quit_room() {
        given(peerConnections.remove(any())).willReturn(Mono.empty());
        given(quitRoom.quit(any(), any())).willReturn(Mono.empty());

        StepVerifier.create(handler.leave(context)).verifyComplete();

        InOrder order = inOrder(peerConnections, quitRoom);
        order.verify(peerConnections).remove(context.peer().getId());
        order.verify(quitRoom).quit(context.roomId(), context.peer());
    }

    @DisplayName("quit이 실패해도(이미 삭제된 방 등) 에러 없이 완료된다.")
    @Test
    void when_quit_fails_then_complete_without_error() {
        given(peerConnections.remove(any())).willReturn(Mono.empty());
        given(quitRoom.quit(any(), any())).willReturn(Mono.error(new IllegalArgumentException("방 참여자가 아닙니다.")));

        StepVerifier.create(handler.leave(context)).verifyComplete();
    }
}
