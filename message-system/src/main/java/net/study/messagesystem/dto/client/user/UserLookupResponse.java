package net.study.messagesystem.dto.client.user;

public record UserLookupResponse(Long userId, String username, String inviteCode) { }
