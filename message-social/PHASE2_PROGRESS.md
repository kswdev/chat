# Phase 2 진행 기록 — message-social 친구/소셜 도메인

`MESSAGE_SYSTEM_MSA_PLAN.md`의 Phase 2 작업 기록이다. `invite` API(도메인 로직 + 단위 테스트)는
작업 시작 전에 이미 완료되어 있었고, 이번 작업은 그 위에 나머지 5개 엔드포인트(accept/reject/
disconnect/connections/invite-code)의 로직 설계, 단위 테스트, 영속성 계층, 알림 배선, 통합 테스트를
추가했다.

## 손대지 않은 것

`FriendInviteService`와 `FriendInviteServiceTest`, `UserConnection` 도메인 클래스는 **전혀 수정하지
않았다.** 이미 검증된 코드이고, 생성자 시그니처를 바꾸면 `@InjectMocks` 기반 테스트가 깨지기 때문이다.
초대 성공 후 알림 발송도 `FriendInviteService` 내부가 아니라 컨트롤러 계층(`FriendNotificationService`)
에서 별도로 처리하도록 설계했다 — 같은 이유.

## 이번에 구현한 것

### 1. 나머지 5개 엔드포인트 (도메인 로직 + 단위 테스트)
`invite`와 동일한 헥사고날 패턴(port/in 유스케이스 인터페이스 + 전용 서비스 구현체)으로 추가:

- `FriendAccept` / `FriendAcceptService` — PENDING 상태 검증 + 연결 수 제한(10명) 체크 후 ACCEPTED로 전이
- `FriendReject` / `FriendRejectService` — PENDING 상태 검증 후 REJECTED로 전이
- `FriendDisconnect` / `FriendDisconnectService` — ACCEPTED 상태 검증 후 DISCONNECTED로 전이. 최초
  inviter/invitee 중 누가 끊어도 되도록 양방향으로 연결을 조회한다.
- `FriendConnectionQuery` / `FriendConnectionQueryService` — 상태별 연결 목록 조회
- `FriendInviteCodeQuery` / `FriendInviteCodeQueryService` — 본인 초대 코드 조회

각 서비스마다 대응하는 Mockito 단위 테스트 작성(`*ServiceTest`), 정상/실패 케이스와 예외 타입을 검증한다.

### 2. 연결 수 제한 (계획 2d)
기존 message-system처럼 `UserEntity`에 `connection_count`를 비정규화해서 증감시키는 대신, `user_connection`
테이블에 대한 COUNT 쿼리로 즉석 계산하도록 설계했다(`UserConnectionJpaRepository.countByUserIdAndStatus`).
`UserConnectionCount` 도메인 객체에 `LIMIT`(10), `isAtLimit()`, DB 조회값을 담기 위한 `of(userId, count)`
팩토리를 추가했다. accept 시 accepter/inviter 양쪽의 제한을 각각 체크하고, 어느 쪽이 초과인지에 따라
다른 에러 메시지(`CONNECTION_LIMIT_REACHED` / `CONNECTION_LIMIT_REACHED_BY_PARTNER`)를 반환한다.

### 3. 버그 수정: `FriendErrorController`의 예외 타입 불일치
`@ExceptionHandler({InvalidInviteCodeException.class, AlreadyInvitedException.class, AlreadyConnectedException.class})`
가 등록돼 있었는데 핸들러 메서드 파라미터 타입은 `InvalidInviteCodeException` 하나였다. 세 예외 클래스가
서로 형제 관계(모두 `RuntimeException` 직속)라 `AlreadyInvitedException`/`AlreadyConnectedException`이
실제로 발생하면 파라미터 바인딩에 실패해 500으로 새 나갈 수 있는 버그였다. 공통 추상 클래스
`FriendException`을 추가하고 기존 3개 + 신규 4개 예외가 모두 이를 상속하도록 정리, 핸들러도
`@ExceptionHandler(FriendException.class)` 하나로 단순화했다.

새로 추가한 예외: `UserNotFoundException`(404), `ConnectionNotFoundException`(404),
`InvalidConnectionStatusException`(400), `LimitExceededException`(400).

