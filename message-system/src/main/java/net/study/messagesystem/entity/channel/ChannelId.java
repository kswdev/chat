package net.study.messagesystem.entity.channel;

import lombok.*;

import java.io.Serializable;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"userId", "channelId"})
public class ChannelId implements Serializable{

    private Long userId;
    private Long channelId;
}

