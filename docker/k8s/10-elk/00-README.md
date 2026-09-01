# ELK 스택 설치 순서

## 0. 미리 알아두실 것

- 지금까지 만든 것 중 리소스를 제일 많이 씁니다 (Elasticsearch 힙 512MB, 총 요청 리소스
  합치면 CPU 1코어+/메모리 2GB+ 정도). Docker Desktop 리소스 할당(Settings → Resources)이
  넉넉한지 먼저 확인해주세요. 지난번 "No space left on device"처럼 디스크/메모리 부족
  증상이 다시 날 수 있어요.
- Elasticsearch가 요구하는 `vm.max_map_count` 커널 파라미터는 initContainer가 자동으로
  올려줍니다 (Mac은 보통 Docker Desktop의 리눅스 VM 자체가 이미 충분한 값으로 설정돼
  있어서 문제 없을 거예요).

## 1. 적용

```bash
kubectl apply -f 00-namespace.yaml
kubectl apply -f 01-elasticsearch.yaml
kubectl apply -f 02-logstash.yaml
kubectl apply -f 03-filebeat.yaml
kubectl apply -f 04-kibana.yaml
```

Elasticsearch가 제일 먼저 Ready 돼야 나머지가 정상 연결됩니다. 순서대로 확인:

```bash
kubectl get pods -n logging -w
```

## 2. Kibana에서 로그 보기

```bash
kubectl port-forward svc/kibana 5601:5601 -n logging
```

브라우저에서 `localhost:5601` → 왼쪽 메뉴 Discover → index pattern으로
`chat-logs-*` 만들어주면 그 뒤로 모든 pod 로그가 들어옵니다.

Kibana Discover 화면에서 왼쪽 필드 목록에 `kubernetes.namespace`, `kubernetes.pod.name`,
`kubernetes.labels.app` 등이 보일 거예요 — 이걸로 `kubernetes.labels.app: message-connection-flux`
같은 필터를 걸면, 지금까지 `kubectl logs`로 따로따로 보시던 걸 한 화면에서 검색/필터링하며
볼 수 있습니다.

## 3. 확인이 안 되면

```bash
# filebeat가 실제로 로그를 수집해서 보내고 있는지
kubectl logs -n logging -l app=filebeat --tail=50

# logstash가 받아서 elasticsearch로 잘 넘기는지
kubectl logs -n logging -l app=logstash --tail=50

# elasticsearch에 인덱스가 실제로 생겼는지
kubectl exec -it elasticsearch-0 -n logging -- curl -s localhost:9200/_cat/indices?v
```
