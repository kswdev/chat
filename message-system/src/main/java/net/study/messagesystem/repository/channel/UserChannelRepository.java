package net.study.messagesystem.repository.channel;

import io.lettuce.core.dynamic.annotation.Param;
import net.study.messagesystem.dto.projection.ChannelProjection;
import net.study.messagesystem.dto.projection.LastReadMsgSeqProjection;
import net.study.messagesystem.dto.projection.UserIdProjection;
import net.study.messagesystem.entity.channel.UserChannelEntity;
import net.study.messagesystem.entity.channel.UserChannelId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserChannelRepository extends JpaRepository<UserChannelEntity, UserChannelId> {

    boolean existsByUserIdAndChannelId(Long userId, Long channelId);
    void deleteByUserIdAndChannelId(Long userId, Long channelId);

    Optional<LastReadMsgSeqProjection> findLastReadMsgSeqByUserIdAndChannelId(Long userId, Long channelId);

    List<UserIdProjection> findUserIdsByChannelId(Long channelId);

    @Query("""
            SELECT c.channelId AS channelId, c.title AS title, c.headCount AS headCount
              FROM UserChannelEntity uc
        INNER JOIN ChannelEntity c ON uc.channelId = c.channelId
             WHERE uc.userId = :userId
    """)
    List<ChannelProjection> findChannelsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("""
            UPDATE UserChannelEntity uc
               SET uc.lastReadMsgSeq = :lastReadMsgSeq
             WHERE uc.userId = :userId
               AND uc.channelId = :channelId
               AND uc.lastReadMsgSeq < :lastReadMsgSeq
    """)
    int updateLastReadMsgSeqByUserIdAndChannelId(
            @Param("userId") Long userId,
            @Param("userId") Long channelId,
            @Param("userId") Long lastReadMsgSeq);
}
