package net.study.messagesystem.service;

import lombok.Getter;
import net.study.messagesystem.dto.channel.ChannelId;
import net.study.messagesystem.dto.message.Message;
import net.study.messagesystem.dto.message.MessageSeqId;

import java.util.TreeSet;

public class UserService {

    private enum Location {
        LOBBY, CHANNEL
    }

    private Location userLocation = Location.LOBBY;

    @Getter private String username = "";
    @Getter private ChannelId channelId = null;

    private final TreeSet<Message> messageBuffer = new TreeSet<>();

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

    public boolean isInLobby() {
        return userLocation == Location.LOBBY;
    }

    public boolean isInChannel() {
        return userLocation == Location.CHANNEL;
    }

    public void moveToChannel(ChannelId channelId) {
        this.userLocation = Location.CHANNEL;
        this.channelId = channelId;
        setLastReadMessageSeqId(null);
        messageBuffer.clear();
    }

    public void moveToLobby() {
        this.userLocation = Location.LOBBY;
        this.channelId = null;
        setLastReadMessageSeqId(null);
        messageBuffer.clear();
    }

    public synchronized void setLastReadMessageSeqId(MessageSeqId lastReadMessageSeqId) {
        if (getLastReadMessageSeqId() == null ||
            lastReadMessageSeqId == null ||
            getLastReadMessageSeqId().id() < lastReadMessageSeqId.id())
            this.lastReadMessageSeqId = lastReadMessageSeqId;
    }

    public boolean isBufferEmpty() {
        return messageBuffer.isEmpty();
    }

    public int getBufferSize() {
        return messageBuffer.size();
    }

    public Message peekMessage() {
        return messageBuffer.first();
    }

    public Message popMessage() {
        return messageBuffer.pollFirst();
    }

    public void addMessage(Message message) {
        messageBuffer.add(message);
    }
}
