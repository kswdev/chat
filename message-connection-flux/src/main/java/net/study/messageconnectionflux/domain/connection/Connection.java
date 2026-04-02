package net.study.messageconnectionflux.domain.connection;


import net.study.messagecommon.constant.UserConnectionStatus;

public record Connection(String username, UserConnectionStatus status) { }
