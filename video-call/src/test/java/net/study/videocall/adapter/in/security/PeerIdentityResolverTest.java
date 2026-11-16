package net.study.videocall.adapter.in.security;

import net.study.messagecommon.constant.IdKey;
import net.study.videocall.domain.model.Peer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeerIdentityResolverTest {

    PeerIdentityResolver resolver = new PeerIdentityResolver();

    @DisplayName("USER_ID, USERNAME 헤더가 모두 있으면 Peer를 생성한다.")
    @Test
    void when_headers_present_then_resolve_peer() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(IdKey.USER_ID.getValue(), "1");
        headers.add(IdKey.USERNAME.getValue(), "kim");

        Peer peer = resolver.resolve(headers);

        assertThat(peer.getId()).isEqualTo(1L);
        assertThat(peer.getName()).isEqualTo("kim");
    }

    @DisplayName("USER_ID 헤더가 없으면 예외를 던진다.")
    @Test
    void when_user_id_header_missing_then_throw() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(IdKey.USERNAME.getValue(), "kim");

        assertThatThrownBy(() -> resolver.resolve(headers))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("USERNAME 헤더가 없으면 예외를 던진다.")
    @Test
    void when_username_header_missing_then_throw() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(IdKey.USER_ID.getValue(), "1");

        assertThatThrownBy(() -> resolver.resolve(headers))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
