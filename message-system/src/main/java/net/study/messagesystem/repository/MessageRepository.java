package net.study.messagesystem.repository;

import net.study.messagesystem.entity.messae.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    @Transactional(readOnly = true)
    Optional<MessageEntity> findTopByOrderByMessageSequenceDesc();
}
