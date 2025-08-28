package net.study.messagesystem.repository.channel;

import net.study.messagesystem.dto.projection.UserIdProjection;
import net.study.messagesystem.entity.channel.UserChannelId;
import net.study.messagesystem.entity.channel.UserChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserChannelRepository extends JpaRepository<UserChannelEntity, UserChannelId> {

    boolean existsByUserIdAndChannelId(Long userId, Long channelId);
    List<UserIdProjection> findUserIdsByChannelId(Long channelId);
}
