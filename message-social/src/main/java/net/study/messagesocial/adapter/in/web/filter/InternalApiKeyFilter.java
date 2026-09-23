package net.study.messagesocial.adapter.in.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.study.messagecommon.constant.IdKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * web-gateway를 거치지 않는 서비스 간 내부 전용 엔드포인트를 보호하기 위한 API 키 검증.
 * 대상 경로 외 요청은 그대로 통과시킨다.
 */
@Component
public class InternalApiKeyFilter implements Filter {

    private static final String TARGET_PATH = "/api/v1/social/friends/connections/accepted-count";

    @Value("${message-social.internal-api-key}")
    private String expectedApiKey;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (TARGET_PATH.equals(httpRequest.getRequestURI())) {
            String apiKey = httpRequest.getHeader(IdKey.INTERNAL_API_KEY.getValue());
            if (apiKey == null || !apiKey.equals(expectedApiKey)) {
                reject(httpResponse);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"Invalid or missing internal API key\"}");
    }
}
