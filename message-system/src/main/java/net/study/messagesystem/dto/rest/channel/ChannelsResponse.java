package net.study.messagesystem.dto.rest.channel;

import net.study.messagesystem.domain.channel.Channel;

import java.util.List;

public record ChannelsResponse(List<Channel> channels) { }