### 4. 영속성 계층 (JPA)
포트만 있고 어댑터가 없던 상태였어서 통합 테스트를 위해 추가했다:

- `UserConnectionJpaEntity` / `UserConnectionJpaRepository` / `UserConnectionPersistenceAdapter`
  — `SaveFriendPort`, `LoadUserConnectionPort`, `LoadUserConnectionCountPort` 3개를 한 어댑터가 구현
- `SocialUserJpaEntity` / `SocialUserJpaRepository` / `UserPersistenceAdapter`(`LoadUserPort` 구현)
  — 계획 2a가 요구한 "userId/username/inviteCode만 갖는 최소 read 모델". **이 테이블은 지금 아무도 채우지
  않는다** — Phase 3에서 message-user의 프로필 이벤트를 구독해 채우기 전까지는 빈 테이블이다. 통합
  테스트에서는 직접 `save()`로 시드 데이터를 넣어 검증했다.

### 5. Redis/Kafka 알림 배선 (단위 테스트만, 지시사항대로)
message-system의 `RedisNotifier`/`SessionService`/`PushService`/`ClientNotificationService`/`KafkaProducer`
패턴을 `adapter/out/notification` 패키지에 복제했다(계획 2a의 "각 서비스가 자체 사본 보유" 방침과 동일).
차이점:
- `SessionService`는 읽기 전용(`getListenTopic`만) — 세션 기록은 여전히 message-connection-flux 소관.
- `InviteNotificationRecord`/`AcceptNotificationRecord`/`RecordInterface`도 message-social 자체 사본을
  새로 만들었다. **아직 message-common에 없다** — Phase 1(Kafka DTO를 message-common으로 이동)이 시작
  전이기 때문. Phase 1이 끝나면 이 3개 파일은 공유 버전으로 교체/삭제 대상이다.
- 알림 발송은 `FriendController`가 `friendInvite.invite(...)` / `friendAccept.accept(...)` 성공 직후
  `FriendNotificationService`(신규, application 계층)를 호출하는 방식으로 배선했다 — 위에서 말한 대로
  `FriendInviteService`를 건드리지 않기 위한 선택.
- 단위 테스트: `RedisNotifierTest`, `ClientNotificationServiceTest`, `PushServiceTest`, `KafkaProducerTest`,
  `FriendNotificationServiceTest`. 실제 Redis/Kafka 브로커 없이 전부 Mockito로 검증한다(지시사항대로
  통합 테스트에서는 이 경계를 목으로 대체).

### 6. 통합 테스트
`FriendConnectionIntegrationTest` (`@SpringBootTest` + `@AutoConfigureMockMvc`, H2 인메모리 DB) —
컨트롤러 → 서비스 → 실제 JPA 영속성까지 실제 빈으로 묶어서 검증한다. `RedisNotifier`/`SessionService`/
`PushService`/`KafkaProducer` 4개만 `@MockitoBean`으로 대체(실제 브로커 불필요). 시나리오:
- 초대 → 수락 → 양쪽에서 ACCEPTED 연결이 보이는지
- 초대 → 거절 → 거절된 연결은 disconnect 불가(400)
- 수락 후 disconnect는 최초 inviter/invitee 어느 쪽이 요청해도 성공
- disconnect 후 재초대 시 새 PENDING 연결 생성
- 존재하지 않는 초대 코드는 404
- invite-code 조회

### 7. 인프라/빌드 변경
- `build.gradle`: `spring-kafka`, `mysql-connector-java`(runtime), 테스트용 `h2`, `spring-kafka-test` 추가
- `src/main/resources/application.yaml`: datasource(단일 소스, 포트 13312), JPA, Redis 클러스터,
  Kafka 프로듀서 설정 추가. `ddl-auto: validate`로 뒀다 — 스키마는 아래 Phase 2c 초기화 스크립트가
  담당하고 애플리케이션은 검증만 한다.
