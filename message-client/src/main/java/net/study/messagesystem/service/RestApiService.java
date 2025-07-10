package net.study.messagesystem.service;

import lombok.Getter;
import net.study.messagesystem.dto.login.LoginRequest;
import net.study.messagesystem.dto.signup.SignUpRequest;
import net.study.messagesystem.util.JsonUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.function.Predicate;

public class RestApiService {

    private final TerminalService terminalService;
    private final String url;

    @Getter
    private String sessionId;

    public RestApiService(TerminalService terminalService, String url) {
        this.terminalService = terminalService;
        this.url = "http://" + url;
    }

    public boolean register(String username, String password) {
        return request("/api/v1/auth/register", "", new SignUpRequest(username, password))
                .filter(isStatusCodeOK())
                .isPresent();
    }

    public boolean unregister() {
        if (sessionId.isEmpty()) return false;

        return request("/api/v1/auth/unregister", sessionId, null)
                .filter(isStatusCodeOK())
                .isPresent();
    }

    public boolean login(String username, String password) {
        return request("/api/v1/auth/login", "", new LoginRequest(username, password))
                .filter(isStatusCodeOK())
                .map(HttpResponse::body)
                .map(sessionId -> {
                    setSessionId(sessionId);
                    return true;
                })
                .orElse(false);
    }

    public boolean logout() {
        if (sessionId.isEmpty()) return false;

        return request("/api/v1/auth/logout", sessionId, null)
                .filter(isStatusCodeOK())
                .isPresent();
    }

    private Optional<HttpResponse<String>> request(String path, String sessionId, Object body) {
        try {
            HttpRequest.Builder builder = HttpRequest
                    .newBuilder()
                    .uri(new URI(url + path))
                    .header("Content-Type", "application/json");

            if (!sessionId.isEmpty())
                builder.header("Cookie", "SESSION=" + sessionId);

            if (body != null)
                JsonUtil.toJson(body)
                        .ifPresent(jsonBody -> builder.POST(HttpRequest.BodyPublishers.ofString(jsonBody)));
            else
                builder.POST(HttpRequest.BodyPublishers.noBody());

            HttpResponse<String> httpResponse =
                    HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());

            terminalService.printSystemMessage("Response Status: %d, body: %s".formatted(httpResponse.statusCode(), httpResponse.body()));

            return Optional.of(httpResponse);
        } catch (Exception e) {
            terminalService.printSystemMessage("Failed to request cause: %s".formatted(e.getMessage()));
            return Optional.empty();
        }
    }

    private Predicate<HttpResponse<String>> isStatusCodeOK() {
        return httpResponse -> httpResponse.statusCode() == 200;
    }

    private void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
