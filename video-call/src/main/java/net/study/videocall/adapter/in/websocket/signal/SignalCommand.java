package net.study.videocall.adapter.in.websocket.signal;

import com.fasterxml.jackson.databind.JsonNode;
import net.study.videocall.domain.model.SignalType;

public record SignalCommand(SignalType type, JsonNode payload) {
}