- `src/test/resources/application.yaml`: H2(`create-drop`)로 오버라이드. Redis/Kafka는 오토컨피그를
  그대로 두되(연결은 지연 생성이라 기동에는 문제 없음) 실제 통신이 필요한 지점만 테스트별로 목 처리.

### 8. Phase 2c — 전용 MySQL 인스턴스 (docker-compose + k8s)

- `docker/docker-compose.yaml`: `mysql-source-social` 서비스 추가. 포트 `13312:3306`, DB명 `social`,
  기존 `mysql-credentials`와 동일한 `dev_user`/`dev_password` 재사용. 계획 2c대로 **replica 없이 source
  단일 구성**이라 다른 source 컨테이너들과 달리 `--log_bin`/`--gtid-mode` 등 복제용 플래그는 넣지
  않았다. `docker/.env`는 건드릴 필요 없었다(포트가 다른 DB들처럼 compose 파일에 직접 박혀 있고,
  `.env`는 `HOST_IP`/`COMPOSE_PROJECT_NAME`만 관리하는 구조라서).
- `docker/init-scripts-source-social/01-schema.sql`: `social_user`(`user_id`/`username`/`invite_code`,
  username·invite_code UNIQUE)와 `user_connection`(`id`/`inviter_id`/`invitee_id`/`status`,
  `(inviter_id, invitee_id)` UNIQUE) 테이블 생성 — `UserConnectionJpaEntity`/`SocialUserJpaEntity`와
  컬럼명을 그대로 맞췄다.
- `docker/k8s/01-mysql/08-source-social.yaml`: 같은 패턴(StatefulSet + headless Service)으로 k8s에도
  추가. replica가 없어서 `04-source-message1.yaml` 같은 소스 파일에서 복제 관련 플래그만 뺀 형태.
  기존 `mysql-credentials` Secret을 그대로 참조한다.
- `docker/k8s/README.md`: 적용 순서에 `mysql-init-source-social` ConfigMap 생성 커맨드 추가, 표/주의사항
  갱신. **message-social 애플리케이션 자체의 Deployment/Service는 아직 k8s에 없다** — 이번에 추가한 건
  DB뿐이다.

로컬 Docker 데몬이 떠 있지 않아 `docker compose up -d mysql-source-social`로 실제 기동/초기화 스크립트
동작까지는 확인하지 못했다 — docker-compose.yaml/SQL 문법은 확인했지만 직접 띄워서 검증해보는 걸 권장한다.

### 9. message-social 앱 자체의 k8s 배포 (Dockerfile + Deployment)

DB 말고 message-social 애플리케이션도 k8s에 올릴 수 있도록 `06-message-auth`와 동일한 패턴으로 추가:

- `docker/k8s/11-message-social/Dockerfile` — gradle:8.10-jdk21로 `:message-social:bootJar` 빌드 후
  eclipse-temurin:21-jre-jammy 런타임에 얹는, 다른 서비스들과 동일한 멀티스테이지 빌드.
- `docker/k8s/11-message-social/00-all.yaml` — ConfigMap(`SOCIAL_DB_URL`/`KAFKA_BOOTSTRAP_SERVERS`/
  `REDIS_CLUSTER_NODES`) + Deployment + Service(8087) + HPA(1~3) + PodDisruptionBudget.
  `DB_USERNAME`/`DB_PASSWORD`는 기존 `mysql-credentials` Secret 재사용.
- 번호는 `11`로 붙였다 — `09`/`10`은 이미 monitoring/elk가 쓰고 있어서, 기존 폴더 번호를 밀어내는
  위험한 리네이밍 대신 뒤에 추가하는 쪽을 택했다.
- **web-gateway 라우팅은 연결 안 했다.** `08-web-gateway`의 `application.yaml`에 `/api/v1/social/**`
  라우트를 추가하는 건 계획 Phase 2e 항목이라 이번 스코프 밖. 그 전까지는
  `kubectl port-forward svc/message-social 8087:8087 -n chat-system`으로 직접 접근해야 한다.
- `docker/k8s/README.md`에 표/적용 순서/이미지 빌드 안내/주의사항을 전부 갱신했다.

### 10. 트랜잭션 처리 + 동시 accept 레이스 컨디션 수정

