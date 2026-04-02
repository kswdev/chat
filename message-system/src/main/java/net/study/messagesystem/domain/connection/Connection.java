package net.study.messagesystem.domain.connection;

import net.study.messagecommon.constant.UserConnectionStatus;

public record Connection(String username, UserConnectionStatus status) { }
