package net.study.messagesocial.application.port.out;

import net.study.messagesocial.domain.userconnectioncount.UserConnectionCount;

public interface LoadUserConnectionCountPort {
    /**
     * userId의 connection_count를 잠근 채로 읽는다. 호출자는 이 결과를 근거로 제한(10명) 검사 후
     * 같은 트랜잭션 안에서 증감 + 저장까지 마쳐야 동시 accept/disconnect 사이의 TOCTOU 레이스가
     * 막힌다. 이 유저의 row가 아직 없으면 count 0인 상태로 새로 만들어서 반환한다 — userId 존재
     * 검증은 호출자(username 조회를 거친 inviter, 인증된 accepter 등)가 이미 끝낸 뒤라고 가정한다.
     */
    UserConnectionCount lockAndLoad(Long userId);
}
