package net.study.messagesocial.adapter.in.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import net.study.messagecommon.constant.IdKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpRequestFilterTest {

    @Mock
    private FilterChain chain;

    private HttpRequestFilter requestFilter;

    @BeforeEach
    void setup() {
        requestFilter = new HttpRequestFilter();
    }

    @Test
    void should_pass_when_required_header_is_valid() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/social");
        HttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(IdKey.USER_ID.getValue(), "testUser");

        // when
        requestFilter.doFilter(request, response, chain);

        // then
        verify(chain, times(1))
                .doFilter(request, response);
    }

    @Test
    void should_not_pass_when_required_header_is_missing() throws ServletException, IOException {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/social");
        HttpServletResponse response = new MockHttpServletResponse();

        //when
        requestFilter.doFilter(request, response, chain);

        //then
        assertThat(response.getStatus()).isEqualTo(400);
        verifyNoInteractions(chain);
    }
}
