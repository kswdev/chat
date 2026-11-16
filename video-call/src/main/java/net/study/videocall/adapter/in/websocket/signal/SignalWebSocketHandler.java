package net.study.videocall.adapter.in.websocket.signal;

import lombok.RequiredArgsConstructor;
import net.study.videocall.adapter.in.security.PeerIdentityResolver;
import net.study.videocall.adapter.out.websocket.SignalOutbox;
import net.study.videocall.application.port.in.EnterRoomUseCase;
import net.study.videocall.application.port.in.QuitRoomUseCase;
import net.study.videocall.application.port.in.RelaySignalUseCase;
import net.study.videocall.application.port.out.PeerConnections;
import net.study.videocall.domain.model.Peer;
import net.study.videocall.util.JsonUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class SignalWebSocketHandler implements WebSocketHandler {

    private static final String PATH_PREFIX = "/ws/v1/video-call/";

    private final JsonUtil jsonUtil;
    private final PeerIdentityResolver peerIdentityResolver;
    private final EnterRoomUseCase enterRoom;
    private final QuitRoomUseCase quitRoom;
    private final RelaySignalUseCase relaySignal;
    private final PeerConnections peerConnections;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return Mono.fromCallable(() -> resolve(session))
                .flatMap(context -> run(session, context));
    }

    private Context resolve(WebSocketSession session) {
        String roomId = extractRoomId(session.getHandshakeInfo().getUri());
        Peer peer = peerIdentityResolver.resolve(session.getHandshakeInfo().getHeaders());
        return new Context(roomId, peer);
    }

    private Mono<Void> run(WebSocketSession session, Context context) {
        SignalOutbox outbox = new SignalOutbox();

        Flux<WebSocketMessage> output = outbox.outbound().map(session::textMessage);

        Mono<Void> input = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(text -> Mono.justOrEmpty(jsonUtil.fromJson(text, SignalCommand.class)))
                .flatMap(command -> relaySignal.relay(context.roomId(), context.peer(), command.type(), command.payload()))
                .doFinally(signal -> {
                    outbox.close();
                    leave(context).subscribe();
                })
                .then();

        return join(context, outbox::send)
                .then(session.send(output).and(input));
    }

    Mono<Void> join(Context context, Consumer<String> sink) {
        return peerConnections.register(context.peer().getId(), sink)
                .then(enterRoom.enter(context.roomId(), context.peer())
                        .onErrorResume(e -> peerConnections.remove(context.peer().getId())
                                .then(Mono.error(e))
                        )
                )
                .then();
    }

    Mono<Void> leave(Context context) {
        return peerConnections.remove(context.peer().getId())
                .then(quitRoom.quit(context.roomId(), context.peer()))
                .onErrorResume(e -> Mono.empty())
                .then();
    }

    static String extractRoomId(URI uri) {
        String path = uri.getPath();
        if (!path.startsWith(PATH_PREFIX)) {
            throw new IllegalArgumentException("roomId가 URI에 없습니다: " + uri);
        }

        String roomId = path.substring(PATH_PREFIX.length());
        if (roomId.endsWith("/")) {
            roomId = roomId.substring(0, roomId.length() - 1);
        }
        if (roomId.isBlank()) {
            throw new IllegalArgumentException("roomId가 URI에 없습니다: " + uri);
        }

        return roomId;
    }

    record Context(String roomId, Peer peer) {}
}
