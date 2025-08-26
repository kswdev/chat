package net.study.messagesystem.repository.channel;

import net.study.messagesystem.entity.channel.ChannelId;
import net.study.messagesystem.entity.channel.UserChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserChannelRepository extends JpaRepository<UserChannelEntity, ChannelId> {
}
