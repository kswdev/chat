package net.study.webgateway.auth.jwt;

import net.study.webgateway.auth.role.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class TokenUser {
    private String id;
    private Set<Role> role;
}
