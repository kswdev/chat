package net.study.videocall.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoomJoinTest {

    private Room room;

    @BeforeEach
    void setRoom() {
        this.room = Room.create("test-1", "test-name");
    }

    @DisplayName("한 명이 입장하면 인원이 1이 된다.")
    @Test
    void when_a_peer_joins_room_then_rooms_size_should_one() {
        // given
        Peer peer = Peer.create("first");

        // when
        boolean joined = room.join(peer);

        // then
        assertThat(joined).isTrue();
        assertThat(room.size()).isEqualTo(1);
    }

    @DisplayName("두 명이 입장하면 인원이 2이 된다.")
    @Test
    void when_two_peers_join_room_then_rooms_size_should_two() {
        // given
        Peer first = Peer.create("first");
        Peer second = Peer.create("second");

        // when
        boolean firstJoined = room.join(first);
        boolean secondJoined = room.join(second);

        // then
        assertThat(firstJoined).isTrue();
        assertThat(secondJoined).isTrue();
        assertThat(room.size()).isEqualTo(2);
    }

    @DisplayName("세 명이 입장하면 인원이 2이 된다.")
    @Test
    void when_room_is_full_then_join_should_reject() {
        // given
        Peer first = Peer.create("first");
        Peer second = Peer.create("second");
        Peer third = Peer.create("third");

        boolean firstJoined = room.join(first);
        boolean secondJoined = room.join(second);

        // when
        boolean thirdJoined = room.join(third);

        // then
        assertThat(firstJoined).isTrue();
        assertThat(secondJoined).isTrue();
        assertThat(thirdJoined).isFalse();

        assertThat(room.size()).isEqualTo(2);
    }

    @DisplayName("같은 참가자가 두 번 참가해도 참가 인원은 변하지 않는다.")
    @Test
    void when_joined_peer_again_request_join_then_room_size_is_not_change() {
        // given
        Peer first = Peer.create("first");

        boolean firstJoined = room.join(first);

        // when
        boolean firstJoinedAgainResult = room.join(first);

        // then
        assertThat(firstJoined).isTrue();
        assertThat(firstJoinedAgainResult).isTrue();

        assertThat(room.size()).isEqualTo(1);
    }
}
