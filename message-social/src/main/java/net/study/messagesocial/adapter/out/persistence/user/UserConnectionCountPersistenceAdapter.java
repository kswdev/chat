package net.study.messagesocial.adapter.out.persistence.user;

import lombok.RequiredArgsConstructor;
import net.study.messagesocial.application.port.out.LoadUserConnectionCountPort;
import net.study.messagesocial.application.port.out.SaveUserConnectionCountPort;
import net.study.messagesocial.domain.userconnectioncount.UserConnectionCount;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserConnectionCountPersistenceAdapter implements LoadUserConnectionCountPort, SaveUserConnectionCountPort {

    private final UserConnectionCountJpaRepository userConnectionCountJpaRepository;

    @Override
    @Transactional
    public UserConnectionCount lockAndLoad(Long userId) {
        return userConnectionCountJpaRepository.findByIdForUpdate(userId)
                .map(entity -> UserConnectionCount.of(entity.getUserId(), entity.getConnectionCount()))
                .orElseGet(() -> UserConnectionCount.create(userId));
    }

    @Override
    @Transactional
    public void save(UserConnectionCount count) {
        UserConnectionCountJpaEntity entity = userConnectionCountJpaRepository.findById(count.getUserId())
                .orElseGet(() -> UserConnectionCountJpaEntity.create(count.getUserId(), 0));
        entity.updateConnectionCount(count.getCount());
        userConnectionCountJpaRepository.save(entity);
    }
}
