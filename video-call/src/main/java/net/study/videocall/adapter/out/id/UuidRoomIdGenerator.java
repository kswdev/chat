package net.study.videocall.adapter.out.id;

import net.study.videocall.application.port.out.RoomIdGenerator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidRoomIdGenerator implements RoomIdGenerator {

    @Override
    public String create() {
        return UUID.randomUUID().toString();
    }
}
