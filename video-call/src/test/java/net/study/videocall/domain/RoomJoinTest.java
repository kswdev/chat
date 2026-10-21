package net.study.videocall.domain;

import net.study.videocall.domain.model.Peer;
import net.study.videocall.domain.model.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoomJoinTest {

    private Room room;

    @BeforeEach
    void setRoom() {
        Peer creator = createPeer(0L, "creator");
        this.room = Room.create("test-1", "test-name", creator);
    }

    @DisplayName("한 명이 입장하면 인원이 2가 된다.")
    @Test
    void when_a_peer_joins_room_then_rooms_size_should_two() {
        // given
        Peer peer = createPeer(1L, "first");

        // when
        boolean joined = room.join(peer);

        // then
        assertThat(joined).isTrue();
        assertThat(room.size()).isEqualTo(2);
    }

    @DisplayName("두 명이 입장해도 총 인원을 넘길 수 없어 인원은 여전히 2가 된다.")
    @Test
    void when_two_peers_join_room_then_rooms_size_should_two() {
        // given
        Peer first = createPeer(1L, "first");
        Peer second = createPeer(2L, "second");

        // when
        boolean firstJoined = room.join(first);
        boolean secondJoined = room.join(second);

        // then
        assertThat(firstJoined).isTrue();
        assertThat(secondJoined).isFalse();
        assertThat(room.size()).isEqualTo(2);
    }

    @DisplayName("같은 참가자가 두 번 참가해도 참가 인원은 변하지 않는다.")
    @Test
    void when_joined_peer_again_request_join_then_room_size_is_not_change() {
        // given
        Peer first = createPeer(1L, "first");

        boolean firstJoined = room.join(first);

        // when
        boolean firstJoinedAgainResult = room.join(first);

        // then
        assertThat(firstJoined).isTrue();
        assertThat(firstJoinedAgainResult).isTrue();

        assertThat(room.size()).isEqualTo(2);
    }

    private Peer createPeer(Long id, String name) {
        return Peer.create(id, name);
    }
}
