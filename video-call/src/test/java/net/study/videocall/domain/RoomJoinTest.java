package net.study.videocall.domain;

import net.study.videocall.domain.model.Peer;
import net.study.videocall.domain.model.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

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
        room.join(peer);

        // then
        assertTrue(room.isJoined(peer));
        assertThat(room.size()).isEqualTo(2);
    }

    @DisplayName("두 명이 입장해도 총 인원을 넘길 수 없어 인원은 여전히 2가 된다.")
    @Test
    void when_two_peers_join_room_then_rooms_size_should_two() {
        // given
        Peer first = createPeer(1L, "first");
        Peer second = createPeer(2L, "second");

        // when & then
        room.join(first);

        assertThatThrownBy(() -> room.join(second))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("방이 가득 찼습니다.");
    }

    @DisplayName("같은 참가자가 두 번 참가해도 참가 인원은 변하지 않는다.")
    @Test
    void when_joined_peer_again_request_join_then_room_size_is_not_change() {
        // given
        Peer first = createPeer(1L, "first");
        room.join(first);
        int previousJoinCount = room.size();

        // when
        room.join(first);
        int afterDuplicatelyJoinCount = room.size();

        // then
        assertTrue(room.isJoined(first));
        assertThat(room.size()).isEqualTo(2);
        assertEquals(previousJoinCount, afterDuplicatelyJoinCount);
    }

    private Peer createPeer(Long id, String name) {
        return Peer.create(id, name);
    }
}
