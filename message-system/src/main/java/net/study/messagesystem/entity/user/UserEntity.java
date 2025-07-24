package net.study.messagesystem.entity.user;

import jakarta.persistence.*;
import lombok.*;
import net.study.messagesystem.entity.BaseEntity;

import java.util.Objects;
import java.util.UUID;

@Getter
@ToString
@Entity
@Table(name = "user")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"userId"}, callSuper = false)
public class UserEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "connection_invite_code", nullable = false)
    private String connectionInviteCode;

    @Setter
    @Column(name = "connection_count", nullable = false)
    private int connectionCount;

    public UserEntity(String username, String password) {
        this.username = username;
        this.password = password;
        this.connectionInviteCode = UUID.randomUUID().toString().replace("-", "");
    }

    public static UserEntity testUser(Long userId) {
        UserEntity user = new UserEntity();
        user.userId = userId;
        return user;
    }
}
