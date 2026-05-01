# Real-time Chat System

분산 실시간 채팅 시스템. Spring Boot 마이크로서비스 아키텍처로 구성되며, WebSocket 기반 실시간 통신, Kafka 비동기 메시징, MySQL 샤딩, Redis 클러스터를 활용합니다.

---

## Architecture Overview

```
Client (Browser / CLI)
  └─→ web-gateway:8080  (JWT 검증, 라우팅)
        ├─→ /api/v1/auth/**   → message-auth:8081      [JWT 발급]
        ├─→ /api/v1/user/**   → message-user:8082      [회원 관리]
        └─→ /ws/v1/message    → message-connection-flux:8090  [WebSocket]
                                        │
                              Kafka: message-request
                                        │
                                 message-system:8070   [메시지 저장 / 샤딩]
                                        │
                              Kafka: push-notification
                                        │
                                  message-push         [클라이언트 알림 전달]
```

### 요청 흐름

1. 클라이언트가 `POST /api/v1/auth/**`로 로그인 → JWT 발급
2. WebSocket 연결 시 JWT를 쿼리 파라미터(`?token=`)로 전달
3. `web-gateway`의 `AuthorizationHeaderFilter`가 JWT 검증 후 `X-Authorization-Id` 헤더를 추가하여 하위 서비스로 라우팅
4. `message-connection-flux`가 WebSocket 연결을 수립하고 메시지를 Kafka(`message-request`)로 전송
5. `message-system`이 메시지를 수신하여 MySQL 샤드에 저장, Redis에 캐싱, `push-notification` 토픽으로 발행
6. `message-push`와 `message-connection-flux`가 push 이벤트를 수신하여 연결된 클라이언트로 WebSocket 응답 전송

---

## Modules

| 모듈 | 포트 | 설명 |
|------|------|------|
| `message-common` | — | 공통 DTO, 상수 (non-runnable 라이브러리) |
| `message-auth` | 8081 | Spring Security + JWT 인증 |
| `message-user` | 8082 | 회원 등록/탈퇴 |
| `web-gateway` | 8080 | Spring Cloud Gateway, JWT 검증, 라우팅 |
| `message-connection-flux` | 8090 | WebSocket 서버 (Reactive, 헥사고날 아키텍처) |
| `message-system` | 8070 | 메시지 영속성, DB 샤딩 |
| `message-push` | — | 클라이언트 알림 전달 (Kafka consumer) |
| `message-client` | — | CLI WebSocket 테스트 클라이언트 |

> `message-connection` 모듈은 deprecated. `message-connection-flux`를 사용합니다.

---

## Technology Stack

| 분류 | 기술 |
|------|------|
| Framework | Spring Boot 3.4.4 / 3.5.0, Spring Cloud Gateway 2024.0.3 |
| Reactive | Spring WebFlux, Project Reactor, Reactor Kafka |
| Messaging | Apache Kafka 4.1.0 (KRaft mode, ZooKeeper 없음) |
| Persistence | MySQL 8.0.40 (GTID 복제), Spring Data JPA, QueryDSL 5.0.0 |
| Cache | Redis 7.4.1 클러스터 (6노드), Spring Data Redis Reactive |
| Auth | Auth0 Java JWT 4.2.2, JJWT 0.11.2, Spring Security |
| Testing | Spock 2.4 (Groovy BDD), JUnit 5 |
| Build | Gradle 8.x (멀티모듈), Java 17 / 21 |

---

## Infrastructure

### 사전 요구사항

- Docker & Docker Compose
- Java 21+
- Gradle 8.x

### 인프라 실행

```bash
cd docker/

# HOST_IP를 현재 머신의 LAN IP로 수정 (Redis 클러스터 연결에 필요)
# .env 파일 예시: HOST_IP=192.168.0.5

docker-compose up -d

# Kafka 토픽 생성 (message-relay, message-request, push-notification)
bash prepare_topics.sh
```

### 인프라 엔드포인트

| 서비스 | 주소 |
|--------|------|
| Kafka UI | http://localhost:18090 |
| Redis Insight | http://localhost:15540 |
| MySQL user DB (source) | localhost:13306 |
| MySQL user DB (replica) | localhost:13307 |
| MySQL message DB shard1 (source) | localhost:13308 |
| MySQL message DB shard1 (replica) | localhost:13309 |
| MySQL message DB shard2 (source) | localhost:13310 |
| MySQL message DB shard2 (replica) | localhost:13311 |
| Redis cluster | localhost:6380 ~ 6385 |
| Kafka brokers | localhost:19094, 19095, 19096 |

---

## Build & Run

```bash
# 전체 빌드
./gradlew build

# 테스트 생략 빌드
./gradlew build -x test

# 특정 모듈 테스트
./gradlew :message-connection-flux:test

# 특정 테스트 클래스 실행
./gradlew :message-system:test --tests "ClassName"
```

### 서비스 실행 순서

