package net.study.messagesocial;

import net.study.messagecommon.constant.IdKey;
import net.study.messagesocial.adapter.out.notification.KafkaProducer;
import net.study.messagesocial.adapter.out.notification.PushService;
import net.study.messagesocial.adapter.out.persistence.user.UserConnectionCountJpaRepository;
import net.study.messagesocial.application.port.out.LoadUserPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FriendConnectionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserConnectionCountJpaRepository userConnectionCountJpaRepository;

    @MockitoBean
    private LoadUserPort loadUser;

    @MockitoBean
    private PushService pushService;

    @MockitoBean
    private KafkaProducer kafkaProducer;

    private static final Long ALICE_ID = 1L;
    private static final Long BOB_ID = 2L;

    @BeforeEach
    void stubMessageUserLookups() {
        given(loadUser.getUserIdByUsername("alice")).willReturn(Optional.of(ALICE_ID));
        given(loadUser.getUserIdByUsername("bob")).willReturn(Optional.of(BOB_ID));
        given(loadUser.getUserIdByInviteCode("BOB0001")).willReturn(Optional.of(BOB_ID));
        given(loadUser.getUserIdByInviteCode("ALICE01")).willReturn(Optional.of(ALICE_ID));
        given(loadUser.getUserIdByInviteCode("NOPE")).willReturn(Optional.empty());
        given(loadUser.getInviteCode(ALICE_ID)).willReturn(Optional.of("ALICE01"));
        given(loadUser.getInviteCode(BOB_ID)).willReturn(Optional.of("BOB0001"));
        given(loadUser.getUsernames(any())).willReturn(Map.of(ALICE_ID, "alice", BOB_ID, "bob"));
    }

    @Test
    void invite_then_accept_establishes_an_accepted_connection_visible_to_both_users() throws Exception {
        // alice invites bob using bob's invite code
        mockMvc.perform(asUser(post("/api/v1/social/friends/invite/{inviteCode}", "BOB0001"), ALICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        // bob accepts alice's invite by alice's username
        mockMvc.perform(asUser(post("/api/v1/social/friends/accept/{username}", "alice"), BOB_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        // both sides now see each other under ACCEPTED
        mockMvc.perform(asUser(get("/api/v1/social/friends/connections").param("status", "ACCEPTED"), ALICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connections[0].userId").value(BOB_ID))
                .andExpect(jsonPath("$.connections[0].username").value("bob"))
                .andExpect(jsonPath("$.connections[0].status").value("ACCEPTED"));

        mockMvc.perform(asUser(get("/api/v1/social/friends/connections").param("status", "ACCEPTED"), BOB_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connections[0].userId").value(ALICE_ID))
                .andExpect(jsonPath("$.connections[0].username").value("alice"))
                .andExpect(jsonPath("$.connections[0].status").value("ACCEPTED"));
    }

    @Test
    void invite_then_reject_leaves_connection_rejected_and_not_disconnectable() throws Exception {
        mockMvc.perform(asUser(post("/api/v1/social/friends/invite/{inviteCode}", "BOB0001"), ALICE_ID))
                .andExpect(status().isOk());

        mockMvc.perform(asUser(post("/api/v1/social/friends/reject/{username}", "alice"), BOB_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        // disconnect only makes sense for an ACCEPTED connection
        mockMvc.perform(asUser(post("/api/v1/social/friends/disconnect/{username}", "alice"), BOB_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CONNECTION_STATUS"));
    }

    @Test
    void accepted_connection_can_be_disconnected_by_either_original_party() throws Exception {
        mockMvc.perform(asUser(post("/api/v1/social/friends/invite/{inviteCode}", "BOB0001"), ALICE_ID))
                .andExpect(status().isOk());
        mockMvc.perform(asUser(post("/api/v1/social/friends/accept/{username}", "alice"), BOB_ID))
                .andExpect(status().isOk());

        // the original inviter (alice) disconnects, even though bob was the one who accepted
        mockMvc.perform(asUser(post("/api/v1/social/friends/disconnect/{username}", "bob"), ALICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISCONNECTED"));
    }

    @Test
    void re_inviting_after_disconnect_creates_a_new_pending_connection() throws Exception {
        mockMvc.perform(asUser(post("/api/v1/social/friends/invite/{inviteCode}", "BOB0001"), ALICE_ID))
                .andExpect(status().isOk());
        mockMvc.perform(asUser(post("/api/v1/social/friends/accept/{username}", "alice"), BOB_ID))
                .andExpect(status().isOk());
        mockMvc.perform(asUser(post("/api/v1/social/friends/disconnect/{username}", "bob"), ALICE_ID))
                .andExpect(status().isOk());

        mockMvc.perform(asUser(post("/api/v1/social/friends/invite/{inviteCode}", "BOB0001"), ALICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void accept_then_disconnect_updates_connection_count_on_both_sides() throws Exception {
        mockMvc.perform(asUser(post("/api/v1/social/friends/invite/{inviteCode}", "BOB0001"), ALICE_ID))
                .andExpect(status().isOk());
        mockMvc.perform(asUser(post("/api/v1/social/friends/accept/{username}", "alice"), BOB_ID))
                .andExpect(status().isOk());

        assertThat(userConnectionCountJpaRepository.findById(ALICE_ID).orElseThrow().getConnectionCount()).isEqualTo(1);
        assertThat(userConnectionCountJpaRepository.findById(BOB_ID).orElseThrow().getConnectionCount()).isEqualTo(1);

        mockMvc.perform(asUser(post("/api/v1/social/friends/disconnect/{username}", "bob"), ALICE_ID))
                .andExpect(status().isOk());

        assertThat(userConnectionCountJpaRepository.findById(ALICE_ID).orElseThrow().getConnectionCount()).isEqualTo(0);
        assertThat(userConnectionCountJpaRepository.findById(BOB_ID).orElseThrow().getConnectionCount()).isEqualTo(0);
    }

    @Test
    void invite_with_unknown_invite_code_returns_404() throws Exception {
        mockMvc.perform(asUser(post("/api/v1/social/friends/invite/{inviteCode}", "NOPE"), ALICE_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INVITE_CODE_NOT_FOUND"));
    }

    @Test
    void invite_with_own_invite_code_returns_400() throws Exception {
        mockMvc.perform(asUser(post("/api/v1/social/friends/invite/{inviteCode}", "ALICE01"), ALICE_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SELF_INVITE_NOT_ALLOWED"));
    }

    @Test
    void inviteCode_endpoint_returns_the_caller_own_invite_code() throws Exception {
        mockMvc.perform(asUser(get("/api/v1/social/friends/invite-code"), ALICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inviteCode").value("ALICE01"));
    }

    private MockHttpServletRequestBuilder asUser(MockHttpServletRequestBuilder builder, Long userId) {
        return builder
                .header(IdKey.USER_ID.getValue(), userId)
                .header(IdKey.USERNAME.getValue(), usernameOf(userId));
    }

    private String usernameOf(Long userId) {
        if (userId.equals(ALICE_ID)) return "alice";
        if (userId.equals(BOB_ID)) return "bob";
        throw new IllegalArgumentException("Unknown test userId: " + userId);
    }
}