애플리케이션 서비스 중 조회 전용 2개(`FriendConnectionQueryService`, `FriendInviteCodeQueryService`)만
`@Transactional(readOnly = true)`가 붙어 있었고, 쓰기 유스케이스 4개(`FriendInviteService`,
`FriendAcceptService`, `FriendRejectService`, `FriendDisconnectService`)는 트랜잭션 경계가 아예 없던
버그를 발견해 수정했다.

- 네 서비스 모두 클래스 레벨 `@Transactional` 추가. 순수 애노테이션 추가라 생성자 시그니처가 안 바뀌고,
  `FriendInviteService`(사용자가 이미 작성/테스트 완료한 파일)도 `@InjectMocks` 기반 테스트에 영향
  없이 안전하게 적용 가능했다.
- 트랜잭션 경계만으로는 안 끝나는 문제가 하나 있었다: `FriendAcceptService`의 연결 수 제한(10명) 체크가
  "COUNT 조회 → 제한 검사 → save" 흐름인데, 락이 없으면 같은 유저를 대상으로 한 두 개의 동시 accept
  요청이 둘 다 "제한 미만"으로 읽고 둘 다 통과해서 제한이 뚫릴 수 있는 TOCTOU 레이스였다(MySQL 기본
  REPEATABLE READ에서 `@Transactional`만으로는 안 막힘).
이 레이스를 막는 방식을 세 번 갈아엎었다. 최종적으로는 **비정규화 카운터**로 정착했다.

- **1차 시도(폐기)**: `social_user` 테이블의 유저 row에 비관적 쓰기 락을 거는 `LockUserPort`를
  만들어 붙였다. "COUNT 대상은 `user_connection`인데 락은 엉뚱한 `social_user`에 건다"는 지적을
  받아 폐기.
- **2차 시도(폐기)**: 락을 실제 COUNT 대상인 `user_connection`에 직접 걸도록 바꿨다. 기존 조회
  조건 `(inviter_id = X OR invitee_id = X)`이 인덱스 없는 OR라 `FOR UPDATE`를 걸면 풀 테이블
  락이 될 위험이 있어서, `inviter_id`/`invitee_id`를 오름차순 정렬한 조회 전용 컬럼
  `user_id_a`/`user_id_b` + 복합 인덱스를 추가해 인덱스 레인지 스캔으로 처리되게 했다(라이브
  COUNT 방식). 동작은 했지만, 코드 리뷰 중 `UserConnectionCount.increase()/decrease()`가 죽은
  코드로 남아있는 걸 발견 → "증가 로직이 빠진 거 아니냐"는 질문 → 논의 끝에 **"그냥 비정규화 하자"**로
  최종 결론이 바뀌었다.
- **최종 설계(비정규화 카운터)**: message-system의 옛 `connection_count` 컬럼 + increase/decrease
  패턴으로 되돌아가되, `UserConnectionCount` 도메인 객체가 이미 그 모양으로 스캐폴드돼 있었으니
  그걸 실제로 쓰도록 완성했다.
  - `social_user`에 `connection_count`(INT, 기본값 0) 컬럼 추가(`SocialUserJpaEntity
    .connectionCount`/`updateConnectionCount(int)`).
  - `SocialUserJpaRepository.findByIdForUpdate` — `@Lock(PESSIMISTIC_WRITE)`로 유저 row를 잠근다.
    이번엔 실제로 그 row의 `connection_count`를 바꾸는 용도라 "엉뚱한 테이블을 잠근다"는 지적이
    더 이상 해당 안 된다.
  - `LoadUserConnectionCountPort.lockAndLoad(Long)`(`Optional<UserConnectionCount>`)과
    `SaveUserConnectionCountPort.save(UserConnectionCount)` 두 포트로 재설계, 둘 다
    `UserPersistenceAdapter`(`social_user` 담당)가 구현한다. `UserConnectionPersistenceAdapter`
    (`user_connection` 담당)에서는 카운트 관련 코드를 전부 제거했다 — `user_id_a`/`user_id_b`
    컬럼/인덱스와 락 없는 `findByUserIdAndStatus`(목록 조회)는 그 자체로 유효한 최적화라 유지.
  - `FriendAcceptService`: accepter/inviter를 **오름차순 userId로** 잠그고(`lockAndLoad`) 두 쪽
    다 제한 통과를 확인한 뒤에야 `increase()` 호출 + 저장 — 한쪽만 증가하고 다른 쪽은 제한에 걸려
    실패하는 상황을 방지.
  - `FriendDisconnectService`: 지금까지 카운트를 전혀 안 건드리고 있던 걸 발견해 accept와 대칭으로
    `decrease()` + 저장을 추가했다(라이브 COUNT 방식일 땐 필요 없었지만, 비정규화로 바꾸는 순간
    여기서 안 줄이면 카운터가 영원히 안 줄어드는 버그가 된다).
  - `UserConnectionCount.decrease()`를 `count = Math.max(0, count - 1)`로 방어적으로 수정 —
    데이터 불일치로 카운터가 이미 0이어도 disconnect 요청 자체는 예외 없이 그대로 성공시킨다(사용자
    확인 후 결정).
  - `docker/init-scripts-source-social/01-schema.sql`의 `social_user` 테이블에 `connection_count`
    컬럼을 반영했다.
