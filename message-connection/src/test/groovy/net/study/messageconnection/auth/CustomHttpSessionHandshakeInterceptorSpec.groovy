package net.study.messageconnection.auth

import net.study.messagecommon.constant.IdKey
import net.study.messageconnection.domain.user.UserId
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.mock.web.MockHttpServletRequest
import net.study.messageconnection.handler.WebSocketHandler
import org.springframework.http.HttpStatus
import spock.lang.Specification

class CustomHttpSessionHandshakeInterceptorSpec extends Specification {

    def interceptor = new CustomHttpSessionHandshakeInterceptor()

    def "Gateway에서 넣어준 X-Authorization-Id 헤더를 읽어 userId로 저장한다"() {
        given:
        def servletRequest = new MockHttpServletRequest()
        servletRequest.addHeader("X-Authorization-Id", "1")

        def request = new ServletServerHttpRequest(servletRequest)
        def response = Mock(ServerHttpResponse)
        def wsHandler = Mock(WebSocketHandler)
        def attributes = new HashMap()

        when:
        def result = interceptor.beforeHandshake(request, response, wsHandler, attributes)

        then:
        result
        attributes.get(IdKey.USER_ID.getValue()) == new UserId(1L)
    }

    def "헤더가 없으면 handshake 실패한다"() {
        given:
        def servletRequest = new MockHttpServletRequest()

        def request = new ServletServerHttpRequest(servletRequest)
        def response = Mock(ServerHttpResponse)
        def wsHandler = Mock(WebSocketHandler)
        def attributes = new HashMap()

        when:
        def result = interceptor.beforeHandshake(request, response, wsHandler, attributes)

        then:
        !result
        1 * response.setStatusCode(HttpStatus.UNAUTHORIZED)
    }
}
