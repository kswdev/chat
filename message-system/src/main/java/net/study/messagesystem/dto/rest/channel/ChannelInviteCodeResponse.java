package net.study.messagesystem.dto.rest.channel;

import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.user.InviteCode;

public record ChannelInviteCodeResponse(ChannelId channelId, InviteCode inviteCode) { }