- `FriendAcceptServiceTest`/`FriendDisconnectServiceTest`를 새 포트에 맞게 다시 쓰고, 오름차순
  잠금 순서, 증가/감소 후 저장되는 값, "락 대상 유저 row가 없으면 UserNotFoundException", "카운터가
  이미 0이어도 disconnect는 성공" 케이스를 각각 추가했다. `FriendConnectionIntegrationTest`에도
  accept→disconnect 후 양쪽 `connection_count`가 실제로 1→0으로 바뀌는지 확인하는 케이스를
  추가했다.
- 진짜 동시 요청 경쟁 상황을 재현하는 멀티스레드 테스트는 여전히 없고, H2의 락 동작이 MySQL과
  완전히 같다는 보장도 없다(운영 DB로 별도 검증 권장) — 이건 설계를 세 번 바꾸는 동안 계속 남아있는
  한계다.

### 11. 제한 검사를 도메인 안으로 이동 (도메인 예외 + 서비스에서 웹 예외로 변환)

`FriendAcceptService`가 `isAtLimit()`으로 먼저 확인하고 나서 `increase()`를 호출하는 "확인 후 실행"
구조였는데, 이걸 `UserConnectionCount.increase()`가 스스로 제한을 지키도록(자기 검증) 바꿨다:

- `UserConnectionCount.isAtLimit()`을 `private`으로 내리고, `increase()` 내부에서 호출해서 제한
  초과 시 새로 만든 순수 도메인 예외 `domain.userconnectioncount.ConnectionLimitExceededException`
  (userId만 담음, `HttpStatus`/`FriendErrorCode` 등 웹 계층 타입을 전혀 참조하지 않음)을 던진다.
- `FriendAcceptService.applyConnectionCount`가 `firstCount.increase()`/`secondCount.increase()`를
  `try`로 감싸고, 이 도메인 예외를 잡아서 `getUserId()`가 accepterId와 일치하는지로 accepter/partner
  중 누가 걸렸는지 판단해 기존 웹 계층 예외(`adapter.in.web.exception.ConnectionLimitExceededException`
  + `FriendErrorCode`)로 변환해 던진다 — 도메인 → 애플리케이션(변환) → 웹, 계층이 명확히 분리됐다.
  같은 이름의 예외 클래스가 도메인/웹 두 패키지에 각각 있어서, 서비스 코드에서 웹 쪽은 풀네임으로
  참조해 구분한다.
- 최종적으로 `accept()`가 던지는 예외 타입과 HTTP 응답은 이전과 동일해서 기존 테스트는 전부 그대로
  통과했다. 도메인 객체의 자기 검증 자체를 검증하는 `UserConnectionCountTest`(increase가 제한에서
  막히는지, decrease가 0 밑으로 안 내려가는지)를 새로 추가했다.

