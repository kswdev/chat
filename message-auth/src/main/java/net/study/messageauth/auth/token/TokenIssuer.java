package net.study.messageauth.auth.token;

public interface TokenIssuer {
    String issue(String username);
}
