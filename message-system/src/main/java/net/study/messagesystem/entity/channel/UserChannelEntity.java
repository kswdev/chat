package net.study.messagesystem.entity.channel;

import jakarta.persistence.*;
import lombok.*;
import net.study.messagesystem.entity.BaseEntity;

@Getter
@Entity @Table(name = "user_connection")
@IdClass(UserChannelId.class)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"channelId", "userId"}, callSuper = false)
public class UserChannelEntity extends BaseEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    @Column(name = "inviter_user_id", nullable = false)
    private Long last_read_msg_seq;
}