### 12. username/inviteCode를 message-user REST 호출로 전환, connection_count는 message-social 자체 상태로 분리

`social_user`가 username/inviteCode(message-user 소유 데이터)와 connection_count(message-social
자체 도메인 상태)를 한 테이블에 섞어서 들고 있던 걸 경계 따라 나눴다. 계기: "message-social 안에
userEntity가 있을 필요는 없어, 이건 message-user에서 조회할거야"라는 제안 — 다만 connection_count는
message-user 데이터가 아니라 message-social 자체 상태라 그것까지 message-user로 넘길 순 없다고
판단해서, 그 경계선을 따라 쪼개는 방향으로 합의했다.

- **message-user**: 지금까지 `/register`/`/unregister`만 있던 `UserController`에 조회 전용
  엔드포인트 3개 추가 — `GET /api/v1/user/by-username/{username}`, `GET /api/v1/user/by-invite-code/{inviteCode}`,
  `GET /api/v1/user/{userId}`(찾으면 200 + `UserLookupResponse{userId, username, inviteCode}`,
  없으면 404). `UserRepository.findByInviteCode` 추가, `UserService`에 읽기 전용 조회 메서드 3개
  추가. `UserControllerTest`(WebMvcTest, 6개 케이스) 신규 작성.
  - **환경 이슈 발견 및 수정**: message-user는 `build.gradle`에 Java 툴체인 지정이 없어서, 이 로컬
    환경(JDK 25 + Lombok 1.18.36 조합)에서 `compileJava` 자체가 `NoSuchFieldException`으로 실패하고
    있었다(내가 손대기 전부터 이미 깨져 있었음 — `git stash`로 확인). message-system은 이미
    `JavaLanguageVersion.of(17)` 툴체인이 박혀 있어 문제 없었던 것과 대조. CLAUDE.md에 "message-auth/
    message-user/message-system 전부 Java 17 타깃"이라고 적혀 있는 것과 맞춰 message-user에도 같은
    툴체인 블록을 추가해서 고쳤다 — 내 변경과 무관하게 이미 깨져 있던 걸 발견해서 바로잡은 것.
- **message-social**:
  - `SocialUserJpaEntity`/`SocialUserJpaRepository`/`UserPersistenceAdapter`(username/inviteCode +
    connection_count를 같이 들고 있던 원래 구조) 전부 삭제.
  - `LoadUserPort` 구현을 `MessageUserPersistenceAdapter`(신규, `adapter/out/client/messageuser`)로
    교체 — Spring 6.1의 `RestClient`로 message-user의 3개 엔드포인트를 동기 호출. 404는
    `HttpClientErrorException.NotFound`를 잡아서 `Optional.empty()`로 변환. base-url은
    `message-social.message-user.base-url`(`MESSAGE_USER_BASE_URL` 환경변수, 로컬 기본값
    `http://localhost:8082`) — k8s ConfigMap(`11-message-social/00-all.yaml`)에도 서비스 DNS로 추가.
  - connection_count 전용으로 `UserConnectionCountJpaEntity`/`UserConnectionCountJpaRepository`
    (테이블 `user_connection_count`, 컬럼은 `user_id`/`connection_count`뿐)와
    `UserConnectionCountPersistenceAdapter`(`LoadUserConnectionCountPort`/`SaveUserConnectionCountPort`
    구현)를 새로 만들었다. 락(`findByIdForUpdate`, `@Lock(PESSIMISTIC_WRITE)`)은 이전 설계와 동일.
  - **부수 효과(의도적 개선)**: `LoadUserConnectionCountPort.lockAndLoad`가 이제 `Optional`이 아니라
    `UserConnectionCount`를 직접 반환한다 — row가 없으면 count 0으로 새로 만들어서 반환(auto-vivify).
    예전엔 "row가 없으면 `UserNotFoundException`"이었는데, 그 존재 검증이 애초에 username/inviteCode
    캐시 테이블이 있어야만 가능했던 것이라, 그 테이블이 없어진 지금은 존재 검증을 굳이 여기서 다시
    할 이유가 없다(inviter는 이미 message-user 조회로, accepter는 인증 헤더로 각각 존재가 확인된
    상태에서 이 코드에 도달함). `FriendAcceptService`/`FriendDisconnectService`의 `lockAndLoad` 헬퍼도
    그에 맞춰 단순화.
  - `docker/init-scripts-source-social/01-schema.sql`: `social_user` 테이블을 `user_connection_count`
    (user_id, connection_count만)로 교체.
  - `FriendConnectionIntegrationTest`: `SocialUserJpaEntity` 시드 대신 `LoadUserPort`를
    `@MockitoBean`으로 대체해서 alice/bob의 username/inviteCode 조회를 스텁 — message-user 연동
    자체는 `MessageUserPersistenceAdapterTest`(신규, `MockRestServiceServer`로 요청/응답 검증)가
    맡고, 이 통합 테스트는 원래 목적(컨트롤러→서비스→connection_count 영속성 연결)에 집중하게 했다.
  - `FriendAcceptServiceTest`/`FriendDisconnectServiceTest`: `lockAndLoad` 스텁에서 `Optional.of(...)`
    래핑 전부 제거. "락 대상 row 없음 → UserNotFoundException" 테스트는 더 이상 그 케이스가 존재하지
    않아서 삭제.
