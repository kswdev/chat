package net.study.videocall.adapter.out.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThatCode;


class SignalOutboxTest {

    private SignalOutbox outbox;

    @BeforeEach
    void setUp() {
        outbox = new SignalOutbox();
    }

    @DisplayName("상대가 보낸 offer 가 내 연결로 전달된다.")
    @Test
    void when_peer_sends_offer_then_i_receive_it() {
        StepVerifier.create(outbox.outbound())
                .then(() -> outbox.send("offer"))
                .expectNext("offer")
                .thenCancel()
                .verify();
    }

    @DisplayName("WebSocket 이 구독을 시작하기 전에 보낸 joined 도 전달된다.")
    @Test
    void when_joined_sent_before_subscribe_then_not_lost() {
        outbox.send("joined");

        StepVerifier.create(outbox.outbound())
                .expectNext("joined")
                .thenCancel()
                .verify();
    }

    @DisplayName("offer 와 candidate 는 보낸 순서대로 전달된다.")
    @Test
    void when_offer_then_candidates_sent_then_order_is_kept() {
        StepVerifier.create(outbox.outbound())
                .then(() -> {
                    outbox.send("offer");
                    outbox.send("candidate-1");
                    outbox.send("candidate-2");
                    outbox.send("candidate-3");
                })
                .expectNext("offer", "candidate-1", "candidate-2", "candidate-3")
                .thenCancel()
                .verify();
    }

    @DisplayName("통화를 종료하면 보내던 메시지까지 전달한 뒤 흐름이 끝난다.")
    @Test
    void when_close_then_flow_completes_after_pending_messages() {
        outbox.send("peer-left");
        outbox.close();

        StepVerifier.create(outbox.outbound())
                .expectNext("peer-left")
                .verifyComplete();
    }

    @DisplayName("이미 나간 사람에게 candidate 가 중계되어도 예외가 나지 않는다.")
    @Test
    void when_send_after_close_then_ignored_without_error() {
        outbox.close();

        assertThatCode(() -> outbox.send("late-candidate"))
                .doesNotThrowAnyException();

        StepVerifier.create(outbox.outbound())
                .verifyComplete();
    }

    private static final int SENDERS = 10;
    private static final int CANDIDATES_PER_SENDER = 1_000;

    @DisplayName("여러 스레드가 동시에 candidate 를 보내도 하나도 유실되지 않는다.")
    @RepeatedTest(20)
    void when_candidates_sent_concurrently_then_none_lost() {
        StepVerifier.create(outbox.outbound())
                .then(this::sendCandidatesConcurrentlyThenClose)
                .expectNextCount(SENDERS * CANDIDATES_PER_SENDER)
                .expectComplete()
                .verify(Duration.ofSeconds(10));
    }

    private void sendCandidatesConcurrentlyThenClose() {
        ExecutorService executor = Executors.newFixedThreadPool(SENDERS);
        CountDownLatch ready = new CountDownLatch(SENDERS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        for (int s = 0; s < SENDERS; s++) {
            int sender = s;
            futures.add(executor.submit(() -> {
                ready.countDown();
                start.await();
                for (int i = 0; i < CANDIDATES_PER_SENDER; i++) {
                    outbox.send("candidate-" + sender + "-" + i);
                }
                return null;
            }));
        }

        try {
            ready.await();
            start.countDown();
            for (Future<?> f : futures) {
                f.get(5, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }
        outbox.close();
    }
}