package net.study.messagesocial.adapter.in.web.api;

import net.study.messagecommon.constant.IdKey;
import net.study.messagesocial.application.port.in.FriendInvite;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

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
        String inviteCode = "ABC123";
        Long userId = 123L;

        // when & then
        mockMvc.perform(withValidHeaders(post("/api/v1/social/friends/invite/{inviteCode}", inviteCode), userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.inviteCode").value(inviteCode))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(friendInvite).invite(userId, inviteCode);
    }

    private MockHttpServletRequestBuilder withValidHeaders(MockHttpServletRequestBuilder builder, Long userId) {
        return builder
                .header(IdKey.USER_ID.getValue(), userId);
    }

}