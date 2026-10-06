package net.study.videocall.domain;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode(of = "name")
@ToString(of = "name")
public class Peer {

    @Getter
    private final String name;

    private Peer(String name) {
        this.name = name;
    }

    public static Peer create(String name) {
        return new Peer(name);
    }
}
