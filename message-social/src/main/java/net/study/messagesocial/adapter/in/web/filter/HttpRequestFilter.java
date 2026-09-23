package net.study.messagesocial.adapter.in.web.filter;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.study.messagecommon.constant.IdKey;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.regex.Pattern;


@Component
public class HttpRequestFilter implements Filter {

    private static final String REQUIRED_HEADER = IdKey.USER_ID.getValue();
    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("^[0-9]+$");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        // k8s readiness/liveness probe, Prometheus 스크레이핑은 인증 헤더 없이 호출하므로 제외
        if (httpServletRequest.getRequestURI().startsWith("/actuator")) {
            chain.doFilter(request, response);
            return;
        }

        String requestId = httpServletRequest.getHeader(REQUIRED_HEADER);
        if (requestId == null || requestId.isBlank()) {
            reject(httpServletResponse, HttpStatus.BAD_REQUEST, "Missing required header: " + REQUIRED_HEADER);
            return;
        }

        if (!REQUEST_ID_PATTERN.matcher(requestId).matches()) {
            reject(httpServletResponse, HttpStatus.BAD_REQUEST, "Invalid USER_ID value");
            return;
        }

        chain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
