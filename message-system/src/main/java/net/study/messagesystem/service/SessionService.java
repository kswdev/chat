package net.study.messagesystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository<? extends Session> httpSessionRepository;

    public void refreshTTL(String httpSessionId) {
        Session httpSession = httpSessionRepository.findById(httpSessionId);
        if (httpSession != null) {
            httpSession.setLastAccessedTime(Instant.now());
        }
    }
}
