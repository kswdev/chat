# message-system MSA/DDD 경계 정리 계획

## Context

두 차례의 조사를 통해 `message-system`(메시지 영속화 서비스, 8070)에서 MSA/DDD 관점의 경계 위반을 확인했다:

1. **공유 DB 안티패턴**: message-system이 message-user/message-auth와 동일한 MySQL 인스턴스의 `user` 테이블을 자체 `UserEntity`로 직접 매핑해 읽고(`UserService`), 심지어 `UserConnectionEntity.increaseConnectionCount/decreaseConnectionCount`를 통해 **직접 쓰기(UPDATE)**까지 하고 있다.
2. **인프라 세부사항 누수**: `RedisNotifier`가 message-connection-flux의 pod-naming 규칙(`ws:deliver:{podName}`)을 알고 채널명을 스스로 재조합한다. 두 서비스가 각자 같은 config key(`message-system.redis.channel-prefix`)를 따로 들고 "정확히 일치해야 한다"는 주석으로만 계약을 지킨다.
3. **Kafka 스키마 중복**: message-system의 Kafka DTO(`dto/kafka/*Record.java`)를 message-push가 필드 단위로 손으로 복제해서 쓰고 있어, 필드 변경 시 컴파일 타임이 아닌 런타임 역직렬화 실패로 이어진다.
4. **DDD 바운디드 컨텍스트 과잉**: message-system이 메시징/채널 관리(적절, 응집도 있음) 외에 **친구·소셜 그래프**(초대/수락/거절/연결끊기/연결수 제한)까지 16개 Kafka 핸들러 중 6개로 처리하고 있다. 이는 메시징과 무관한 별도 바운디드 컨텍스트다. 이미 팀에서 이를 인지하고 `message-social` 모듈 스캐폴드(최근 커밋 `e81955f`)를 만들어뒀지만 실제 코드 이전은 시작 전이다.

목표: 위 4가지 문제를 모두 해소하되, 라이브 메시지 송수신 경로(hot path)에 대한 리스크는 최소화하는 순서로 진행한다. 아래 순서는 "무엇이 무엇을 단순화/차단하는지"를 기준으로 정했다.

**사용자 확정 사항**:
- 요청 경로: message-social은 message-connection-flux/Kafka로부터 요청을 받지 않는다. 클라이언트(웹/CLI)가 web-gateway를 거쳐 message-social에 **직접 HTTP 요청**을 보낸다 (`/api/v1/social/**`, message-auth/message-user와 동일한 REST 라우팅 패턴). 응답은 동기 HTTP 응답으로 즉시 반환하고, 상대방(제3자)에게 필요한 실시간 알림(초대/수락 알림)만 기존 Redis Streams 배선(`ClientNotificationService`→`RedisNotifier`)을 그대로 재사용해 WS로 푸시한다.
- 제3자 user 조회: **이벤트 기반 로컬 read 캐시** (message-user가 프로필 이벤트 발행 → message-system/message-social이 구독해 최소 로컬 테이블 유지).
- message-social DB: **완전히 별도의 MySQL 인스턴스**로 구성 (기존 컨테이너에 스키마만 얹는 방식 아님).

---

## Phase 0 — RedisNotifier: pod-naming 결합 제거 (가장 먼저, 격리된 quick win)

**근거**: `PodIdentity.getDeliveryChannel()`이 이미 존재하지만 미사용 상태. `SessionServiceImpl.setOnline`은 `podIdentity.getPodName()`(bare pod name)만 Redis `USER_SESSION:{userId}`에 저장하고, message-system의 `RedisNotifier.publish(podName, ...)`가 `channelPrefix + podName`을 스스로 재조합해 채널명을 만든다 (직접 코드 확인 완료).

**변경**:
- `message-connection-flux/.../application/service/SessionServiceImpl.java`: `setOnline`이 `podIdentity.getDeliveryChannel()`(완성된 채널 문자열)을 저장하도록 변경.
- `message-system/.../service/RedisNotifier.java`: `publish(String deliveryChannel, RecordInterface record)`로 시그니처 변경, 전달받은 문자열을 그대로 스트림 키로 사용. `channelPrefix` `@Value` 필드와 포맷팅 로직, 상단 "PodIdentity와 정확히 일치해야 함" 주석 제거.
- `message-system/.../service/ClientNotificationService.java`: `podName` 파라미터명을 `deliveryChannel`로 정정 (더 이상 pod name이 아님).
- `message-system/src/main/resources/application.yml`: `message-system.redis.channel-prefix` 제거 (message-connection-flux는 자체 사본 유지).

