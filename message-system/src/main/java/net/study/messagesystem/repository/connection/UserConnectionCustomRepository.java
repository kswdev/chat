package net.study.messagesystem.repository.connection;

import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.entity.user.connection.UserConnectionEntity;

import java.util.Optional;

public interface UserConnectionCustomRepository {

    Optional<UserConnectionEntity> findForUpdateByPartnerUserIdsAndStatus(Long userIdA, Long userIdB, UserConnectionStatus status);
}
