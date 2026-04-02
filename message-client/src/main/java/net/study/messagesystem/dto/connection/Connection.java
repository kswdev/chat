package net.study.messagesystem.dto.connection;

import net.study.messagecommon.constant.UserConnectionStatus;

public record Connection(String username, UserConnectionStatus status) { }
