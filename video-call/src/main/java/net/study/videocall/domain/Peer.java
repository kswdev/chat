package net.study.videocall.domain;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode(of = "name")
@ToString(of = "name")
public class Peer {

    @Getter
    private final Long id;

    @Getter
    private final String name;

    private Peer(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Peer create(Long id, String name) {
        return new Peer(id, name);
    }
}
