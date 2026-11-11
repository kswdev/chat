package net.study.videocall.adapter.out.websocket;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;

public class SignalOutbox {

    private final Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

    public void send(String message) {
        sink.emitNext(message, Sinks.EmitFailureHandler.busyLooping(Duration.ofMillis(100)));
    }

    public Flux<String> outbound() {
        return sink.asFlux();
    }

    public void close() {
        sink.tryEmitComplete();
    }
}
