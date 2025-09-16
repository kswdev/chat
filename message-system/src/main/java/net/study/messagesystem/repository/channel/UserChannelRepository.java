package net.study.messagesystem.repository.channel;

import net.study.messagesystem.dto.projection.ChannelProjection;
import net.study.messagesystem.dto.projection.UserIdProjection;
import net.study.messagesystem.entity.channel.UserChannelEntity;
import net.study.messagesystem.entity.channel.UserChannelId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserChannelRepository extends JpaRepository<UserChannelEntity, UserChannelId> {

    boolean existsByUserIdAndChannelId(Long userId, Long channelId);
    void deleteByUserIdAndChannelId(Long userId, Long channelId);

    List<UserIdProjection> findUserIdsByChannelId(Long channelId);

    @Query("""
            SELECT c.channelId AS channelId, c.title AS title, c.headCount AS headCount
              FROM UserChannelEntity uc
        INNER JOIN ChannelEntity c ON uc.channelId = c.channelId
             WHERE uc.userId = :userId
    """)
    List<ChannelProjection> findChannelsByUserId(Long userId);
}
