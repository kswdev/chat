package net.study.messagesystem.repository.connection;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PessimisticLockScope;
import lombok.RequiredArgsConstructor;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.entity.user.connection.QUserConnectionEntity;
import net.study.messagesystem.entity.user.connection.UserConnectionEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static net.study.messagesystem.entity.user.connection.QUserConnectionEntity.*;

@Repository
@RequiredArgsConstructor
public class UserConnectionCustomRepositoryImpl implements UserConnectionCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<UserConnectionEntity> findForUpdateByPartnerUserIdsAndStatus(Long userIdA, Long userIdB, UserConnectionStatus status) {
        QUserConnectionEntity uc = userConnectionEntity;

        JPAQuery<UserConnectionEntity> query = queryFactory
                .select(uc)
                .from(uc)
                .join(uc.partnerAUser).fetchJoin()
                .join(uc.partnerBUser).fetchJoin()
                .where(
                        uc.partnerAUser.userId.eq(userIdA)
                                .and(uc.partnerBUser.userId.eq(userIdB))
                                .and(uc.status.eq(status))
                )
                .setLockMode(LockModeType.PESSIMISTIC_WRITE);

        return Optional.ofNullable(query.fetchOne());

    }
}
