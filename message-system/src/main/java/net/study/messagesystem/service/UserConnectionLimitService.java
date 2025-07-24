package net.study.messagesystem.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.dto.user.UserId;
import net.study.messagesystem.entity.user.connection.UserConnectionEntity;
import net.study.messagesystem.repository.UserConnectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserConnectionLimitService {

    private final UserConnectionRepository userConnectionRepository;

    @Transactional
    public void connect(UserId accepterUserId, UserId inviterUserId) {
        Long firstUserId = Math.min(accepterUserId.id(), inviterUserId.id());
        Long secondUserId = Math.max(accepterUserId.id(), inviterUserId.id());

        UserConnectionEntity connectionEntity = userConnectionRepository
                .findByPartnerAUser_userIdAndPartnerBUser_userIdAndStatus(firstUserId, secondUserId, UserConnectionStatus.PENDING)
                .orElseThrow(() -> new EntityNotFoundException("Invalid status"));

        connectionEntity.connect();
    }
}
