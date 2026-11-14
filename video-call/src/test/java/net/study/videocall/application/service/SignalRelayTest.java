package net.study.videocall.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.study.videocall.application.port.out.LoadRoom;
import net.study.videocall.application.port.out.PeerConnections;
import net.study.videocall.domain.model.Peer;
import net.study.videocall.domain.model.Room;
import net.study.videocall.domain.model.Signal;
import net.study.videocall.domain.model.SignalType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SignalRelayTest {

    @InjectMocks
    private SignalRelay signalRelay;

    @Mock
    private LoadRoom loadRoom;

    @Mock
    private PeerConnections peerConnections;

    private final ObjectMapper om = new ObjectMapper();

    @DisplayName("자기를 제외하고 방 참여자에게 시그널을 전달한다.")
    @Test
    void when_relay_signal_then_signal_relay_room_participants_except_sender() {
        // given
        String roomId = "roomId";
        Peer sender = Peer.create(1L, "sender");
        Peer other = Peer.create(2L, "other");
        SignalType signalType = SignalType.OFFER;
        ObjectNode payload = om.createObjectNode()
                .put("type", "OFFER")
                .put("sdp", "v=0");

        Room room = Room.create(roomId, "test-room-name", sender);
        room.join(other);

        given(loadRoom.loadById(roomId)).willReturn(Mono.just(room));
        given(peerConnections.send(any(), any())).willReturn(Mono.empty());

        // when
        Mono<Void> result = signalRelay.relay(roomId, sender, signalType, payload);

        // then
        StepVerifier.create(result).verifyComplete();

        then(peerConnections).should()
                .send(eq(other.getId()), eq(new Signal(sender.getId(), signalType, payload)));
        then(peerConnections).should(never())
                .send(eq(sender.getId()), any());
    }

    @DisplayName("혼자 있는 방에서는 전달할 상대가 없으므로 시그널을 전송하지 않는다.")
    @Test
    void when_relay_signal_in_solo_room_then_no_signal_sent() {
        // given
        String roomId = "roomId";
        Peer sender = Peer.create(1L, "sender");
        Room room = Room.create(roomId, "test-room-name", sender);

        given(loadRoom.loadById(roomId)).willReturn(Mono.just(room));

        // when
        Mono<Void> result = signalRelay.relay(roomId, sender, SignalType.OFFER, om.createObjectNode());

        // then
        StepVerifier.create(result).verifyComplete();

        then(peerConnections).should(never()).send(any(), any());
    }

    @DisplayName("방이 존재하지 않으면 에러 없이 완료된다.")
    @Test
    void when_relay_signal_to_non_existent_room_then_complete_without_error() {
        // given
        String roomId = "deleted-room-id";
        Peer sender = Peer.create(1L, "sender");

        given(loadRoom.loadById(roomId)).willReturn(Mono.empty());

        // when
        Mono<Void> result = signalRelay.relay(roomId, sender, SignalType.CANDIDATE, om.createObjectNode());

        // then
        StepVerifier.create(result).verifyComplete();

        then(peerConnections).should(never()).send(any(), any());
    }
}
