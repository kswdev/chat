package net.study.messagesocial.domain.userconnection;

public record FriendConnectionSummary(Long partnerId, String partnerUsername, UserConnectionStatus status) { }
