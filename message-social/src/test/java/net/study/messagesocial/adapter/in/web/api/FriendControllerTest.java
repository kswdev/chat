package net.study.messagesocial.adapter.in.web.api;

import net.study.messagecommon.constant.IdKey;
import net.study.messagesocial.adapter.in.web.dto.error.FriendErrorCode;
import net.study.messagesocial.adapter.in.web.exception.AlreadyConnectedException;
import net.study.messagesocial.adapter.in.web.exception.ConnectionNotFoundException;
import net.study.messagesocial.adapter.in.web.exception.InvalidConnectionStatusException;
import net.study.messagesocial.application.port.in.*;
import net.study.messagesocial.application.service.FriendNotificationService;
import net.study.messagesocial.domain.userconnection.FriendConnectionSummary;
import net.study.messagesocial.domain.userconnection.UserConnection;
import net.study.messagesocial.domain.userconnection.UserConnectionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FriendController.class)
class FriendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FriendInvite friendInvite;

    @MockitoBean
    private FriendAccept friendAccept;

    @MockitoBean
    private FriendReject friendReject;

    @MockitoBean
    private FriendDisconnect friendDisconnect;

    @MockitoBean
    private FriendConnectionQuery friendConnectionQuery;

    @MockitoBean
    private FriendInviteCodeQuery friendInviteCodeQuery;

    @MockitoBean
    private FriendNotificationService friendNotificationService;

    @Test
    void shouldReturn200WhenInviteCodeIsValid() throws Exception {
        // given
        Long inviterUserId = 123L;
        Long inviteeUserId = 456L;

        String inviteCode = "ABC123";

        UserConnection userConnection = UserConnection.builder()
                        .inviterId(inviterUserId)
                        .inviteeId(inviteeUserId)
                        .build();

        userConnection.changeStatus(UserConnectionStatus.PENDING);

        given(friendInvite.invite(inviterUserId, inviteCode))
                .willReturn(userConnection);

        // when & then
        mockMvc.perform(withValidHeaders(post("/api/v1/social/friends/invite/{inviteCode}", inviteCode), inviterUserId, "inviter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inviter").value(inviterUserId))
                .andExpect(jsonPath("$.invitee").value(inviteeUserId))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(friendInvite).invite(inviterUserId, inviteCode);
        verify(friendNotificationService).notifyInvite(inviteeUserId, "inviter");
    }

    @Test
    void shouldReturn409WhenAlreadyFriend() throws Exception {
        Long inviterUserId = 123L;
        String inviteCode = "ABC123";

        given(friendInvite.invite(inviterUserId, inviteCode))
                .willThrow(new AlreadyConnectedException(FriendErrorCode.ALREADY_FRIEND));

        mockMvc.perform(withValidHeaders(post("/api/v1/social/friends/invite/{inviteCode}", inviteCode), inviterUserId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ALREADY_FRIEND"));
    }

    @Test
    void shouldReturn200AndNotifyInviterWhenAcceptSucceeds() throws Exception {
        Long accepterUserId = 456L;
        Long inviterUserId = 123L;
        String inviterUsername = "inviter";

        UserConnection userConnection = UserConnection.builder()
                .inviterId(inviterUserId)
                .inviteeId(accepterUserId)
                .status(UserConnectionStatus.ACCEPTED)
                .build();

        given(friendAccept.accept(accepterUserId, inviterUsername)).willReturn(userConnection);

        mockMvc.perform(withValidHeaders(post("/api/v1/social/friends/accept/{username}", inviterUsername), accepterUserId, "accepter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        verify(friendNotificationService).notifyAccept(inviterUserId, "accepter");
    }

    @Test
    void shouldReturn404WhenAcceptTargetConnectionMissing() throws Exception {
        Long accepterUserId = 456L;
        String inviterUsername = "inviter";

        given(friendAccept.accept(accepterUserId, inviterUsername))
                .willThrow(new ConnectionNotFoundException(FriendErrorCode.CONNECTION_NOT_FOUND));

        mockMvc.perform(withValidHeaders(post("/api/v1/social/friends/accept/{username}", inviterUsername), accepterUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CONNECTION_NOT_FOUND"));
    }

    @Test
    void shouldReturn200WhenRejectSucceeds() throws Exception {
        Long rejecterUserId = 456L;
        Long inviterUserId = 123L;
        String inviterUsername = "inviter";

        UserConnection userConnection = UserConnection.builder()
                .inviterId(inviterUserId)
                .inviteeId(rejecterUserId)
                .status(UserConnectionStatus.REJECTED)
                .build();

        given(friendReject.reject(rejecterUserId, inviterUsername)).willReturn(userConnection);

        mockMvc.perform(withValidHeaders(post("/api/v1/social/friends/reject/{username}", inviterUsername), rejecterUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void shouldReturn400WhenDisconnectTargetIsNotAccepted() throws Exception {
        Long senderUserId = 456L;
        String partnerUsername = "partner";

        given(friendDisconnect.disconnect(senderUserId, partnerUsername))
                .willThrow(new InvalidConnectionStatusException(FriendErrorCode.INVALID_CONNECTION_STATUS));

        mockMvc.perform(withValidHeaders(post("/api/v1/social/friends/disconnect/{username}", partnerUsername), senderUserId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CONNECTION_STATUS"));
    }

    @Test
    void shouldReturnConnectionsForGivenStatus() throws Exception {
        Long userId = 456L;

        given(friendConnectionQuery.getConnections(userId, UserConnectionStatus.ACCEPTED))
                .willReturn(List.of(new FriendConnectionSummary(789L, "bob", UserConnectionStatus.ACCEPTED)));

        mockMvc.perform(withValidHeaders(
                        get("/api/v1/social/friends/connections").param("status", "ACCEPTED"), userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connections[0].userId").value(789))
                .andExpect(jsonPath("$.connections[0].username").value("bob"))
                .andExpect(jsonPath("$.connections[0].status").value("ACCEPTED"));
    }

    @Test
    void shouldReturnOwnInviteCode() throws Exception {
        Long userId = 456L;
        given(friendInviteCodeQuery.getInviteCode(userId)).willReturn("ZZZ999");

        mockMvc.perform(withValidHeaders(get("/api/v1/social/friends/invite-code"), userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inviteCode").value("ZZZ999"));
    }

    private MockHttpServletRequestBuilder withValidHeaders(MockHttpServletRequestBuilder builder, Long userId) {
        return withValidHeaders(builder, userId, "tester");
    }

    private MockHttpServletRequestBuilder withValidHeaders(MockHttpServletRequestBuilder builder, Long userId, String username) {
        return builder
                .header(IdKey.USER_ID.getValue(), userId)
                .header(IdKey.USERNAME.getValue(), username);
    }

}
