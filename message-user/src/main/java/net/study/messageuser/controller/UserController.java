package net.study.messageuser.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.study.messagecommon.constant.IdKey;
import net.study.messageuser.dto.rest.signup.SignUpRequest;
import net.study.messageuser.dto.rest.user.UserLookupResponse;
import net.study.messageuser.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody SignUpRequest request
    ) {
        try {
            userService.addUser(request.username(), request.password());
            return ResponseEntity.ok("User register.");
        } catch (Exception ex) {
            log.error("Add user failed. cause: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Register user failed");
        }
    }

    @PostMapping("/unregister")
    public ResponseEntity<String> unregister(HttpServletRequest request) {
        try {
            String userId = request.getHeader(IdKey.USER_ID.getValue());
            userService.removeUser(userId);
            return ResponseEntity.ok("User unregister.");
        } catch (Exception ex) {
            log.error("Remove user failed. cause: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unregister user failed");
        }
    }

    @GetMapping("/by-username/{username}")
    public ResponseEntity<UserLookupResponse> getByUsername(@PathVariable String username) {
        return userService.getByUsername(username)
                .map(UserLookupResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-invite-code/{inviteCode}")
    public ResponseEntity<UserLookupResponse> getByInviteCode(@PathVariable String inviteCode) {
        return userService.getByInviteCode(inviteCode)
                .map(UserLookupResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserLookupResponse> getById(@PathVariable Long userId) {
        return userService.getById(userId)
                .map(UserLookupResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/batch")
    public ResponseEntity<List<UserLookupResponse>> getByIds(@RequestParam List<Long> userIds) {
        List<UserLookupResponse> users = userService.getByIds(userIds).stream()
                .map(UserLookupResponse::from)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/batch-by-username")
    public ResponseEntity<List<UserLookupResponse>> getByUsernames(@RequestParam List<String> usernames) {
        List<UserLookupResponse> users = userService.getByUsernames(usernames).stream()
                .map(UserLookupResponse::from)
                .toList();
        return ResponseEntity.ok(users);
    }
}
