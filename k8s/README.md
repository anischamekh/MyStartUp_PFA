# Kubernetes deployment

## Prerequisites

- Images built and pushed (`anischamekh/mystartup-*:latest`)
- `kubectl` configured for your cluster

## Deploy order

```bash
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/postgres-init-configmap.yaml
kubectl apply -f k8s/zookeeper-deployment.yaml
kubectl apply -f k8s/kafka-deployment.yaml
kubectl apply -f k8s/redis-deployment.yaml
kubectl apply -f k8s/postgres-deployment.yaml

# Wait for postgres (first start runs init-databases.sql)
kubectl wait --for=condition=ready pod -l app=postgres --timeout=180s

kubectl apply -f k8s/auth-service-deployment.yaml
kubectl apply -f k8s/hrm-service-deployment.yaml
kubectl apply -f k8s/project-service-deployment.yaml
kubectl apply -f k8s/chatbot-service-deployment.yaml
kubectl apply -f k8s/api-gateway-deployment.yaml
kubectl apply -f k8s/ingress.yaml
```

## JWT secret (WeakKeyException)

`JWT_SECRET` in `secrets.yaml` must be **at least 32 bytes**. The placeholder `replace-me` causes `WeakKeyException` at startup.

After changing secrets, restart workloads so pods pick up env vars:

```bash
kubectl apply -f k8s/secrets.yaml
kubectl rollout restart deployment auth-service hrm-service project-service chatbot-service api-gateway
```

## Fresh Postgres volume

Init scripts run only on **first** PVC creation. If databases are missing, delete the PVC and redeploy postgres:

```bash
kubectl delete deployment postgres
kubectl delete pvc postgres-pvc
kubectl apply -f k8s/postgres-deployment.yaml
```

## Verify

```bash
kubectl get pods
kubectl logs deployment/auth-service --tail=50
```
