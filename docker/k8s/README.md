# chat 프로젝트 k8s 마이그레이션 파일 모음

폴더 앞 번호가 곧 **적용 순서**입니다. 앞 단계가 준비돼야 뒷 단계가 정상 동작하니
꼭 순서대로 진행해주세요. 전부 네임스페이스 `chat-system` 기준입니다.

## 폴더별 내용

| 폴더 | 내용 | 비고 |
|---|---|---|
| `01-mysql` | MySQL source/replica 6개 (StatefulSet) | 가장 먼저 적용. `00-namespace.yaml`부터 시작 |
| `02-kafka` | Kafka 3브로커 KRaft (StatefulSet) + kafka-ui | podManagementPolicy: Parallel 적용됨 |
| `03-redis` | Redis Cluster 6노드 (StatefulSet) + RedisInsight | podManagementPolicy: Parallel 적용됨 |
| `04-message-connection-flux` | WebSocket 서버 (Deployment, HPA 적용됨) | `patched-source.zip` = Redis Pub/Sub 구조로 고친 소스코드 |
| `05-message-system` | 비즈니스 로직/DB 처리 서버 (Deployment, HPA 적용됨) | `patched-source.zip` = Redis Pub/Sub 발행으로 고친 소스코드 |
| `06-message-auth` | 인증/JWT 발급 서버 | ⚠️ 실제 코드 미확인, 가정 기반 (본문 참고) |
| `07-message-user` | 유저 등록 서버 | ⚠️ 실제 코드 미확인, 가정 기반 |
| `08-web-gateway` | 진입점 (라우팅) | application.yaml 라우팅 직접 수정 필요 (아래 참고) |

## 전체 적용 순서

```bash
# 1. 인프라
kubectl apply -f 01-mysql/00-namespace.yaml
kubectl apply -f 01-mysql/01-secret.yaml
kubectl apply -f 01-mysql/  # 나머지 6개 (source/replica) 한 번에
kubectl apply -f 02-kafka/
kubectl apply -f 03-redis/

# 2. 애플리케이션 (각 폴더의 Dockerfile로 이미지 빌드 + kind load 먼저 해야 함)
kubectl apply -f 04-message-connection-flux/
kubectl apply -f 05-message-system/
kubectl apply -f 06-message-auth/00-all.yaml
kubectl apply -f 07-message-user/00-all.yaml
kubectl apply -f 08-web-gateway/00-all.yaml
```

## 애플리케이션 이미지 빌드 (04~08 공통 패턴)

각 폴더의 `Dockerfile`을 chat 프로젝트 루트(`settings.gradle` 있는 위치)에 놓고:

```bash
docker build -t <서비스이름>:local .
kind load docker-image <서비스이름>:local
```

- `message-connection-flux` → `patched-source.zip` 안 내용으로 소스 덮어쓴 뒤 빌드
- `message-system` → 마찬가지로 `patched-source.zip` 내용으로 덮어쓴 뒤 빌드
- `message-auth`, `message-user`, `web-gateway` → 기존 소스 그대로 빌드 (아직 코드 수정 없음)

## 주의사항 요약 (지금까지 대화에서 나온 것들)

- **web-gateway `application.yaml`**: `ws://localhost:8090/` 같은 하드코딩을
  `ws://message-connection-flux.chat-system.svc.cluster.local:8090/` 로 반드시 수정.
  message-auth/message-user 라우팅도 `http://message-auth.chat-system.svc.cluster.local:8081/`
  형태로 수정. (실제 파일 아직 못 봐서 직접 수정 필요)
- **message-auth/message-user**: DB 접속 프로퍼티 이름이 message-system과 같다고
  가정하고 만들었습니다. 실제로 다르면 알려주시면 바로 고쳐드립니다.
- **Kafka/Redis StatefulSet**: `podManagementPolicy: Parallel` 필수 (부트스트랩
  데드락 회피). 이미 반영되어 있음.
- **외부 접근**: web-gateway는 `kubectl port-forward svc/web-gateway 8080:8080 -n chat-system`
  으로 접근.
