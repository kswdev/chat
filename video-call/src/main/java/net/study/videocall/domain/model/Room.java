package net.study.videocall.domain.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Room {

    private static final int MAX_PEER = 2;

    @Getter
    private final String id;

    @Getter
    private final String name;

    private List<Peer> peers = new ArrayList<>();

    private Room(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Room create(String id, String name, Peer peer) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("방 이름이 비어있습니다. : " + name);

        Room room = new Room(id, name);
        room.join(peer);
        return room;
    }

    public boolean isEmpty() {
        return peers.isEmpty();
    }

    public Room join(Peer peer) {
        if (isJoined(peer))
            return this;

        if (isFull())
            throw new IllegalStateException("방이 가득 찼습니다.");

        peers.add(peer);
        return this;
    }

    public Room quit(Peer peer) {
        if (!isJoined(peer))
            throw new IllegalArgumentException("방 참여자가 아닙니다. : " + peer.getId());

        peers.remove(peer);
        return this;
    }

    public int size() {
        return peers.size();
    }

    public List<Peer> otherThan(Peer participant) {
        return peers.stream()
                .filter(peer -> !peer.equals(participant))
                .toList();
    }

    private boolean isFull() {
        return peers.size() >= MAX_PEER;
    }

    public boolean isJoined(Peer peer) {
        return peers.contains(peer);
    }
}