**검증**: 두 사용자를 같은 채널에 연결 후 메시지 송수신 E2E 스모크 테스트. `RedisNotifier.publish`가 인자를 그대로 스트림 키로 쓰는지 단위 테스트.

---

## Phase 2 — 친구/소셜 도메인을 message-social로 이전

이 단계가 실제로 "공유 DB에 쓰기"를 멈추는 핵심 단계다.

### 2a. 이관 대상
- invite 도메인도 따로 만들기(유저 생성 이벤트 받아서 따로 생성)
`net.study.messagesystem` → `net.study.messagesocial` 패키지로 이동:
- `entity/user/connection/UserConnectionEntity.java`, `UserConnectionId.java`
- `repository/connection/UserConnectionRepository.java` 및 QueryDSL 커스텀 구현체
- `service/UserConnectionService.java`, `UserConnectionLimitService.java`
- 핸들러 6개(`net.study.messagesystem.handler.kafka.*RequestRecordHandler`) → `SocialController`의 REST 엔드포인트 메서드로 재작성. 원본 핸들러의 도메인 로직(`userConnectionService.invite/accept/reject/disconnect/getUsersByStatus`, `userService.getInviteCode` 호출부)만 그대로 옮기고, 본인에게 보내던 응답은 HTTP 응답 바디로 동기 반환한다 (Phase 2e 참고).
- 인프라 배선(message-push 방식과 동일하게 각 서비스가 자체 사본 보유): `CacheService`, `SessionService`(읽기 전용), `RedisNotifier`(Phase 0 적용된 상태로 새로 작성), `PushService`, `ClientNotificationService`, `JsonUtil`. Kafka 인바운드 디스패치 인프라(`KafkaConfig`/`RecordDispatcher`/`BaseRecordHandler`)는 message-social에는 불필요 — Spring MVC `@RestController`가 인바운드 진입점 역할을 한다.
- Kafka DTO 중 **`InviteNotificationRecord`/`AcceptNotificationRecord`만** message-common에서 import (상대방에게 보내는 실시간 알림 2종, Phase 1에서 이미 이동됨). 나머지 `Invite/Accept/Reject/Disconnect/FetchConnections/FetchUserInviteCode`의 Request/Response Record 및 이 6개 흐름에 쓰이던 `ErrorResponseRecord` 용례는 HTTP 동기 응답/에러로 대체되어 신규 작성 불필요 — message-system에서 삭제 시 다른 소비자가 없는지 확인 후 폐기 (Phase 2e).

**새로 작성 (그대로 이전 금지)**: `entity/user/UserEntity.java`를 message-social에 만들 때 `password`, `connectionCount` 필드는 포함하지 않는다. `userId`, `username`, `inviteCode`만 갖는 최소 read 모델로 재작성 — 그리고 이 read 모델은 Phase 3에서 로컬 이벤트 캐시로 대체될 임시 구조임을 명시.

### 2b. JPQL JOIN 재작성 (놓치기 쉬운 포인트)
현재 `UserConnectionRepository.findByPartnerAUser_userIdAndStatus` 등은 `user_connection`과 `user`를 한 JPQL로 JOIN한다 — 같은 스키마에 있을 때만 가능한 쿼리다. `user_connection`이 message-social의 별도 DB로 이동하면 이 JOIN은 더 이상 성립하지 않는다. `user_connection` 단독 조회 후, 파트너 userId들에 대해 message-social의 `UserService`로 username을 별도 조회하는 2단계 방식으로 재작성 필요. 별도 단위 테스트로 검증.

