package net.study.messagesystem.entity.channel;

import jakarta.persistence.*;
import lombok.*;
import net.study.messagesystem.entity.BaseEntity;

import java.util.UUID;

@Getter
@ToString
@Entity
@Table(name = "channel")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"channelId"}, callSuper = false)
public class ChannelEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "channel_id")
    private Long channelId;

    @Column(nullable = false)
    private String title;

    @Column(name = "channel_invite_code", nullable = false)
    private String channelInviteCode;

    @Setter
    @Column(name = "head_count", nullable = false)
    private int headCount;

    public ChannelEntity(String title, int headCount) {
        this.title = title;
        this.headCount = headCount;
        this.channelInviteCode = UUID.randomUUID().toString().replace("-", "");
    }
}
