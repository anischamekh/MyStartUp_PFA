# Kafka access (Docker)

## Why http://localhost:9092 does not work

Port **9092** is the Kafka **binary protocol**, not HTTP. Opening it in a browser sends an HTTP request; the broker logs:

`InvalidReceiveException: Invalid receive (size = … larger than 104857600)`

That is expected and does **not** mean Kafka is broken.

## How to use Kafka correctly

| Use case | How |
|----------|-----|
| From microservices in Docker | `KAFKA_BOOTSTRAP_SERVERS=kafka:29092` (already in `docker-compose.yml`) |
| From your host (apps, CLI) | `localhost:9092` with a **Kafka client** (Spring Kafka, `kafka-console-producer`, etc.) |
| Web UI | **Kafka UI** at http://localhost:8090 (`kafka-ui` service in `docker-compose.yml`) |

## Quick checks

```powershell
docker compose ps kafka
docker compose logs kafka --tail 20
```

Produce a test message (inside the broker container):

```powershell
docker compose exec kafka kafka-console-producer --bootstrap-server localhost:9092 --topic test
```
