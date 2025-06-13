package net.study.messagesystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@Getter
@ToString
@Entity
@Table(name = "message")
public class MessageEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_sequence")
    private Long messageSequence;

    @Column(name = "user_name", nullable = false)
    private String username;

    @Column(name = "content", nullable = false)
    private String content;

    public MessageEntity() {}

    public MessageEntity(String username, String content) {
        this.username = username;
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
