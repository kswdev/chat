#!/bin/bash

docker exec -it chat-kafka1-1 /opt/kafka/bin/kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic push-notification \
  --partitions 6 \
  --replication-factor 3

docker exec -it chat-kafka1-1 /opt/kafka/bin/kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic message-relay \
  --partitions 6 \
  --replication-factor 3

docker exec -it chat-kafka1-1 /opt/kafka/bin/kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic message-request \
  --partitions 6 \
  --replication-factor 3

