package net.study.messagesystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.client.user.UserLookupResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageUserClient {

    private final RestClient messageUserRestClient;

    public Map<UserId, String> getUsernames(Collection<UserId> userIds) {
        if (userIds.isEmpty())
            return Map.of();

        List<Long> ids = userIds.stream().map(UserId::id).toList();

        List<UserLookupResponse> responses = messageUserRestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/user/batch")
                        .queryParam("userIds", ids)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<UserLookupResponse>>() { });

        if (responses == null)
            return Map.of();

        return responses.stream()
                .collect(Collectors.toMap(r -> new UserId(r.userId()), UserLookupResponse::username));
    }

    public List<UserId> getUserIds(List<String> usernames) {
        if (usernames.isEmpty())
            return List.of();

        List<UserLookupResponse> responses = messageUserRestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/user/batch-by-username")
                        .queryParam("usernames", usernames)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<UserLookupResponse>>() { });

        if (responses == null)
            return List.of();

        return responses.stream().map(r -> new UserId(r.userId())).toList();
    }
}
