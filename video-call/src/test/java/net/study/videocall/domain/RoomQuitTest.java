package net.study.videocall.domain;

import net.study.videocall.domain.model.Peer;
import net.study.videocall.domain.model.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class RoomQuitTest {

    private Room room;
    private Peer creator;

    @BeforeEach
    void setRoom() {
        this.creator = createPeer(0L, "creator");
        this.room = Room.create("test-1", "test-name", creator);
    }

    @DisplayName("참가자가 퇴장하면 인원이 1이 된다.")
    @Test
    void when_a_peer_quits_room_then_rooms_size_should_one() {
        // given
        Peer peer = createPeer(1L, "first");
        room.join(peer);

        // when
        room.quit(peer);

        // then
        assertFalse(room.isJoined(peer));
        assertThat(room.size()).isEqualTo(1);
    }

    @DisplayName("방에 남은 마지막 참가자가 퇴장하면 방이 비어있다.")
    @Test
    void when_last_peer_quits_room_then_room_should_empty() {
        // when
        room.quit(creator);

        // then
        assertFalse(room.isJoined(creator));
        assertTrue(room.isEmpty());
    }

    @DisplayName("참여하지 않은 사용자가 퇴장을 시도하면 예외가 발생한다.")
    @Test
    void when_not_joined_peer_quits_room_then_throw_exception() {
        // given
        Peer notJoinedPeer = createPeer(1L, "first");

        // when & then
        assertThatThrownBy(() -> room.quit(notJoinedPeer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("방 참여자가 아닙니다. : " + notJoinedPeer.getId());
    }

    @DisplayName("이미 퇴장한 참가자가 다시 퇴장을 시도하면 예외가 발생한다.")
    @Test
    void when_quit_peer_quits_room_again_then_throw_exception() {
        // given
        Peer peer = createPeer(1L, "first");
        room.join(peer);
        room.quit(peer);

        // when & then
        assertThatThrownBy(() -> room.quit(peer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("방 참여자가 아닙니다. : " + peer.getId());
    }

    private Peer createPeer(Long id, String name) {
        return Peer.create(id, name);
    }
}
