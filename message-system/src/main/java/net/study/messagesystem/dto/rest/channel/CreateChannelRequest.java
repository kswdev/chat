package net.study.messagesystem.dto.rest.channel;

import java.util.List;

public record CreateChannelRequest(String title, List<String> participantUsernames) { }
