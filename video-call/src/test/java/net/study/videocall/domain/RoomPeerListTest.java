package net.study.videocall.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoomPeerListTest {

    private Room room;

    @BeforeEach
    void setRoom() {
        room = Room.create("test-id", "test-name");
    }

    @DisplayName("나를 제외한 참여자만 조회한다.")
    @Test
    void when_inquire_peer_list_then_get_peer_list_except_me() {
        // given
        Peer me = Peer.create("me");
        Peer other = Peer.create("other");

        room.join(me);
        room.join(other);

        // when
        List<Peer> list = room.otherThan(me);

        // then
        assertThat(list).hasSize(1);
        assertThat(list).doesNotContain(me);
        assertThat(list).contains(other);
    }

}
