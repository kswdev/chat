package net.study.messageconnection.domain.connection;


import net.study.messageconnection.constant.UserConnectionStatus;

public record Connection(String username, UserConnectionStatus status) { }