```bash
# 1. 인프라 먼저 기동
cd docker && docker-compose up -d && bash prepare_topics.sh

# 2. 각 서비스 실행 (별도 터미널)
./gradlew :message-auth:bootRun
./gradlew :message-user:bootRun
./gradlew :message-system:bootRun
./gradlew :message-push:bootRun
./gradlew :message-connection-flux:bootRun
./gradlew :web-gateway:bootRun
```

### CLI 테스트 클라이언트

```bash
./gradlew :message-client:run
```

---

## Module Details

### message-connection-flux — 헥사고날 아키텍처

현재 사용 중인 WebSocket 서버. Spring WebFlux 기반 리액티브 구현이며 헥사고날(Ports & Adapters) 아키텍처를 적용합니다.

```
message-connection-flux/
├── adpter/
│   ├── in/
│   │   ├── kafka/          # Kafka 인바운드 (25+ 핸들러 타입)
│   │   │   ├── RecordDispatcher.java
│   │   │   └── ListenTopicConsumer.java
│   │   └── websocket/      # WebSocket 인바운드 (15+ 핸들러 타입)
│   │       ├── MessageWebSocketHandler.java
│   │       └── request/RequestDispatcher.java
│   └── out/
│       ├── kafka/           # Kafka 아웃바운드
│       │   └── KafkaProducer.java
│       └── persistence/redis/
│           ├── WebSocketSessionManager.java
│           └── RedisCacheRepository.java
├── application/
│   ├── dto/
│   │   ├── kafka/           # ~90 Kafka record DTO
│   │   └── websocket/       # 20+ 인바운드 / 20+ 아웃바운드 DTO
│   ├── port/
│   │   ├── in/SessionService.java
│   │   └── out/
│   │       ├── CachePort.java
│   │       ├── EventProducer.java
│   │       └── ClientNotificationService.java
│   └── service/SessionServiceImpl.java
└── domain/
    ├── channel/Channel.java
    ├── connection/Connection.java
    ├── message/Message.java
    └── user/UserId.java
```

**핵심 패턴:**
- `WebSocketSessionManager`: `ConcurrentHashMap<UserId, Sink<String>>`로 활성 세션 관리
- `RecordDispatcher`: Kafka 레코드 타입별 핸들러 디스패치
- `RequestDispatcher`: WebSocket 요청 타입별 핸들러 디스패치
- Reactor `Sink`를 통한 백프레셔 지원 메시지 스트리밍

### message-system — DB 샤딩

채널 ID 기반으로 메시지를 두 MySQL 샤드에 분산 저장합니다.

```
샤딩 키: channelId % 2
  0 → source-message1 (13308) / replica-message1 (13309)
  1 → source-message2 (13310) / replica-message2 (13311)
```

`RoutingDataSource`가 런타임에 샤드를 결정하며, `@Transactional(readOnly = true)` 트랜잭션은 자동으로 레플리카로 라우팅됩니다.

**Kafka 토픽 구독:**
- `message-request` (group: `request-group`) — 채널 생성, 메시지 저장 등 커맨드 처리
- `message-relay` (group: `relay-group`) — 메시지 릴레이 이벤트

**Kafka 발행:**
- `push-notification` — 클라이언트 알림 이벤트

### web-gateway — JWT 인증 필터

`AuthorizationHeaderFilter`가 각 요청의 JWT를 검증합니다.

- HTTP 요청: `Authorization: Bearer <token>`
- WebSocket 연결: `?token=<token>` 쿼리 파라미터
- 검증 성공 시 `X-Authorization-Id` 헤더(user ID)를 추가하여 하위 서비스로 전달
- CORS: `localhost:3000` 허용

### Kafka 토픽 구조

| 토픽 | 생산자 | 소비자 | 용도 |
|------|--------|--------|------|
| `message-request` | message-connection-flux | message-system | 채널/메시지 커맨드 |
| `message-relay` | message-system | message-connection-flux | 메시지 릴레이 알림 |
| `push-notification` | message-system | message-push, message-connection-flux | 클라이언트 알림 |

---

## Authentication

```
POST /api/v1/auth/**
  → RestApiLoginAuthFilter (Spring Security)
  → JwtIssuer: JWT 발급 (유효시간 1시간)

클라이언트는 발급된 JWT를 이후 요청에 포함:
  REST:      Authorization: Bearer <token>
  WebSocket: ws://localhost:8080/ws/v1/message?token=<token>
```

---

## Design Decisions

- **헥사고날 아키텍처** (`message-connection-flux`): 포트/어댑터 패턴으로 비즈니스 로직과 인프라를 분리
- **리액티브 스택**: WebFlux + Reactor Kafka + Reactive Redis로 논블로킹 I/O 구현
- **DB 샤딩**: `channelId % 2` 기반 수평 샤딩으로 메시지 DB 분산
- **읽기/쓰기 분리**: `RoutingDataSource`로 읽기 전용 트랜잭션을 레플리카로 자동 라우팅
- **Java 21** (`message-connection-flux`): 가상 스레드 지원 활용; 나머지 모듈은 Java 17
- **Kafka KRaft**: ZooKeeper 없이 Kafka 자체 합의 알고리즘으로 운영
