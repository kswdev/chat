package net.study.messagesystem.entity.messae;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.study.messagesystem.entity.BaseEntity;

import java.util.Objects;

@ToString
@Getter
@IdClass(ChannelSequenceId.class)
@AllArgsConstructor
@Entity @Table(name = "message")
public class MessageEntity extends BaseEntity {

    @Id
    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    @Id
    @Column(name = "message_sequence", nullable = false)
    private Long messageSequence;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "content", nullable = false)
    private String content;

    public MessageEntity() {}

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MessageEntity that = (MessageEntity) o;
        return Objects.equals(messageSequence, that.messageSequence);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(messageSequence);
    }
}
