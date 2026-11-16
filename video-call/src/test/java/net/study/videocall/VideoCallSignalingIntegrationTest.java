package net.study.videocall;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class VideoCallSignalingIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ReactorNettyWebSocketClient wsClient = new ReactorNettyWebSocketClient();

    record CreatedResponse(String roomId) {}

    record RoomSummaryResponse(String roomId, String name, int peerCount) {}

    @DisplayName("두 피어가 입장해 offer를 교환하고, 한쪽이 퇴장하면 방이 정리된다.")
    @Test
    void when_two_peers_signal_and_one_leaves_then_room_is_cleaned_up() throws Exception {

        // given: alice가 방을 만들고, alice와 bob이 각각 WS로 입장한다
        String roomId = createRoom(1L, "alice", "test-room");

        BlockingQueue<String> bobReceived = new LinkedBlockingQueue<>();
        AtomicReference<WebSocketSession> aliceSessionRef = new AtomicReference<>();
        AtomicReference<WebSocketSession> bobSessionRef = new AtomicReference<>();
        CountDownLatch bothConnected = new CountDownLatch(2);

        Disposable bobConnection = connect(roomId, 2L, "bob", session -> {
            bobSessionRef.set(session);
            bothConnected.countDown();
            return session.receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(bobReceived::offer)
                    .then();
        });

        Disposable aliceConnection = connect(roomId, 1L, "alice", session -> {
            aliceSessionRef.set(session);
            bothConnected.countDown();
            return session.receive().then();
        });

        assertThat(bothConnected.await(3, TimeUnit.SECONDS)).isTrue();

        // when: alice가 OFFER를 보낸다
        send(aliceSessionRef.get(), "OFFER", Map.of("sdp", "v=0"));

        // then: bob이 senderId=alice.id, type=OFFER로 수신한다
        String received = bobReceived.poll(3, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        JsonNode signal = objectMapper.readTree(received);
        assertThat(signal.get("senderId").asLong()).isEqualTo(1L);
        assertThat(signal.get("type").asText()).isEqualTo("OFFER");

        // when: alice가 퇴장한다 (bob은 아직 방에 남아있음)
        aliceSessionRef.get().close().block(Duration.ofSeconds(2));
        aliceConnection.dispose();

        // then: 방에서 alice가 빠져 peerCount가 1로 줄어든다(bob만 남음, 방은 삭제되지 않음)
        assertRoomPeerCountEventually(roomId, 1);

        // when: bob도 퇴장한다 (마지막 피어)
        bobSessionRef.get().close().block(Duration.ofSeconds(2));
        bobConnection.dispose();

        // then: 방에 아무도 남지 않아 방 자체가 삭제된다
        assertRoomEventuallyGone(roomId);
    }

    private String createRoom(long userId, String username, String name) {
        CreatedResponse response = webTestClient.post().uri("/api/v1/video-call/room")
                .header("USER_ID", String.valueOf(userId))
                .header("USERNAME", username)
                .bodyValue(Map.of("name", name))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CreatedResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        return response.roomId();
    }

    private Disposable connect(
            String roomId,
            long userId,
            String username,
            java.util.function.Function<WebSocketSession, Mono<Void>> handler) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("USER_ID", String.valueOf(userId));
        headers.add("USERNAME", username);
        URI uri = URI.create("ws://localhost:" + port + "/ws/v1/video-call/" + roomId);

        return wsClient.execute(uri, headers, handler::apply).subscribe();
    }

    private void send(WebSocketSession session, String type, Object payload) throws Exception {
        String json = objectMapper.writeValueAsString(Map.of("type", type, "payload", payload));
        session.send(Mono.just(session.textMessage(json))).block(Duration.ofSeconds(2));
    }

    private void assertRoomPeerCountEventually(String roomId, int expectedPeerCount) throws InterruptedException {
        for (int i = 0; i < 20; i++) {
            RoomSummaryResponse room = findRoom(roomId);
            if (room != null && room.peerCount() == expectedPeerCount) {
                return;
            }
            Thread.sleep(100);
        }
        fail("방의 peerCount가 " + expectedPeerCount + "로 바뀌지 않았습니다: " + roomId);
    }

    private RoomSummaryResponse findRoom(String roomId) {
        List<RoomSummaryResponse> rooms = webTestClient.get().uri("/api/v1/video-call/room")
                .header("USER_ID", "1")
                .header("USERNAME", "alice")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RoomSummaryResponse.class)
                .returnResult()
                .getResponseBody();

        if (rooms == null) {
            return null;
        }
        return rooms.stream().filter(r -> roomId.equals(r.roomId())).findFirst().orElse(null);
    }

    private void assertRoomEventuallyGone(String roomId) throws InterruptedException {
        for (int i = 0; i < 20; i++) {
            if (findRoom(roomId) == null) {
                return;
            }
            Thread.sleep(100);
        }
        fail("방이 정리되지 않았습니다: " + roomId);
    }
}
