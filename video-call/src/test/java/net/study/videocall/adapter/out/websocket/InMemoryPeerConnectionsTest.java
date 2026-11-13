package net.study.videocall.adapter.out.websocket;


import net.study.videocall.domain.model.Signal;
import net.study.videocall.domain.model.SignalType;
import net.study.videocall.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.*;

class InMemoryPeerConnectionsTest {

    private JsonUtil jsonUtil;
    private InMemoryPeerConnections connections;

    private static final Long ALICE = 1L;
    private static final Long BOB = 2L;

    @BeforeEach
    void setUp() {
        jsonUtil = new JsonUtil();
        connections = new InMemoryPeerConnections(jsonUtil);
    }

    private List<String> fakeConnection(Long peerId) {
        List<String> received = new CopyOnWriteArrayList<>();
        connections.register(peerId, received::add).block();
        return received;
    }

    private Signal offerFrom(Long senderId) {
        return new Signal(
                senderId, SignalType.OFFER,
                """
                {"type":"OFFER","sdp":"v=0"}"""
        );
    }

    @Nested
    @DisplayName("연결 상태")
    class ConnectionState {

        @DisplayName("등록하면 연결된 상태다.")
        @Test
        void when_register_then_connected() {
            fakeConnection(ALICE);

            StepVerifier.create(connections.isConnected(ALICE))
                    .expectNext(true)
                    .verifyComplete();
        }

        @DisplayName("등록하지 않은 사람은 연결되지 않은 상태다.")
        @Test
        void when_not_registered_then_not_connected() {
            StepVerifier.create(connections.isConnected(ALICE))
                    .expectNext(false)
                    .verifyComplete();
        }

        @DisplayName("해제하면 연결되지 않은 상태다.")
        @Test
        void when_remove_then_not_connected() {
            fakeConnection(ALICE);

            connections.remove(ALICE).block();

            StepVerifier.create(connections.isConnected(ALICE))
                    .expectNext(false)
                    .verifyComplete();
        }

        @DisplayName("등록하지 않은 사람을 해제해도 에러가 나지 않는다.")
        @Test
        void when_remove_unregistered_then_no_error() {
            StepVerifier.create(connections.remove(ALICE))
                    .verifyComplete();
        }

        @DisplayName("구독하지 않으면 등록되지 않는다.")
        @Test
        void when_register_not_subscribed_then_not_registered() {
            connections.register(ALICE, message -> { });   // 구독 없음

            StepVerifier.create(connections.isConnected(ALICE))
                    .expectNext(false)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("보내기")
    class Send {

        @DisplayName("등록된 연결로 중계 시그널 JSON 이 전달된다.")
        @Test
        void when_send_then_consumer_receives_json() {
            List<String> bobReceived = fakeConnection(BOB);

            connections.send(BOB, offerFrom(ALICE)).block();

            assertThat(bobReceived).hasSize(1);
            assertThat(bobReceived.getFirst())
                    .isEqualTo("""
                            {"type":"OFFER","sdp":"v=0"}""");
        }

        @DisplayName("다른 사람의 연결로는 전달되지 않는다.")
        @Test
        void when_send_to_bob_then_alice_receives_nothing() {
            List<String> aliceReceived = fakeConnection(ALICE);
            fakeConnection(BOB);

            connections.send(BOB, offerFrom(ALICE)).block();

            assertThat(aliceReceived).isEmpty();
        }

        @DisplayName("등록하지 않은 사람에게 보내면 에러 없이 무시된다.")
        @Test
        void when_send_to_unregistered_then_ignored() {
            StepVerifier.create(connections.send(BOB, offerFrom(ALICE)))
                    .verifyComplete();
        }

        @DisplayName("해제된 사람에게 보내면 전달되지 않는다.")
        @Test
        void when_send_to_removed_then_not_delivered() {
            List<String> bobReceived = fakeConnection(BOB);
            connections.remove(BOB).block();

            StepVerifier.create(connections.send(BOB, offerFrom(ALICE)))
                    .verifyComplete();
            assertThat(bobReceived).isEmpty();
        }

        @DisplayName("구독하지 않으면 보내지 않는다.")
        @Test
        void when_send_not_subscribed_then_not_delivered() {
            List<String> bobReceived = fakeConnection(BOB);

            connections.send(BOB, offerFrom(ALICE));

            assertThat(bobReceived).isEmpty();
        }
    }
}