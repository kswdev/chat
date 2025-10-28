package net.study.messagesystem.service;

import lombok.Getter;
import net.study.messagesystem.dto.channel.ChannelId;
import net.study.messagesystem.dto.message.MessageSeqId;

public class UserService {

    private enum Location {
        LOBBY, CHANNEL
    }

    private Location userLocation = Location.LOBBY;

    @Getter private String username = "";
    @Getter private ChannelId channelId = null;

    @Getter
    private volatile MessageSeqId lastReadMessageSeqId = null;

    public void login(String username) {
        this.username = username;
        moveToLobby();
    }

    public void logout() {
        this.username = "";
        moveToLobby();
    }

    public void moveToChannel(ChannelId channelId) {
        this.userLocation = Location.CHANNEL;
        this.channelId = channelId;
        setLastReadMessageSeqId(null);
    }

    public void moveToLobby() {
        this.userLocation = Location.LOBBY;
        this.channelId = null;
        setLastReadMessageSeqId(null);
    }

    public boolean isInLobby() {
        return userLocation == Location.LOBBY;
    }

    public boolean isInChannel() {
        return userLocation == Location.CHANNEL;
    }


    public synchronized void setLastReadMessageSeqId(MessageSeqId lastReadMessageSeqId) {
        if (getLastReadMessageSeqId() == null ||
            lastReadMessageSeqId == null ||
            getLastReadMessageSeqId().id() < lastReadMessageSeqId.id())
            this.lastReadMessageSeqId = lastReadMessageSeqId;
    }
}
