package net.study.messagesocial.adapter.out.client.messageuser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpStatus.NOT_FOUND;

class MessageUserPersistenceAdapterTest {

    private static final String BASE_URL = "http://message-user";

    private MockRestServiceServer mockServer;
    private MessageUserPersistenceAdapter adapter;

    @BeforeEach
    void setup() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        adapter = new MessageUserPersistenceAdapter(builder.build());
    }

    @Test
    void getUserIdByUsername_returnsUserId_whenMessageUserFindsIt() {
        mockServer.expect(requestTo(BASE_URL + "/api/v1/user/by-username/alice"))
                .andRespond(withSuccess(
                        "{\"userId\":1,\"username\":\"alice\",\"inviteCode\":\"ALICE01\"}",
                        MediaType.APPLICATION_JSON));

        Optional<Long> result = adapter.getUserIdByUsername("alice");

        assertThat(result).contains(1L);
        mockServer.verify();
    }

    @Test
    void getUserIdByUsername_returnsEmpty_whenMessageUserReturns404() {
        mockServer.expect(requestTo(BASE_URL + "/api/v1/user/by-username/nobody"))
                .andRespond(withStatus(NOT_FOUND));

        Optional<Long> result = adapter.getUserIdByUsername("nobody");

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    void getUserIdByInviteCode_returnsUserId_whenMessageUserFindsIt() {
        mockServer.expect(requestTo(BASE_URL + "/api/v1/user/by-invite-code/BOB0001"))
                .andRespond(withSuccess(
                        "{\"userId\":2,\"username\":\"bob\",\"inviteCode\":\"BOB0001\"}",
                        MediaType.APPLICATION_JSON));

        Optional<Long> result = adapter.getUserIdByInviteCode("BOB0001");

        assertThat(result).contains(2L);
        mockServer.verify();
    }

    @Test
    void getUserIdByInviteCode_returnsEmpty_whenMessageUserReturns404() {
        mockServer.expect(requestTo(BASE_URL + "/api/v1/user/by-invite-code/NOPE"))
                .andRespond(withStatus(NOT_FOUND));

        Optional<Long> result = adapter.getUserIdByInviteCode("NOPE");

        assertThat(result).isEmpty();
        mockServer.verify();
    }

    @Test
    void getUsername_returnsUsername_whenMessageUserFindsIt() {
        mockServer.expect(requestTo(BASE_URL + "/api/v1/user/1"))
                .andRespond(withSuccess(
                        "{\"userId\":1,\"username\":\"alice\",\"inviteCode\":\"ALICE01\"}",
                        MediaType.APPLICATION_JSON));

        Optional<String> result = adapter.getUsername(1L);

        assertThat(result).contains("alice");
        mockServer.verify();
    }

    @Test
    void getInviteCode_returnsInviteCode_whenMessageUserFindsIt() {
        mockServer.expect(requestTo(BASE_URL + "/api/v1/user/1"))
                .andRespond(withSuccess(
                        "{\"userId\":1,\"username\":\"alice\",\"inviteCode\":\"ALICE01\"}",
                        MediaType.APPLICATION_JSON));

        Optional<String> result = adapter.getInviteCode(1L);

        assertThat(result).contains("ALICE01");
        mockServer.verify();
    }

    @Test
    void getUsername_returnsEmpty_whenMessageUserReturns404() {
        mockServer.expect(requestTo(BASE_URL + "/api/v1/user/999"))
                .andRespond(withStatus(NOT_FOUND));

        Optional<String> result = adapter.getUsername(999L);

        assertThat(result).isEmpty();
        mockServer.verify();
    }
}
