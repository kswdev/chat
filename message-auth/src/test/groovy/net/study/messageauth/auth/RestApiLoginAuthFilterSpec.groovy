package net.study.messageauth.auth

import net.study.messageauth.auth.token.TokenIssuer
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.web.util.matcher.RequestMatcher
import spock.lang.Specification

class RestApiLoginAuthFilterSpec extends Specification {

    private AuthenticationManager manager = Mock()
    private RequestMatcher matcher = Mock()
    private TokenIssuer issuer = Mock()
    private RestApiLoginAuthFilter filter

    def setup() {
        filter = new RestApiLoginAuthFilter(matcher, manager, issuer)
    }

    def "로그인을 성공하면 jwt 토큰을 반환한다."() {
        given:
        def request = new MockHttpServletRequest("POST", "/login")
        request.contentType = "application/json"
        request.content = '{"username":"user","password":"pass"}'.bytes

        def response = new MockHttpServletResponse()

        def auth = new UsernamePasswordAuthenticationToken("user", "pass")

        when:
        filter.doFilter(request, response, new MockFilterChain())

        then:
        1 * matcher.matches(_) >> true
        1 * manager.authenticate(_) >> auth
        1 * issuer.issue(_) >> 'any'
        response.status == 200
        response.getHeader("access-token") != null
    }
}
