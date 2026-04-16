package net.study.messageconnectionflux.handler.request

import net.study.messagecommon.constant.IdKey
import net.study.messageconnectionflux.domain.user.UserId
import net.study.messageconnectionflux.dto.kafka.AcceptRequestRecord
import net.study.messageconnectionflux.dto.websocket.inbound.AcceptRequest
import net.study.messageconnectionflux.kafka.KafkaProducer
import net.study.messageconnectionflux.service.ClientNotificationService
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import spock.lang.Specification

class AcceptRequestHandlerSpec extends Specification {

    private KafkaProducer kafkaProducer = Mock()
    private ClientNotificationService notificationService = Stub()
    private AcceptRequestHandler acceptRequestHandler

    private WebSocketSession senderSession = Mock()

    def setup() {
        acceptRequestHandler = new AcceptRequestHandler(kafkaProducer, notificationService)
    }

    def "친구 요청 수락 완료 처리."() {
        given:
        UserId accepterUserId = new UserId(1L)
        String username = "test"
        AcceptRequest request = new AcceptRequest(username)

        when:
        acceptRequestHandler.handleRequest(senderSession, request)

        then:
        1 * senderSession.getAttributes() >> Map.of(IdKey.USER_ID.getValue(), accepterUserId)
        1 * kafkaProducer.sendRequest(new AcceptRequestRecord(accepterUserId, username), _ as Runnable) >> Mono.empty()
    }
}
