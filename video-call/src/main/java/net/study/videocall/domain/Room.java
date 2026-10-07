package net.study.videocall.domain;

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

    public static Room create(String id, String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("방 이름이 비어있습니다. : " + name);
        return new Room(id, name);
    }

    public boolean isEmpty() {
        return peers.isEmpty();
    }

    public boolean join(Peer peer) {
        if (isFull())
            return false;

        if (isAlreadyJoined(peer))
            return true;

        return peers.add(peer);
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
        return peers.size() == MAX_PEER;
    }

    private boolean isAlreadyJoined(Peer peer) {
        return peers.contains(peer);
    }
}
