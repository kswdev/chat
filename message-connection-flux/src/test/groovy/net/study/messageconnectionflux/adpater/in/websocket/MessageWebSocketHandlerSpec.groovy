package net.study.messageconnectionflux.adpater.in.websocket

import net.study.messagecommon.constant.IdKey
import net.study.messageconnectionflux.adpter.in.websocket.MessageWebSocketHandler
import net.study.messageconnectionflux.domain.user.UserId
import net.study.messageconnectionflux.application.dto.websocket.inbound.AcceptRequest
import net.study.messageconnectionflux.application.dto.websocket.inbound.BaseRequest
import net.study.messageconnectionflux.adpter.in.websocket.request.RequestDispatcher
import net.study.messageconnectionflux.application.port.in.SessionService
import net.study.messageconnectionflux.adpter.out.persistence.redis.WebSocketSessionManager
import net.study.messageconnectionflux.util.JsonUtil
import org.reactivestreams.Publisher
import org.springframework.web.reactive.socket.HandshakeInfo
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.test.StepVerifier
import spock.lang.Specification

import java.time.Duration

class MessageWebSocketHandlerSpec extends Specification {

    private JsonUtil jsonUtil = Mock()
    private RequestDispatcher dispatcher = Mock()

    private WebSocketSession session = Stub()
    private SessionService cacheService = Mock()

    private MessageWebSocketHandler handler
    private WebSocketSessionManager sessionManager

    def setup() {
        sessionManager = new WebSocketSessionManager()
        handler = new MessageWebSocketHandler(jsonUtil, dispatcher, sessionManager, cacheService)
        def handshakeInfo = Mock(HandshakeInfo)
        def attributes = Map.of(IdKey.USER_ID.getValue(), new UserId(0L))

        handshakeInfo.getAttributes() >> attributes
        session.getHandshakeInfo() >> handshakeInfo
    }

    def "메시지 수신 → 파싱 → dispatcher 호출"() {
        given:
        def payload = '{"type":"ACCEPT_REQUEST","username":"test1"}'
        def message = Stub(WebSocketMessage) {
            getPayloadAsText() >> payload
        }

        session.receive() >> Flux.just(message)

        BaseRequest request = new AcceptRequest("test1")

        when:
        handler.handle(session)
                .take(Duration.ofMillis(100)) // 🔥 무한 스트림 방지
                .block()

        then:
        1 * jsonUtil.fromJson(payload, BaseRequest.class) >> Mono.just(request)
        1 * dispatcher.dispatch(session, request) >> Mono.empty()
    }

    def "메시지 발신 → WebSocket 전송 (StepVerifier)"() {
        given:
        def userId = new UserId(0L)
        def sinkCaptor = Sinks.many().unicast().onBackpressureBuffer()

        session.receive() >> Flux.never()

        session.textMessage(_ as String) >> { String text ->
            Stub(WebSocketMessage) {
                getPayloadAsText() >> text
            }
        }

        session.send(_ as Publisher) >> { Publisher<WebSocketMessage> publisher ->
            Flux.from(publisher)
                    .doOnNext { msg -> sinkCaptor.tryEmitNext(msg.getPayloadAsText()) }
                    .then()
        }

        when:
        def result = handler.handle(session)

        then:
        StepVerifier
                .create(sinkCaptor.asFlux())
                .then {
                    result.subscribe()
                    sessionManager.sendMessage(userId, "hello")
                }
                .expectNext("hello")
                .thenCancel()
                .verify()
    }
}
