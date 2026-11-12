package net.study.videocall.domain.model;

public record Signal(Long senderId, SignalType type, String payload) {
}
