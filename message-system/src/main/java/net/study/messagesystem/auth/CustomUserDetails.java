package net.study.messagesystem.auth;

import com.fasterxml.jackson.annotation.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(of = "username")
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private String password;

    @JsonCreator
    public CustomUserDetails(
            @JsonProperty("userId") Long userId,
            @JsonProperty("username") String username,
            @JsonProperty("password") String password
    ) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    public void erasePassword() {
        this.password = "";
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
}
