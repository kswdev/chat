package net.study.webgateway.auth.jwt;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import net.study.webgateway.auth.role.Role;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class JwtToTokenUserConverter {

    public TokenUser convert(DecodedJWT jwt) {
        return new TokenUser(
                String.valueOf(jwt.getSubject()),
                extractAuthoritiesFromClaim(jwt)
        );
    }


    private Set<Role> extractAuthoritiesFromClaim(DecodedJWT jwt) {
        Claim claim = jwt.getClaim("roles");
        if (claim.isNull() || claim.isMissing()) return Set.of();

        String joinedRole = String.join(",", claim.asList(String.class));

        return Role.fromCode(joinedRole);
    }
}
