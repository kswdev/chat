package net.study.videocall.adapter.in.web.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AuthHeaderWebFilterTest {

    @Mock
    WebFilterChain chain;

    AuthHeaderWebFilter filter = new AuthHeaderWebFilter();

    @DisplayName("USER_ID 헤더가 없으면 401을 반환하고 체인을 호출하지 않는다.")
    @Test
    void when_user_id_header_missing_then_reject() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/video/create"));

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        then(chain).should(never()).filter(any());
    }

    @DisplayName("USER_ID 헤더가 숫자가 아니면 401을 반환하고 체인을 호출하지 않는다.")
    @Test
    void when_user_id_header_not_numeric_then_reject() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/video/create")
                        .header("USER_ID", "abc"));

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        then(chain).should(never()).filter(any());
    }

    @DisplayName("USER_ID 헤더가 유효한 숫자이면 체인을 호출한다.")
    @Test
    void when_user_id_header_numeric_then_pass_through() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/video/create")
                        .header("USER_ID", "1"));

        given(chain.filter(exchange)).willReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        then(chain).should().filter(exchange);
    }

    @DisplayName("video API 경로가 아니면 USER_ID 헤더 없이도 통과한다.")
    @Test
    void when_path_not_targeted_then_pass_through_without_header() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/health"));

        given(chain.filter(exchange)).willReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        then(chain).should().filter(exchange);
    }
}
