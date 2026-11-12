package net.study.videocall.adapter.in.websocket.signal;

import net.study.videocall.domain.model.SignalType;

public record SignalCommand(SignalType type, String payload) {
}
