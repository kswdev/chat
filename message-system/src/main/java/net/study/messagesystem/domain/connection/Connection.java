package net.study.messagesystem.domain.connection;

import net.study.messagesystem.constant.UserConnectionStatus;

public record Connection(String username, UserConnectionStatus status) { }
