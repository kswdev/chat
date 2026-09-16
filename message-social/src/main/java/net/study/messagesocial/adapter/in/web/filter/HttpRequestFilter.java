package net.study.messagesocial.adapter.in.web.filter;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.study.messagecommon.constant.IdKey;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class HttpRequestFilter implements Filter {

    private static final String REQUIRED_HEADER = IdKey.USER_ID.getValue();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        String requestId = httpServletRequest.getHeader(REQUIRED_HEADER);
        if (requestId == null || requestId.isBlank()) {
            reject(httpServletResponse, HttpStatus.BAD_REQUEST, "Missing required header: " + REQUIRED_HEADER);
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
