# chat 프로젝트 k8s 마이그레이션 파일 모음

폴더 앞 번호가 곧 **적용 순서**입니다. 앞 단계가 준비돼야 뒷 단계가 정상 동작하니
꼭 순서대로 진행해주세요. 전부 네임스페이스 `chat-system` 기준입니다.

## 폴더별 내용

| 폴더 | 내용 | 비고 |
|---|---|---|
| `01-mysql` | MySQL source/replica 6개 + message-social 전용 source 1개 (StatefulSet) | 가장 먼저 적용. `00-namespace.yaml`부터 시작. social은 replica 없이 source 단일 구성 |
| `02-kafka` | Kafka 3브로커 KRaft (StatefulSet) + kafka-ui | podManagementPolicy: Parallel 적용됨 |
| `03-redis` | Redis Cluster 6노드 (StatefulSet) + RedisInsight | podManagementPolicy: Parallel 적용됨 |
| `04-message-connection-flux` | WebSocket 서버 (Deployment, HPA 적용됨) | `patched-source.zip` = Redis Pub/Sub 구조로 고친 소스코드 |
| `05-message-system` | 비즈니스 로직/DB 처리 서버 (Deployment, HPA 적용됨) | `patched-source.zip` = Redis Pub/Sub 발행으로 고친 소스코드 |
| `06-message-auth` | 인증/JWT 발급 서버 | ⚠️ 실제 코드 미확인, 가정 기반 (본문 참고) |
| `07-message-user` | 유저 등록 서버 | ⚠️ 실제 코드 미확인, 가정 기반 |
| `08-web-gateway` | 진입점 (라우팅) | application.yaml 라우팅 직접 수정 필요 (아래 참고) |
| `09-monitoring` | Prometheus ServiceMonitor/Grafana datasource 등 | |
| `10-elk` | Elasticsearch/Logstash/Filebeat/Kibana | |
| `11-message-social` | 친구/소셜 도메인 서버 (Deployment, HPA 적용됨) | web-gateway 라우팅은 아직 미연결 (아래 참고) |

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
kubectl apply -f 01-mysql/  # 나머지 7개 (source/replica 6개 + social source 1개) 한 번에
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

## 애플리케이션 이미지 빌드 (04~08, 11 공통 패턴)

각 폴더의 `Dockerfile`을 chat 프로젝트 루트(`settings.gradle` 있는 위치)에 놓고:

```bash
docker build -t <서비스이름>:local .
kind load docker-image <서비스이름>:local
```

- `message-connection-flux` → `patched-source.zip` 안 내용으로 소스 덮어쓴 뒤 빌드
- `message-system` → 마찬가지로 `patched-source.zip` 내용으로 덮어쓴 뒤 빌드
- `message-auth`, `message-user`, `web-gateway`, `message-social` → 기존 소스 그대로 빌드

## message-social 배포/실행 방법

다른 서비스와 달리 web-gateway 라우팅이 아직 없어서(Phase 2e 미착수) 몇 단계가 더 필요하다.

```bash
# 0. 선행 조건: 01-mysql(social용 source 포함), 02-kafka, 03-redis가 이미 적용/기동돼 있어야 함
#    (위 "전체 적용 순서"의 1번 인프라 단계, mysql-init-source-social 컨피그맵까지 포함)

# 1. 이미지 빌드 + kind에 로드 — 방법은 위 "애플리케이션 이미지 빌드" 섹션과 동일
#    (11-message-social/Dockerfile을 chat 프로젝트 루트에 놓고 빌드)
docker build -t message-social:local .
kind load docker-image message-social:local

# 2. 배포
kubectl apply -f 11-message-social/00-all.yaml

# 3. 뜨는지 확인
kubectl get pods -n chat-system -l app=message-social
kubectl logs -n chat-system deploy/message-social -f

# 4. 직접 접근 (web-gateway 라우팅이 없으므로 port-forward로 우회)
kubectl port-forward svc/message-social 8087:8087 -n chat-system
```

`social_user` 테이블은 Phase 3(message-user 프로필 이벤트 구독)가 끝나기 전까지 아무도 채우지 않는
빈 테이블이다. 그래서 지금 상태로 `/api/v1/social/friends/**`를 호출하면 어떤 `USER_ID`를 넣어도
`USER_NOT_FOUND`/`INVITE_CODE_NOT_FOUND`만 돌아온다. 동작을 눈으로 확인하려면 테스트용 행을 직접
넣어야 한다:

```bash
kubectl exec -it -n chat-system mysql-source-social-0 -- \
  mysql -udev_user -pdev_password social \
  -e "INSERT INTO social_user (user_id, username, invite_code) VALUES (1, 'alice', 'ALICE01'), (2, 'bob', 'BOB0001');"
```

그런 다음(위 4번 port-forward가 떠 있는 상태에서):

```bash
# bob(2)이 자기 초대 코드 확인
curl -H "USER_ID: 2" http://localhost:8087/api/v1/social/friends/invite-code

# alice(1)가 bob의 초대 코드로 초대
curl -X POST -H "USER_ID: 1" http://localhost:8087/api/v1/social/friends/invite/BOB0001

# bob(2)이 alice의 초대를 수락
curl -X POST -H "USER_ID: 2" http://localhost:8087/api/v1/social/friends/accept/alice
```

`USER_ID` 헤더는 원래 web-gateway의 `AuthorizationHeaderFilter`가 JWT 검증 후 주입해주는 값이다 —
gateway를 거치지 않고 직접 호출할 때는 이렇게 헤더를 수동으로 넣어줘야 한다. `HttpRequestFilter`가
이 헤더가 숫자인지만 검증하고, 실제 JWT 검증은 하지 않는다.

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
- **message-social**: 배포/실행 방법은 위 "message-social 배포/실행 방법" 섹션 참고. web-gateway
  라우팅이 아직 없어서(Phase 2e 미착수) port-forward로 직접 접근해야 한다.
