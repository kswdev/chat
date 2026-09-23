package net.study.messageuser.controller;

import net.study.messageuser.entity.user.UserEntity;
import net.study.messageuser.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void getByUsername_returns200WithUserWhenFound() throws Exception {
        UserEntity user = UserEntity.testUser(1L, "alice", "ALICE01");
        given(userService.getByUsername("alice")).willReturn(Optional.of(user));

        mockMvc.perform(get("/api/v1/user/by-username/{username}", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.inviteCode").value("ALICE01"));
    }

    @Test
    void getByUsername_returns404WhenNotFound() throws Exception {
        given(userService.getByUsername("nobody")).willReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/user/by-username/{username}", "nobody"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByInviteCode_returns200WithUserWhenFound() throws Exception {
        UserEntity user = UserEntity.testUser(2L, "bob", "BOB0001");
        given(userService.getByInviteCode("BOB0001")).willReturn(Optional.of(user));

        mockMvc.perform(get("/api/v1/user/by-invite-code/{inviteCode}", "BOB0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.username").value("bob"));
    }

    @Test
    void getByInviteCode_returns404WhenNotFound() throws Exception {
        given(userService.getByInviteCode("NOPE")).willReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/user/by-invite-code/{inviteCode}", "NOPE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_returns200WithUserWhenFound() throws Exception {
        UserEntity user = UserEntity.testUser(1L, "alice", "ALICE01");
        given(userService.getById(1L)).willReturn(Optional.of(user));

        mockMvc.perform(get("/api/v1/user/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inviteCode").value("ALICE01"));
    }

    @Test
    void getById_returns404WhenNotFound() throws Exception {
        given(userService.getById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/user/{userId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByIds_returns200WithMatchingUsers() throws Exception {
        UserEntity alice = UserEntity.testUser(1L, "alice", "ALICE01");
        UserEntity bob = UserEntity.testUser(2L, "bob", "BOB0001");
        given(userService.getByIds(List.of(1L, 2L))).willReturn(List.of(alice, bob));

        mockMvc.perform(get("/api/v1/user/batch").param("userIds", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[1].userId").value(2))
                .andExpect(jsonPath("$[1].username").value("bob"));
    }
}
