package net.study.messageconnectionflux.handler.websocket

import net.study.messagecommon.constant.IdKey
import net.study.messageconnectionflux.domain.user.UserId
import net.study.messageconnectionflux.dto.websocket.inbound.AcceptRequest
import net.study.messageconnectionflux.dto.websocket.inbound.BaseRequest
import net.study.messageconnectionflux.handler.request.RequestDispatcher
import net.study.messageconnectionflux.util.JsonUtil
import org.springframework.web.reactive.socket.HandshakeInfo
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Specification

class MessageWebSocketHandlerSpec extends Specification {

    private MessageWebSocketHandler handler
    private RequestDispatcher dispatcher = Mock()
    private JsonUtil jsonUtil = Mock()
    private WebSocketSession session = Stub()

    def setup() {
        handler = new MessageWebSocketHandler(jsonUtil, dispatcher)
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
        handler.handle(session).block()

        then:
        1 * jsonUtil.fromJson(payload, BaseRequest.class) >> Mono.just(request)
        1 * dispatcher.dispatch(session, request) >> Mono.empty()
    }
}
