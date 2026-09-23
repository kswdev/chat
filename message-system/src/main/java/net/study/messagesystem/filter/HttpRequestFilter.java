package net.study.messagesystem.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.study.messagecommon.constant.IdKey;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * web-gateway가 JWT 검증 후 주입하는 USER_ID 헤더를 검증한다. /api/v1/channel/** 에만 적용되고
 * (message-social의 HttpRequestFilter가 /actuator/health까지 막아서 k8s probe가 실패했던 것과
 * 같은 실수를 반복하지 않기 위해 대상 경로를 명시적으로 좁힘) 그 외 요청(actuator 등)은 그대로 통과시킨다.
 */
@Component
public class HttpRequestFilter implements Filter {

    private static final String TARGET_PATH_PREFIX = "/api/v1/channel";
    private static final String REQUIRED_HEADER = IdKey.USER_ID.getValue();
    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("^[0-9]+$");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        if (!httpServletRequest.getRequestURI().startsWith(TARGET_PATH_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String userId = httpServletRequest.getHeader(REQUIRED_HEADER);
        if (userId == null || userId.isBlank()) {
            reject(httpServletResponse, HttpStatus.BAD_REQUEST, "Missing required header: " + REQUIRED_HEADER);
            return;
        }

        if (!REQUEST_ID_PATTERN.matcher(userId).matches()) {
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
