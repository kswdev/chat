package net.study.videocall.adapter.in.security;

import net.study.messagecommon.constant.IdKey;
import net.study.videocall.domain.model.Peer;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class PeerIdentityResolver {

    public Peer resolve(HttpHeaders headers) {
        String userId = headers.getFirst(IdKey.USER_ID.getValue());
        if (userId == null) {
            throw new IllegalArgumentException("USER_ID header is missing");
        }

        String username = headers.getFirst(IdKey.USERNAME.getValue());
        if (username == null) {
            throw new IllegalArgumentException("USERNAME header is missing");
        }

        return Peer.create(Long.valueOf(userId), username);
    }
}
