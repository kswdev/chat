# chat 프로젝트 k8s 마이그레이션 파일 모음

폴더 앞 번호가 곧 **적용 순서**입니다. 앞 단계가 준비돼야 뒷 단계가 정상 동작하니
꼭 순서대로 진행해주세요. 전부 네임스페이스 `chat-system` 기준입니다.

## 폴더별 내용

| 폴더 | 내용 | 비고 |
|---|---|---|
| `01-mysql` | MySQL source/replica 6개 + message-social 전용 source 1개 (StatefulSet) + 내부 API 키 Secret | 가장 먼저 적용. `00-namespace.yaml`부터 시작. social은 replica 없이 source 단일 구성. `09-internal-api-secret.yaml`은 message-system↔message-social 내부 호출 인증용 |
| `02-kafka` | Kafka 3브로커 KRaft (StatefulSet) + kafka-ui | podManagementPolicy: Parallel 적용됨 |
| `03-redis` | Redis Cluster 6노드 (StatefulSet) + RedisInsight | podManagementPolicy: Parallel 적용됨 |
| `04-message-connection-flux` | WebSocket 서버 (Deployment, HPA 적용됨) | `patched-source.zip` = Redis Pub/Sub 구조로 고친 소스코드 |
| `05-message-system` | 비즈니스 로직/DB 처리 서버 (Deployment, HPA 적용됨) | `patched-source.zip` = Redis Pub/Sub 발행으로 고친 소스코드 |
| `06-message-auth` | 인증/JWT 발급 서버 | ⚠️ 실제 코드 미확인, 가정 기반 (본문 참고) |
| `07-message-user` | 유저 등록 서버 | ⚠️ 실제 코드 미확인, 가정 기반 |
| `08-web-gateway` | 진입점 (라우팅) | application.yaml 라우팅 직접 수정 필요 (아래 참고) |
| `09-monitoring` | Prometheus ServiceMonitor/Grafana datasource 등 | |
| `10-elk` | Elasticsearch/Logstash/Filebeat/Kibana | |
| `11-message-social` | 친구/소셜 도메인 서버 (Deployment, HPA 적용됨) | 2026-09-23부로 상시 롤아웃에 포함됨. web-gateway `/api/v1/social/**` 라우팅 이미 연결돼 있음 |

## 전체 적용 순서


```bash
# 0. metrics server
# metrics-server 공식 매니페스트 적용
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# kind 환경 전용 우회 — kubelet 인증서가 클러스터 CA로 서명 안 돼있어서 필요
kubectl patch deployment metrics-server -n kube-system --type='json' \
  -p='[{"op":"add","path":"/spec/template/spec/containers/0/args/-","value":"--kubelet-insecure-tls"}]'

# 1. 인프라
kubectl apply -f 01-mysql/00-namespace.yaml
kubectl apply -f 01-mysql/01-secret.yaml
kubectl apply -f 01-mysql/09-internal-api-secret.yaml  # message-system<->message-social 내부 호출 인증용
kubectl apply -f 01-mysql/  # 나머지 (source/replica 6개 + social source 1개) 한 번에
kubectl apply -f 02-kafka/
kubectl apply -f 03-redis/

kubectl create configmap mysql-init-source \
  --from-file=./init-scripts-source -n chat-system

kubectl create configmap mysql-init-replica \
  --from-file=./init-scripts-replica -n chat-system
  
kubectl create configmap mysql-init-source-message1 \
  --from-file=./init-scripts-source-message1 -n chat-system

kubectl create configmap mysql-init-replica-message1 \
  --from-file=./init-scripts-replica-message1 -n chat-system

kubectl create configmap mysql-init-source-message2 \
  --from-file=./init-scripts-source-message2 -n chat-system

kubectl create configmap mysql-init-replica-message2 \
  --from-file=./init-scripts-replica-message2 -n chat-system

kubectl create configmap mysql-init-source-social \
  --from-file=./init-scripts-source-social -n chat-system

# 2. 애플리케이션 (각 폴더의 Dockerfile로 이미지 빌드 + kind load 먼저 해야 함)
kubectl apply -f 04-message-connection-flux/
kubectl apply -f 05-message-system/
kubectl apply -f 06-message-auth/00-all.yaml
kubectl apply -f 07-message-user/00-all.yaml
kubectl apply -f 08-web-gateway/00-all.yaml
kubectl apply -f 11-message-social/00-all.yaml
```

> `init-scripts-*` 디렉터리들은 `docker/k8s/`가 아니라 `docker/` 바로 아래에 있다(`docker/init-scripts-source-social` 등). 위 `kubectl create configmap` 명령들을 `docker/k8s/`에서 실행한다면 `--from-file` 경로를 `../init-scripts-*`로 잡아야 한다.

## 애플리케이션 이미지 빌드 (04~08, 11 공통 패턴)

각 폴더의 `Dockerfile`을 chat 프로젝트 루트(`settings.gradle` 있는 위치)에 놓고:

```bash
docker build -t <서비스이름>:local .
kind load docker-image <서비스이름>:local
```

