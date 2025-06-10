package net.study.messagesystem.auth;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@EqualsAndHashCode(of = "username")
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
}
