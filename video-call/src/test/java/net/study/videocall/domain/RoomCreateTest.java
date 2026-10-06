package net.study.videocall.domain;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomCreateTest {

    @DisplayName("새로 만든 방은 비어있다.")
    @Test
    void when_room_is_created_then_room_is_empty() {
        // given
        String roomId = "test-id";
        String name = "test-name";

        // when
        Room room = Room.create(roomId, name);

        // then
        assertThat(room.isEmpty()).isTrue();
    }

    @DisplayName("이름 없이는 방을 만들 수 없다.")
    @ParameterizedTest
    @MethodSource("invalidRoomName")
    void when_room_is_created_then_name_should_exists(String roomId, String name) {

        // when & then
        assertThatThrownBy(() -> Room.create(roomId, name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("방 이름");
    }

    private static Stream<Arguments> invalidRoomName() {
        return Stream.of(
                Arguments.of("room-1", ""),
                Arguments.of("room-2", " "),
                Arguments.of("room-3", null)
        );
    }


}