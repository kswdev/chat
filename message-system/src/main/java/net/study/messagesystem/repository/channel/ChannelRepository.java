package net.study.messagesystem.repository.channel;

import jakarta.persistence.LockModeType;
import net.study.messagesystem.dto.projection.ChannelProjection;
import net.study.messagesystem.dto.projection.ChannelTitleProjection;
import net.study.messagesystem.dto.projection.InviteCodeProjection;
import net.study.messagesystem.entity.channel.ChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<ChannelEntity, Long> {

    Optional<ChannelTitleProjection> findChannelTitleByChannelId(Long channelId);
    Optional<InviteCodeProjection> findInviteCodeByChannelId(Long channelId);
    Optional<ChannelProjection> findChannelByInviteCode(String inviteCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ChannelEntity> findForUpdateByChannelId(Long channelId);
}
