package net.study.messagesystem.entity.messae;

import lombok.*;

import java.io.Serializable;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"channelId", "messageSequence"})
public class ChannelSequenceId implements Serializable{

    private Long channelId;
    private Long messageSequence;
}

