package net.study.messageconnectionflux.auth

import net.study.messagecommon.constant.IdKey
import org.springframework.http.HttpHeaders
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono;
import spock.lang.Specification

class WebSocketMessageFilterSpec extends Specification {
    def filter = new WebSocketMessageFilter()

    def "Authorization 없으면 에러 발생"() {
        given:
        def request = MockServerHttpRequest.get("/ws/v1/message")
                .header(HttpHeaders.UPGRADE, "websocket")
                .build()

        def exchange = MockServerWebExchange.from(request)
        def chain = Mock(WebFilterChain)

        when:
        filter.filter(exchange, chain).block()

        then:
        def e = thrown(RuntimeException)
        e.message == "인증 실패"

        0 * chain.filter(_)
    }

    def "정상 요청이면 attribute 저장 후 chain 실행"() {
        given:
        def request = MockServerHttpRequest.get("/ws/v1/message")
                .header(HttpHeaders.UPGRADE, "websocket")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .header("X-Authorization-Id", "123")
                .build()

        def exchange = MockServerWebExchange.from(request)

        def chain = Mock(WebFilterChain) {
            1 * filter(_ as MockServerWebExchange) >> Mono.empty()
        }

        when:
        filter.filter(exchange, chain).block()

        then:
        exchange.attributes.get(IdKey.USER_ID.getValue()) == Optional.of(123L)
    }

    def "WebSocket 요청 아니면 그냥 통과"() {
        given:
        def request = MockServerHttpRequest.get("/api/v1/message")
                .build()

        def exchange = MockServerWebExchange.from(request)

        def chain = Mock(WebFilterChain) {
            1 * filter(_ as MockServerWebExchange) >> Mono.empty()
        }

        when:
        filter.filter(exchange, chain).block()

        then:
        noExceptionThrown()
    }

    def "경로가 /ws/ 아니면 그냥 통과"() {
        given:
        def request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.UPGRADE, "websocket")
                .build()

        def exchange = MockServerWebExchange.from(request)

        def chain = Mock(WebFilterChain) {
            1 * filter(_ as MockServerWebExchange) >> Mono.empty()
        }

        when:
        filter.filter(exchange, chain).block()

        then:
        noExceptionThrown()
    }
}
