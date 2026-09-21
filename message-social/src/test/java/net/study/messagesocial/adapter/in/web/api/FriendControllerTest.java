package net.study.messagesocial.adapter.in.web.api;

import net.study.messagecommon.constant.IdKey;
import net.study.messagesocial.application.port.in.FriendInvite;
import net.study.messagesocial.domain.userconnection.UserConnection;
import net.study.messagesocial.domain.userconnection.UserConnectionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FriendController.class)
class FriendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FriendInvite friendInvite;

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
        mockMvc.perform(withValidHeaders(post("/api/v1/social/friends/invite/{inviteCode}", inviteCode), inviterUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inviter").value(inviterUserId))
                .andExpect(jsonPath("$.invitee").value(inviteeUserId))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(friendInvite).invite(inviterUserId, inviteCode);
    }

    private MockHttpServletRequestBuilder withValidHeaders(MockHttpServletRequestBuilder builder, Long userId) {
        return builder
                .header(IdKey.USER_ID.getValue(), userId);
    }

}