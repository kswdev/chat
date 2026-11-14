package net.study.videocall.domain.model;

import com.fasterxml.jackson.databind.JsonNode;

public record Signal(Long senderId, SignalType type, JsonNode payload) {
}
