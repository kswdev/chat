package net.study.messageconnectionflux.adpater.in.websocket.request

import net.study.messagecommon.constant.IdKey
import net.study.messageconnectionflux.adpter.in.websocket.request.AcceptRequestHandler
import net.study.messageconnectionflux.application.port.out.EventProducer
import net.study.messageconnectionflux.domain.user.UserId
import net.study.messageconnectionflux.application.dto.kafka.AcceptRequestRecord
import net.study.messageconnectionflux.application.dto.websocket.inbound.AcceptRequest
import net.study.messageconnectionflux.application.port.out.ClientNotificationService
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import spock.lang.Specification

class AcceptRequestHandlerSpec extends Specification {

    private EventProducer eventProducer = Mock()
    private ClientNotificationService notificationService = Stub()
    private AcceptRequestHandler acceptRequestHandler

    private WebSocketSession senderSession = Mock()

    def setup() {
        acceptRequestHandler = new AcceptRequestHandler(eventProducer, notificationService)
    }

    def "친구 요청 수락 완료 처리."() {
        given:
        UserId accepterUserId = new UserId(1L)
        String username = "test"
        AcceptRequest request = new AcceptRequest(username)

        when:
        acceptRequestHandler.handleRequest(senderSession, request).block()

        then:
        1 * senderSession.getAttributes() >> Map.of(IdKey.USER_ID.getValue(), accepterUserId)
        1 * eventProducer.sendRequest(new AcceptRequestRecord(accepterUserId, username), _ as Runnable) >> Mono.empty()
    }
}
