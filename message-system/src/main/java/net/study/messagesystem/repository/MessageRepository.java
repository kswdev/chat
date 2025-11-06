package net.study.messagesystem.repository;

import net.study.messagesystem.dto.projection.MessageInfoProjection;
import net.study.messagesystem.entity.messae.ChannelSequenceId;
import net.study.messagesystem.entity.messae.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, ChannelSequenceId> {

    @Query("SELECT MAX(m.messageSequence) FROM MessageEntity m WHERE m.channelId = :channelId")
    @Transactional(readOnly = true)
    Optional<Long> findLastMessageSequenceByChannelId(Long channelId);

    List<MessageInfoProjection> findByChannelIdAndMessageSequenceBetween(Long channelId, Long startMessageSequence, Long endMessageSequence);
}
