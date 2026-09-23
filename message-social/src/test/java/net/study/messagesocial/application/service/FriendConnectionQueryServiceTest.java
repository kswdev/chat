package net.study.messagesocial.application.service;

import net.study.messagesocial.domain.userconnection.FriendConnectionSummary;
import net.study.messagesocial.application.port.out.LoadUserConnectionPort;
import net.study.messagesocial.application.port.out.LoadUserPort;
import net.study.messagesocial.domain.userconnection.UserConnection;
import net.study.messagesocial.domain.userconnection.UserConnectionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FriendConnectionQueryServiceTest {

    @InjectMocks
    private FriendConnectionQueryService friendConnectionQuery;

    @Mock
    private LoadUserConnectionPort loadUserConnection;

    @Mock
    private LoadUserPort loadUser;

    @Test
    void getConnections_returns_partner_id_and_username_for_each_connection() {
        Long userId = 1L;
        Long partnerId = 2L;
        UserConnection connection = UserConnection.builder()
                .inviterId(userId).inviteeId(partnerId).status(UserConnectionStatus.ACCEPTED).build();

        given(loadUserConnection.findByUserIdAndStatus(userId, UserConnectionStatus.ACCEPTED))
                .willReturn(List.of(connection));
        given(loadUser.getUsernames(List.of(partnerId))).willReturn(Map.of(partnerId, "bob"));

        List<FriendConnectionSummary> result = friendConnectionQuery.getConnections(userId, UserConnectionStatus.ACCEPTED);

        assertThat(result).containsExactly(new FriendConnectionSummary(partnerId, "bob", UserConnectionStatus.ACCEPTED));
    }

    @Test
    void getConnections_resolves_partner_as_the_other_side_when_userId_is_the_invitee() {
        Long userId = 2L;
        Long partnerId = 1L;
        UserConnection connection = UserConnection.builder()
                .inviterId(partnerId).inviteeId(userId).status(UserConnectionStatus.ACCEPTED).build();

        given(loadUserConnection.findByUserIdAndStatus(userId, UserConnectionStatus.ACCEPTED))
                .willReturn(List.of(connection));
        given(loadUser.getUsernames(List.of(partnerId))).willReturn(Map.of(partnerId, "alice"));

        List<FriendConnectionSummary> result = friendConnectionQuery.getConnections(userId, UserConnectionStatus.ACCEPTED);

        assertThat(result).containsExactly(new FriendConnectionSummary(partnerId, "alice", UserConnectionStatus.ACCEPTED));
    }

    @Test
    void getConnections_leaves_username_null_when_batch_lookup_has_no_match() {
        Long userId = 1L;
        Long partnerId = 2L;
        UserConnection connection = UserConnection.builder()
                .inviterId(userId).inviteeId(partnerId).status(UserConnectionStatus.ACCEPTED).build();

        given(loadUserConnection.findByUserIdAndStatus(userId, UserConnectionStatus.ACCEPTED))
                .willReturn(List.of(connection));
        given(loadUser.getUsernames(List.of(partnerId))).willReturn(Map.of());

        List<FriendConnectionSummary> result = friendConnectionQuery.getConnections(userId, UserConnectionStatus.ACCEPTED);

        assertThat(result).containsExactly(new FriendConnectionSummary(partnerId, null, UserConnectionStatus.ACCEPTED));
    }

    @Test
    void getConnections_returns_empty_list_when_none_found() {
        given(loadUserConnection.findByUserIdAndStatus(1L, UserConnectionStatus.PENDING))
                .willReturn(List.of());
        given(loadUser.getUsernames(List.of())).willReturn(Map.of());

        List<FriendConnectionSummary> result = friendConnectionQuery.getConnections(1L, UserConnectionStatus.PENDING);

        assertThat(result).isEmpty();
    }
}
