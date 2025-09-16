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

    @Column(name = "invite_code", nullable = false)
    private String inviteCode;

    @Setter
    @Column(name = "head_count", nullable = false)
    private int headCount;

    @Transient
    public static final int LIMIT_HEAD_COUNT = 10;

    public static ChannelEntity create(String title, int headCount) {
        checkHeadCount(headCount);
        return new ChannelEntity(title, headCount);
    }

    public void increaseHeadCount() {
        checkHeadCount(this.headCount + 1);
        this.headCount++;
    }

    public void decreaseHeadCount() {
        checkHeadCount(this.headCount - 1);
        this.headCount--;
    }

    private ChannelEntity(String title, int headCount) {
        this.title = title;
        this.headCount = headCount;
        this.inviteCode = UUID.randomUUID().toString().replace("-", "");
    }

    private static void checkHeadCount(int headCount) {
        if (headCount > LIMIT_HEAD_COUNT)
            throw new IllegalStateException("headCount limit reached already");
        else if (headCount < 0)
            throw new IllegalStateException("headCount already zero");
    }
}
