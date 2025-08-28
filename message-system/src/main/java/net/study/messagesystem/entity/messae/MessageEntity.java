package net.study.messagesystem.entity.messae;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;
import net.study.messagesystem.entity.BaseEntity;

import java.util.Objects;

@Getter
@ToString
@Entity
@Table(name = "message")
public class MessageEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_sequence")
    private Long messageSequence;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "content", nullable = false)
    private String content;

    public MessageEntity() {}

    public MessageEntity(Long userId, String content) {
        this.userId = userId;
        this.content = content;
    }

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
