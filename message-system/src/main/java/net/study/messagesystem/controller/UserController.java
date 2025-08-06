package net.study.messagesystem.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.dto.rest.signup.SignUpRequest;
import net.study.messagesystem.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
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
            userService.removeUser();
            request.getSession().invalidate();
            return ResponseEntity.ok("User unregister.");
        } catch (Exception ex) {
            log.error("Remove user failed. cause: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unregister user failed");
        }
    }
}
