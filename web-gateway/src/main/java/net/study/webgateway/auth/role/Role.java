package net.study.webgateway.auth.role;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum Role {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    @Getter
    private final String name;

    Role(String name) {
        this.name = name;
    }

    public static Set<Role> fromCode(String data) {
        String[] array = data.split(",");
        Set<String> roleTypes = new HashSet<>(Arrays.asList(array));
        return Arrays.stream(Role.values())
                .filter(v -> roleTypes.contains(v.getName()))
                .collect(Collectors.toUnmodifiableSet());
    }
}
