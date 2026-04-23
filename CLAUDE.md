# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Distributed real-time chat system built with Spring Boot microservices. Communication flows through a gateway, uses WebSocket for persistent client connections, Kafka for async inter-service messaging, MySQL with read replicas for persistence, and Redis cluster for session/caching.

## Build Commands

```bash
# Build all modules
./gradlew build

# Build and skip tests
./gradlew build -x test

# Run tests (all modules)
./gradlew test

# Run tests for a specific module
./gradlew :message-connection-flux:test

# Run a specific test class
./gradlew :message-system:test --tests "ClassName"

# Run a specific service
./gradlew :message-connection-flux:bootRun
./gradlew :message-system:bootRun
./gradlew :web-gateway:bootRun
```

## Infrastructure Setup

Before running services, start infrastructure with Docker Compose:

```bash
cd docker/
docker-compose up -d
bash prepare_topics.sh   # Create Kafka topics (message-relay, message-request, push-notification)
```

**Important**: `docker/.env` contains `HOST_IP` which must match the local machine's LAN IP (e.g., `192.168.0.5`). Update this when the IP changes.

Infrastructure endpoints:
- Kafka UI: http://localhost:18090
- Redis Insight: http://localhost:15540
- MySQL user DB: `localhost:13306` (source) / `13307` (replica)
- MySQL message DB 1: `localhost:13308` / `13309`
- MySQL message DB 2: `localhost:13310` / `13311`
- Redis cluster: `localhost:6380-6385`
- Kafka brokers (external): `localhost:19094,19095,19096`

## Service Ports

| Service | Port |
|---|---|
| web-gateway | 8080 |
| message-auth | 8081 |
| message-user | 8082 |
| message-system | 8070 |
| message-connection-flux | 8090 |

## Architecture

### Request Flow

```
Client
  └─→ web-gateway (8080)
        ├─→ /api/v1/auth/**   → message-auth (8081)   [JWT issuance]
        ├─→ /api/v1/user/**   → message-user (8082)   [user registration]
        └─→ /ws/v1/message    → message-connection-flux (8090)  [WebSocket]
                                       ↓ Kafka (message-request)
                                 message-system (8070)
                                       ↓ MySQL shards + Redis
                                       ↓ Kafka (push-notification)
                                 message-push  [delivery to clients]
```

### Modules

- **message-common** — shared DTOs and constants; not a runnable service (`bootJar` disabled)
- **message-auth** — Spring Security + Auth0 JWT; MySQL user DB (source/replica)
- **message-user** — user registration; Redis caching; MySQL user DB (source/replica)
- **web-gateway** — Spring Cloud Gateway; JWT validation filter; CORS; routes above services
- **message-connection-flux** — **active WebSocket service** using Spring WebFlux + hexagonal architecture (Java 21); Kafka producer on incoming messages; Kafka consumer for push events
- **message-connection** — older non-reactive WebSocket implementation (main class commented out; superseded by flux module)
- **message-system** — message persistence; 2-shard MySQL setup with QueryDSL; Kafka consumer for message-request; publishes to push-notification
- **message-push** — Kafka consumer for push-notification topic; delivers notifications to clients
- **message-client** — standalone CLI WebSocket test client (Tyrus + JLine); run via `main` in `net.study.messagesystem.MessageClient`

### Key Design Decisions

- **message-connection-flux** is the current WebSocket implementation; `message-connection` is legacy and unused.
- **Hexagonal architecture** is applied in `message-connection-flux` (ports/adapters pattern).
- **Database sharding**: message data is split across two MySQL shard pairs (`source-message1`/`source-message2` + replicas). Routing logic lives in `message-system`.
- **Java versions**: all modules target Java 17 except `message-connection-flux` which uses Java 21.
- **Testing**: Spock 2.4 (Groovy) and JUnit 5 are both available.

## Technology Stack

- Spring Boot 3.4.4 (3.5.0 for message-connection-flux), Spring Cloud Gateway
- Spring WebFlux (Project Reactor) in message-connection-flux
- Apache Kafka 4.1.0 KRaft mode (no ZooKeeper)
- MySQL 8.0.40 with GTID-based replication
- Redis 7.4.1 cluster
- Spring Data JPA + QueryDSL
- Gradle 8.x multi-module build

## Important Notes

- Do NOT use message-connection module (deprecated)
- Always use message-connection-flux

## Global Rules

- message-connection is deprecated. Use message-connection-flux only.
- System uses Kafka for async messaging
- WebSocket handled by message-connection-flux (WebFlux)
- Do not use blocking calls in reactive modules