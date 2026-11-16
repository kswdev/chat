package net.study.videocall.adapter.out.web.room;

import net.study.videocall.domain.model.Room;

public record RoomSummary(String roomId, String name, int peerCount) {

    public static RoomSummary from(Room room) {
        return new RoomSummary(room.getId(), room.getName(), room.size());
    }
}
