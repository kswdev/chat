package net.study.videocall.domain;

import net.study.videocall.domain.model.Peer;
import net.study.videocall.domain.model.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoomPeerListTest {

    private Room room;
    private Peer creator;

    @BeforeEach
    void setRoom() {
        creator = createPeer(0L, "creator");
        room = Room.create("test-id", "test-name", creator);
    }

    @DisplayName("나를 제외한 참여자만 조회한다.")
    @Test
    void when_inquire_peer_list_then_get_peer_list_except_me() {
        // given
        Peer other = createPeer(2L, "other");

        room.join(other);

        // when
        List<Peer> list = room.otherThan(creator);

        // then
        assertThat(list).hasSize(1);
        assertThat(list).doesNotContain(creator);
        assertThat(list).contains(other);
    }

    private Peer createPeer(Long id, String name) {
        return Peer.create(id, name);
    }
}
