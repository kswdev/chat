package net.study.messagesocial.adapter.out.client.messageuser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesocial.application.port.out.LoadUserPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageUserPersistenceAdapter implements LoadUserPort {

    private final RestClient messageUserRestClient;

    @Override
    public Optional<Long> getUserIdByInviteCode(String inviteCode) {
        return lookup("/api/v1/user/by-invite-code/{inviteCode}", inviteCode).map(UserLookupResponse::userId);
    }

    @Override
    public Optional<Long> getUserIdByUsername(String username) {
        return lookup("/api/v1/user/by-username/{username}", username).map(UserLookupResponse::userId);
    }

    @Override
    public Optional<String> getInviteCode(Long userId) {
        return lookup("/api/v1/user/{userId}", userId).map(UserLookupResponse::inviteCode);
    }

    @Override
    public Map<Long, String> getUsernames(Collection<Long> userIds) {
        if (userIds.isEmpty())
            return Map.of();

        List<UserLookupResponse> users = messageUserRestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/user/batch").queryParam("userIds", userIds).build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (users == null)
            return Map.of();

        return users.stream().collect(Collectors.toMap(UserLookupResponse::userId, UserLookupResponse::username));
    }

    private Optional<UserLookupResponse> lookup(String uriTemplate, Object pathVariable) {
        try {
            return Optional.ofNullable(
                    messageUserRestClient.get()
                            .uri(uriTemplate, pathVariable)
                            .retrieve()
                            .body(UserLookupResponse.class));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }
}
