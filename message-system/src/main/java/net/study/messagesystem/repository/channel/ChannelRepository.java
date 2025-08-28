package net.study.messagesystem.repository.channel;

import net.study.messagesystem.dto.projection.ChannelTitleProjection;
import net.study.messagesystem.entity.channel.ChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<ChannelEntity, Long> {

    Optional<ChannelTitleProjection> findChannelTitleByChannelId(Long channelId);
}