- `message-connection-flux` → `patched-source.zip` 안 내용으로 소스 덮어쓴 뒤 빌드
- `message-system` → 마찬가지로 `patched-source.zip` 내용으로 덮어쓴 뒤 빌드
- `message-auth`, `message-user`, `web-gateway`, `message-social` → 기존 소스 그대로 빌드

## message-social 배포/실행 방법 (2026-09-23 갱신: 상시 롤아웃에 포함됨)

이제 다른 서비스와 동일하게 "전체 적용 순서"의 2단계에서 `kubectl apply -f 11-message-social/00-all.yaml`로
같이 배포되고, web-gateway `/api/v1/social/**` 라우팅도 이미 연결돼 있다. 별도 port-forward 없이
web-gateway(`kubectl port-forward svc/web-gateway 8080:8080 -n chat-system`)를 거쳐 정상적인
JWT(`Authorization: Bearer ...`)로 접근하면 된다. 아래는 message-social만 따로 재빌드/재배포하고 싶을 때 참고:

```bash
# 0. 선행 조건: 01-mysql(social용 source + 01-mysql/09-internal-api-secret.yaml 포함), 02-kafka, 03-redis가
#    이미 적용/기동돼 있어야 함 (mysql-init-source-social 컨피그맵까지 포함)

# 1. 이미지 빌드 + kind에 로드 — 방법은 위 "애플리케이션 이미지 빌드" 섹션과 동일
docker build -f 11-message-social/Dockerfile -t message-social:local ../..
kind load docker-image message-social:local

# 2. 배포/재시작
kubectl apply -f 11-message-social/00-all.yaml
kubectl rollout restart deployment/message-social -n chat-system

# 3. 뜨는지 확인
kubectl get pods -n chat-system -l app=message-social
kubectl logs -n chat-system deploy/message-social -f

# 4. (선택) 직접 접근하고 싶을 때만 port-forward
kubectl port-forward svc/message-social 8087:8087 -n chat-system
```

`user_connection`/`user_connection_count` 테이블은 실제 유저 데이터가 없으면 비어있으므로, 눈으로
동작을 보려면 web-gateway를 거쳐 `/api/v1/user/register` → `/api/v1/auth/login`으로 실제 유저 2명을
만들고 그 JWT로 `/api/v1/social/friends/**`를 호출하는 게 가장 정확하다:

```bash
# (port-forward svc/web-gateway 8080:8080 상태에서)
curl -X POST http://localhost:8080/api/v1/user/register -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"pw12345"}'
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"pw12345"}')

curl http://localhost:8080/api/v1/social/friends/invite-code -H "Authorization: Bearer $TOKEN"
```

message-social을 직접(port-forward로) 호출할 때만 `USER_ID` 헤더를 수동으로 넣어야 한다 — 원래는
web-gateway의 `AuthorizationHeaderFilter`가 JWT 검증 후 주입해주는 값이다. `HttpRequestFilter`가
이 헤더가 숫자인지만 검증하고 실제 JWT 검증은 하지 않으므로, 직접 호출 시엔 아무 숫자나 넣어도 통과된다
(내부망에서만 열려있다는 전제). `/actuator/**`는 이 필터에서 예외 처리돼 있어 k8s
readiness/liveness probe와 Prometheus 스크레이핑은 헤더 없이도 통과한다.

message-system → message-social 내부 전용 엔드포인트(`/connections/accepted-count`)는 별도로
`X-Internal-Api-Key` 헤더 검증을 거친다(`InternalApiKeyFilter`). 두 서비스 모두
`01-mysql/09-internal-api-secret.yaml`의 `internal-api-credentials` Secret에서 같은 값을 주입받는다.

## 주의사항 요약 (지금까지 대화에서 나온 것들)

- **web-gateway `application.yaml`**: k8s DNS(`*.chat-system.svc.cluster.local`)로 이미 수정 완료.
  `message-auth`/`message-user`/`message-social`/`message-connection-flux` 라우팅 전부 연결돼 있음.
- **message-auth/message-user**: DB 접속 프로퍼티 이름이 message-system과 같다고
  가정하고 만들었습니다. 실제로 다르면 알려주시면 바로 고쳐드립니다.
- **Kafka/Redis StatefulSet**: `podManagementPolicy: Parallel` 필수 (부트스트랩
  데드락 회피). 이미 반영되어 있음.
- **외부 접근**: web-gateway는 `kubectl port-forward svc/web-gateway 8080:8080 -n chat-system`
  으로 접근.
- **message-social**: 이제 상시 롤아웃에 포함되어 web-gateway 경유로 바로 접근 가능. 자세한 내용은
  위 "message-social 배포/실행 방법" 섹션 참고.
- **kind 클러스터가 갑자기 API 서버에 연결 안 될 때**: Docker Desktop이 재시작되면서
  `kind-control-plane` 컨테이너가 멈춰있을 수 있다 — `docker start kind-control-plane`으로
  재시작하면 대부분 몇 분 안에 자체 복구된다(`kubectl get pods -n chat-system`으로 지켜보면 됨).
