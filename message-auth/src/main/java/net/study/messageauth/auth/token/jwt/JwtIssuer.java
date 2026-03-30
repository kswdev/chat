package net.study.messageauth.auth.token.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import net.study.messageauth.auth.token.TokenIssuer;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtIssuer implements TokenIssuer {

    private final JwtProperties properties;

    public String issue(Long memId) {
        return JWT.create()
                .withSubject(String.valueOf(memId))
                .withExpiresAt(new Date(System.currentTimeMillis() + properties.getExpirationTime()))
                .sign(Algorithm.HMAC256(properties.getSecretKey()));
    }
}