### 13. 본인-본인 초대 차단

앞선 두 항목(11, 12)에서 계속 "self-accept 가드가 흔들리고 있다"고 되짚었던 근본 원인을 이번에
소스에서 막았다 — accept가 아니라 **invite 단계**에서 막는 게 맞다고 판단했다: 자기 자신을 초대하는
연결 자체가 만들어지지 않으면, 그 뒤의 accept/disconnect 쪽 self-* 엣지 케이스도 애초에 발생할 수
없기 때문이다.

- `FriendErrorCode.SELF_INVITE_NOT_ALLOWED`(400) 추가, `SelfInviteException extends FriendException`
  신규.
- `FriendInviteService.invite()`에서 초대 코드로 resolve한 `inviteeId`가 `inviterId`와 같으면
  connection 조회/생성 전에 바로 예외를 던지도록 한 줄 추가 — 사용자가 이미 작성/테스트 완료한
  파일이라 최소 침습으로, 기존 로직 흐름은 건드리지 않고 맨 앞에 가드만 얹었다.
- `FriendInviteServiceTest`에 단위 테스트, `FriendConnectionIntegrationTest`에 HTTP 레벨 테스트
  (`POST /invite/{자기_초대코드}` → 400 `SELF_INVITE_NOT_ALLOWED`) 추가.
- 이걸로 12번에서 남겨뒀던 `FriendAcceptService`의 `first.equals(second)` 가드 유실 문제는 **실질적으로
  해소됐다** — invite에서 막히므로 self-connection이 PENDING 상태로 존재할 수 없고, 따라서 accept가
  그 경로를 탈 일도 없다. 다만 가드 자체를 다시 넣지는 않았다(방어적 이중 안전장치로 넣을지는 별도
  판단 필요 — 지금은 "애초에 발생 불가능한 경로"로 처리).

## 의도적으로 미룬 것 (다음 작업 후보)

1. **connections 응답의 username** — 지금은 `{userId, status}`만 반환한다. message-user 조회 API가
   생겼으니 이제는 기술적으로 채울 수 있지만(Phase 3의 로컬 캐시를 안 기다려도 됨), 목록의 각 항목마다
   message-user를 개별 호출하는 건 N+1이 되므로 배치 조회 API나 캐싱 전략을 먼저 정하고 붙이는 게 맞다.
2. **동시성 테스트 부재** — `FriendAcceptService`의 락이 실제로 레이스를 막는지 검증하는 멀티스레드/동시
   요청 테스트가 없다. 지금은 락 순서와 존재 여부만 단위 테스트로 확인한 상태.

## 테스트 실행

```bash
./gradlew :message-social:test
```

현재 79개 테스트 전부 통과(단위 테스트 다수 + 통합 테스트, Kafka/Redis 관련은 전부 단위 테스트).
