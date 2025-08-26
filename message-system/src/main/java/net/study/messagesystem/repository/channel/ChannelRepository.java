package net.study.messagesystem.repository.channel;

import net.study.messagesystem.entity.channel.ChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends JpaRepository<ChannelEntity, Long> {
}