### 2c. DB 토폴로지 — 완전히 별도의 MySQL 인스턴스
- `docker-compose.yaml`에 message-social 전용 MySQL 컨테이너(source/replica 필요 여부는 트래픽 규모상 source 단일로 충분, 포트 할당(예: `13312`/`13313`), `docker/.env`에 반영.
- `docker/init-scripts-social-source/` 등 스키마 초기화 스크립트 추가 — `user_connection` 테이블 정의만 포함.
- message-social의 `DataSourceConfig`는 message-system 것보다 단순(샤딩 불필요, source/replica만).
- `message-system/src/main/resources/schema.sql`에서 `user_connection` 정의 제거.

### 2d. connectionCount 재설계
- `UserConnectionEntity.connect()/disconnect()`에서 `increaseConnectionCount()/decreaseConnectionCount()`, `checkIfConnectionReachedLimit()/checkIfConnectionReachedZero()` 제거 — 엔티티가 더 이상 `UserEntity`의 가변 상태에 의존하지 않게 함.
- 연결 수 제한 체크는 `UserConnectionLimitService.connect()/disconnect()`에서 `user_connection` 테이블에 대한 `COUNT` 쿼리로 즉석 계산 (denormalized counter 대신). 기존 에러 메시지("Connection limit reached by other user" / "Connection limit reached") 의미 유지.
- 확인됨: `connection_count` 컬럼은 message-user/message-auth 어디에서도 읽히지 않음(죽은 매핑) — 쓰기를 중단해도 다른 서비스에 영향 없음. 컬럼 자체 제거는 Phase 4에서 선택적으로 처리.

### 2e. 요청 경로 — web-gateway 경유 HTTP REST

클라이언트(웹/CLI)가 message-connection-flux의 WebSocket을 거치지 않고, **web-gateway → message-social**로 직접 HTTP 요청을 보낸다. message-auth(`/api/v1/auth/**`)·message-user(`/api/v1/user/**`)와 동일한 REST 라우팅 패턴이며, 응답도 동기 HTTP 응답으로 즉시 반환한다. 단, 초대/수락 시 "상대방(제3자)"에게 보내는 실시간 알림만은 기존 배선(Phase 0 적용된 Redis Streams)을 그대로 재사용해 WS로 푸시한다.

**web-gateway** (`web-gateway/src/main/resources/application.yaml`, `spring.cloud.gateway.routes` 아래, 기존 `message-user-unregister`/`websocket-route-message` 라우트와 동일 패턴):
```yaml
- id: message-social
  uri: http://message-social.chat-system.svc.cluster.local:8087/
  predicates:
    - Path=/api/v1/social/**
  filters:
    - AuthorizationHeaderFilter
```
`/api/v1/user/**`처럼 엔드포인트별로 쪼개지 않고 통짜 라우트로 구성 — 소셜 도메인의 6개 엔드포인트 전부 인증이 필요하므로 굳이 나눌 이유가 없다. `AuthorizationHeaderFilter`가 JWT를 검증하고 `USER_ID`/`X-Authorization-Role` 헤더를 주입하는 것은 기존과 동일.

**message-social — `SocialController`** (`@RestController @RequestMapping("/api/v1/social")`, `net.study.messagesocial.adapter.in.web`): `UserController.unregister`(message-user)와 동일하게 `HttpServletRequest`에서 `IdKey.USER_ID.getValue()` 헤더로 본인 식별. 6개 엔드포인트:
- `POST /invite` `{userInviteCode}` — `userConnectionService.invite(...)` 호출 후 결과를 동기 응답으로 반환. 성공 시 `sessionService.getListenTopic(inviteeUserId)`로 온라인 여부 확인 → 온라인이면 `redisNotifier.publish(topic, new InviteNotificationRecord(...))`, 오프라인이면 `pushService.pushMessage(...)`.
- `POST /accept` `{username}` — 동기 응답 + 성공 시 초대자에게 `AcceptNotificationRecord` 푸시 (위와 동일한 온라인/오프라인 분기).
- `POST /reject` `{username}` — 동기 응답만 (기존 `RejectRequestRecordHandler`도 상대방 알림이 없었음 — 동작 유지).
- `POST /disconnect` `{username}` — 동기 응답만 (기존과 동일, 상대방 알림 없음).
- `GET /connections?status=` — 동기 응답만.
- `GET /invite-code` — 동기 응답만.
- 에러 처리: 도메인 예외를 `@ExceptionHandler`(또는 try/catch)로 잡아 HTTP 4xx + JSON 에러 바디로 응답 (연결수 제한 초과, 초대코드 불일치, 이미 연결됨 등 — 기존 에러 메시지 문구 유지).

**message-common** — `InviteNotificationRecord`/`AcceptNotificationRecord`(+`RecordInterface`)만 이 도메인용으로 남는다. `Invite/Accept/Reject/Disconnect/FetchConnections/FetchUserInviteCode`의 Request/Response Record는 message-system 핸들러 삭제 시 다른 소비자가 없는지 확인 후 함께 삭제.

**message-connection-flux** — 클라이언트가 더 이상 이 6개 액션을 WS로 보내지 않으므로:
- `adpter/in/websocket/request/`의 `InviteRequestHandler`, `AcceptRequestHandler`, `RejectRequestHandler`, `DisconnectRequestHandler`, `FetchConnectionsRequestHandler`, `FetchUserInviteCodeRequestHandler` 삭제.
- 대응하는 `application/dto/websocket/inbound/`의 `InviteRequest`, `AcceptRequest`, `RejectRequest`, `DisconnectRequest`, `FetchUserConnectionsRequest`, `FetchUserInviteCodeRequest` 클래스 삭제, `BaseRequest.java`의 `@JsonSubTypes`에서 해당 6개 항목(`MessageType.INVITE_REQUEST` 등, 현재 12~18행) 제거 — 채널/메시지 관련 항목은 그대로 둠.
- 유지: `adpter/in/kafka/`의 `InviteNotificationRecordHandler`/`AcceptNotificationRecordHandler`(이름은 `kafka` 패키지지만 실제로는 Redis Streams 구독 기반, Phase 0 참고) — 프로듀서만 message-system→message-social로 바뀔 뿐 수신 측 로직은 동일. 더 이상 아무도 만들지 않는 `Reject/Disconnect/FetchConnections/FetchUserInviteCode`의 Response 핸들러(있다면)는 삭제.
- `EventProducer`/`KafkaProducer.sendRequest`는 변경 없음 — 채널/메시지 WS 흐름만 계속 `message-request`로 나간다.

**message-system** — 6개 핸들러·서비스·엔티티·레포지토리는 Phase 2a에서 이미 이관 대상으로 삭제. `RecordDispatcher`가 클래스 타입 기준으로 핸들러를 매핑하므로(토픽별 명시적 등록이 아님) 핸들러 클래스 삭제만으로 충분하고 별도 `@KafkaListener`/토픽 배선 변경은 불필요.

**검증**: `UserConnectionLimitService`의 연결수 10명 제한 경계값 단위 테스트(초대 수락 시 도달, 연결 해제 시 0 도달). `curl -H "Authorization: Bearer <JWT>" http://localhost:8080/api/v1/social/invite-code`로 게이트웨이 라우팅 스모크 테스트. 두 클라이언트로 초대→(상대방 WS로 `ASK_INVITE` 푸시 수신 확인)→수락→(초대자 WS로 `NOTIFY_ACCEPT` 푸시 수신 확인)→메시지 전송→연결끊기 E2E 스모크 테스트, 상대방이 오프라인일 때 push-notification 경유 폴백도 확인. `docker exec`로 새 social DB에 `user_connection` 쓰기가 실제로 들어가는지, 기존 공유 DB의 `user.connection_count`가 더 이상 변경되지 않는지 확인.

---

### 2f. 클라이언트 마이그레이션 — message-front / message-client

요청 경로가 WS가 아닌 HTTP이므로 두 클라이언트 모두 호출 지점을 이 방식으로 구현한다.

**message-front** (React + TS):
- `src/api/socialApi.ts` 신규 작성 — 기존 `src/api/authApi.ts`와 동일 패턴(공유 `axiosInstance`가 `sessionStorage`의 토큰을 자동으로 `Authorization` 헤더에 부착)으로 `/api/v1/social/invite`, `/accept`, `/reject`, `/disconnect`, `/connections`, `/invite-code` 6개 함수 작성.
- `src/contexts/ChatContext.tsx`: 6개 액션 함수(`inviteUser`/`acceptUser`/`rejectUser`/`disconnectUser`/`fetchAcceptedConnections`/`fetchPendingConnections`/`fetchUserInviteCode`, 현재 L615-653)가 새 `socialApi` 함수를 호출하고, 프로미스 resolve 결과로 리듀서 상태를 직접 갱신하도록 구현. WS 핸들러 중 6개 `*_RESPONSE` 케이스(`INVITE_RESPONSE` 등, 현재 L408-472)는 제거 대상(HTTP 응답으로 대체되어 더 이상 오지 않음). `ASK_INVITE`/`NOTIFY_ACCEPT`(L475-496, 상대방 실시간 알림)는 서버 푸시이므로 그대로 유지하되, 내부에서 재조회를 위해 `send({type:'FETCH_USER_CONNECTIONS_REQUEST'})`를 호출하던 부분(L427, L483, L494)은 새 `socialApi` 함수 호출로 구현.
- `src/types/index.ts`의 관련 요청/응답 인터페이스(L100-127, L201-242)는 WS discriminated union이 아니라 axios 함수의 인자/반환 타입으로 정의.
- `PendingInviteList.tsx`, `ConnectionList.tsx`, `Sidebar.tsx`는 context가 제공하는 함수 시그니처가 동일하게 유지되므로 호출부 자체는 변경 없이 그대로 사용 가능 — 회귀 여부만 확인.

**message-client** (Java CLI):
- `RestApiService.java`에 기존 `register`/`login`/`unregister`/`logout` 패턴(JDK `HttpClient`, `Authorization: Bearer {sessionId}` 헤더)과 동일하게 `invite`/`accept`/`reject`/`disconnect`/`fetchUserConnections`/`fetchUserInviteCode` 메서드 추가.
- `CommandHandler.java`의 해당 커맨드 메서드(`inviteCode`/`connections`/`pending`/`invite`/`accept`/`reject`/`disconnect`, 현재 L112-220 부근)가 `webSocketService.sendMessage(...)` 대신 새 `RestApiService` 메서드를 동기 호출하도록 구현. 응답 출력 로직은 기존 `ResponseDispatcher`의 `invite/accept/reject/disconnect/fetchConnections/fetchUserInviteCode` 메서드(현재 L91-142 부근) 내용을 그대로 재사용해 `CommandHandler`에서 직접 호출.
- `ResponseDispatcher.java`에서 6개 `*_REQUEST`/`*_RESPONSE` 관련 핸들러 등록(현재 L40-43, L53-54) 제거. `ASK_INVITE`(L36)/`NOTIFY_ACCEPT`(L46)는 서버 푸시이므로 유지.
- `dto/websocket/outbound/`의 `InviteRequest`, `AcceptRequest`, `RejectRequest`, `DisconnectRequest`, `FetchUserConnectionsRequest`, `FetchUserInviteCodeRequest`와 `dto/websocket/inbound/`의 대응 `*Response` 6종 삭제. `InviteNotification`/`AcceptNotification`(`ASK_INVITE`/`NOTIFY_ACCEPT`)은 유지.

**검증**: message-front에서 초대 코드 발급→다른 브라우저 세션에서 초대→수락(양쪽 실시간 알림/목록 갱신 확인)→연결끊기 수동 QA. message-client CLI로 동일 플로우 수동 QA(`invite`/`accept`/`reject`/`disconnect`/`connections`/`pending`/`inviteCode` 커맨드).

---

## Phase 3 — 제3자 username 조회 탈피 (이벤트 기반 로컬 캐시)

호출 지점을 두 그룹으로 나눠서 처리한다:

**A. 본인(self) 조회 — JWT 클레임 임베딩** (별도 인프라 불필요, 항상 진행):
- `MessageService.sendMessageToParticipants`의 `getUsername(senderUserId)`, `UserConnectionService`의 초대자/수락자 자기 이름 조회는 모두 "현재 요청 주체 본인"의 이름 — 조회가 아니라 요청에 실려오는 값으로 대체 가능.
- message-auth가 JWT 발급 시 `username` 클레임 추가 → web-gateway가 헤더로 전달 → message-connection-flux가 `WriteMessageRecord` 등 생성 시 본인 이름을 실어 보냄. `message-common`의 해당 Record에 `username` 필드 추가(컴파일 타임 체크됨).

**B. 제3자(arbitrary) 조회 — 이벤트 기반 로컬 read 캐시**:
- 대상: `MessageService.getMessages`(메시지 이력의 발신자 목록 일괄 조회), `UserConnectionService.invite/accept`(초대코드/유저명으로 임의 상대방 조회).
- message-user가 `UserRegistered`/`UserProfileChanged` 이벤트를 신규 토픽(예: `user-profile-events`)에 발행.
- message-system과 message-social이 각각 이 토픽을 구독해 `userId, username, inviteCode`만 갖는 최소 로컬 read 테이블을 유지.
- 기존 사용자에 대한 1회성 백필 작업 필요(이벤트 스트림은 프로듀서 도입 이후 발생 건만 커버하므로, 기존 `user` 테이블 스냅샷을 초기 적재).
- 완료 후 message-system/message-social의 `UserRepository`(공유 DB 대상) 삭제.

**변경 파일**:
- `message-auth`: JWT 발급 로직에 `username` 클레임 추가.
- `web-gateway`: `AuthorizationHeaderFilter`/`JwtToTokenUserConverter`에서 username 헤더 전달.
- `message-connection-flux`: 아웃바운드 Record 생성 시 본인 username 첨부.
- `message-common`: `WriteMessageRecord` 등에 `username` 필드 추가.
- `message-user`: 신규 Kafka 프로듀서(`UserRegistered`/`UserProfileChanged`) + 공유 이벤트 DTO.
- `message-system`, `message-social`: 신규 로컬 read 테이블 + 컨슈머, 기존 `UserRepository` 교체.
- `docker/prepare_topics.sh`: 프로필 이벤트 토픽 추가.

**검증**: Tier A는 발신자 이름이 조회 없이 정확히 전달되는지 확인 + `getUsername` 호출량 감소 확인. Tier B는 컨슈머 단위 테스트, 백필 스크립트를 기존 `user` 테이블 건수와 대조, 이력 조회/초대 플로우 스모크 테스트(최근 변경된 username 케이스 포함해 지연 허용 범위 확인).

---

## Phase 4 — 정리 및 폐기

- message-system: `entity/user/UserEntity.java`, `UserRepository.java`, `entity/user/connection/*` 삭제 확인 (Phase 2/3에서 이미 이관/대체됨).
- message-system의 기존 공유 DB용 `source`/`replica` DataSource가 더 이상 필요 없다면 `DataSourceConfig.java`/`application.yml`에서 제거.
- 선택: `connection_count` 컬럼을 message-system/message-user/message-auth 세 곳의 `UserEntity`에서 모두 제거 (안 읽는 것 확인됨, 필수는 아님).
- `CLAUDE.md` 업데이트: 모듈 목록에 message-social 추가, 요청 흐름 다이어그램에 `web-gateway → message-social`(`/api/v1/social/**`, HTTP) 직접 경로와 Phase 0 이후의 정확한 딜리버리 채널(Redis Streams) 설명 반영, 서비스 포트 표에 message-social(8087) 추가, Kafka 토픽 목록에 `user-profile-events` 추가.
- `docker/prepare_topics.sh`, `docker-compose.yaml`, `.env` 최종 정리.

**검증**: 전 모듈 `./gradlew build`. 회원가입→로그인→WS연결→메시지 송수신→친구초대/수락→연결끊기→채널/메시지 이력 조회까지 전체 플로우 1회 E2E 스모크 테스트.

---

## 실행 순서 요약

| Phase | 내용 | 리스크 | 선행조건 |
|---|---|---|---|
| 0 | RedisNotifier pod-naming 결합 제거 | 낮음 (격리) | 없음 |
| 1 | Kafka DTO를 message-common으로 이동 | 낮음 (순수 이동) | 없음 |
| 2a-d | 친구/소셜 도메인 → message-social 이전 (엔티티/서비스/DB) | 중간 (신규 서비스+DB) | Phase 1 |
| 2e | message-social REST API + web-gateway 라우팅 | 중간 (신규 라우트+계약) | 2a-d |
| 2f | message-front/message-client 호출 지점을 HTTP로 구현 | 중간 (양쪽 클라이언트) | 2e |
| 3 | 제3자 username 조회 → 이벤트 기반 캐시 | 높음 (cross-team, 백필) | Phase 2 권장(범위 명확화) |
| 4 | 정리/폐기 | 낮음 | Phase 0~3 |
