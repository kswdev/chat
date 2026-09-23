package net.study.messagesystem.dto.rest.channel;

import net.study.messagesystem.domain.channel.ChannelId;

public record ChannelResponse(ChannelId channelId, String title) { }
