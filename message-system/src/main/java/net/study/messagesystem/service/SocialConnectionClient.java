package net.study.messagesystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.IdKey;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.client.social.ConnectionAcceptedCountResponse;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocialConnectionClient {

    private final RestClient messageSocialRestClient;
    private final RetryTemplate socialConnectionRetryTemplate;

    public long countAcceptedConnections(UserId userId, List<UserId> partnerUserIds) {
        if (partnerUserIds.isEmpty())
            return 0;

        List<Long> partnerIds = partnerUserIds.stream().map(UserId::id).toList();

        ConnectionAcceptedCountResponse response = socialConnectionRetryTemplate.execute(context ->
                messageSocialRestClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/api/v1/social/friends/connections/accepted-count")
                                .queryParam("userId", userId.id())
                                .queryParam("partnerIds", partnerIds)
                                .build())
                        .header(IdKey.USER_ID.getValue(), String.valueOf(userId.id()))
                        .retrieve()
                        .body(ConnectionAcceptedCountResponse.class));

        return response == null ? 0 : response.count();
    }
}
