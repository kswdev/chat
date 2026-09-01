# 모니터링 스택 설치 순서

## 0. Helm으로 kube-prometheus-stack 설치 (아직 안 하셨거나 재설치하시는 경우)

```bash
brew install helm

helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

helm install prometheus-stack prometheus-community/kube-prometheus-stack \
  --namespace monitoring --create-namespace

kubectl get pods -n monitoring -w
```

CRD(ServiceMonitor 등)까지 다 설치되고 pod가 전부 Running이 될 때까지 기다린 뒤 다음 단계로 넘어가세요.
CRD 설치 확인: `kubectl get crd | grep servicemonitor`

## 1. 나머지 매니페스트 적용

```bash
kubectl apply -f 01-app-servicemonitors.yaml
kubectl apply -f 02-kafka-exporter.yaml
kubectl apply -f 03-tempo.yaml
kubectl apply -f 04-influxdb.yaml
kubectl apply -f 05-grafana-extra-datasources.yaml
kubectl apply -f 06-redis-servicemonitor.yaml
```

## 2. InfluxDB에 database 생성 (JMeter Backend Listener용)

```bash
kubectl get pods -n monitoring -l app=influxdb
kubectl exec -it <influxdb pod이름> -n monitoring -- influx -execute "CREATE DATABASE excilys"
kubectl exec -it <influxdb pod이름> -n monitoring -- influx -execute "SHOW DATABASES"
```

## 3. 확인

```bash
# Grafana (Helm이 만든 것 - 기본 계정 admin, 비밀번호는 아래로 확인)
kubectl get secret prometheus-stack-grafana -n monitoring \
  -o jsonpath="{.data.admin-password}" | base64 -d; echo
kubectl port-forward svc/prometheus-stack-grafana 3000:80 -n monitoring

# InfluxDB (JMeter Backend Listener 대상)
kubectl port-forward svc/influxdb 18086:8086 -n monitoring
```

브라우저에서 `localhost:3000` 열어서 좌측 Connections → Data sources에 Prometheus(Helm 기본 등록) +
Tempo + InfluxDB 3개가 다 보이는지 확인하세요.

## 4. 앱들 otlp 트레이싱 엔드포인트 확인

5개 서비스(`message-connection-flux`, `message-system`, `message-auth`, `message-user`, `web-gateway`)의
`application.yml`에서 아래처럼 되어 있는지 확인:

```yaml
management:
  otlp:
    tracing:
      endpoint: http://tempo.monitoring.svc.cluster.local:4318/v1/traces
```

## 5. (선택) 클러스터 전체 대시보드 Import

Grafana → Dashboards → New → Import → 아래 ID 입력, datasource는 Prometheus 선택:

- `315` 또는 `6417` — Kubernetes 클러스터 전체 (Helm이 kube-state-metrics를 이미 같이 설치해줘서 바로 됩니다)
- `4701` — JVM (Micrometer)
- `7589` — Kafka Exporter Overview
