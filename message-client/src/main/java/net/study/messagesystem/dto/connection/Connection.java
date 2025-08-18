package net.study.messagesystem.dto.connection;

import net.study.messagesystem.constant.UserConnectionStatus;

public record Connection(String username, UserConnectionStatus status) { }
