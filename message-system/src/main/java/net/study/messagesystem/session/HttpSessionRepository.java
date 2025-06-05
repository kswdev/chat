package net.study.messagesystem.session;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class HttpSessionRepository implements HttpSessionListener {

    private final Map<String, HttpSession> sessions = new ConcurrentHashMap<>();

    public HttpSession findById(String sessionId) {
        return sessions.get(sessionId);
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        sessions.put(se.getSession().getId(), se.getSession());
        log.info("Session created : {}", se.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        sessions.remove(se.getSession().getId(), se.getSession());
        log.info("Session destroyed : {}", se.getSession().getId());
    }
}
