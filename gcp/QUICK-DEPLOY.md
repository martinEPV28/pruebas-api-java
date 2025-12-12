# 🚀 Deploy Rápido en GCP

## Opción 1: Cloud Run (1 comando)

```bash
# 1. Build y push
docker build -t gcr.io/YOUR_PROJECT_ID/product-api:latest .
docker push gcr.io/YOUR_PROJECT_ID/product-api:latest

# 2. Deploy (reemplaza YOUR_PROJECT_ID y passwords)
gcloud run deploy product-api \
  --image gcr.io/YOUR_PROJECT_ID/product-api:latest \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --min-instances 1 \
  --max-instances 5 \
  --memory 512Mi \
  --set-env-vars "DB_HOST=YOUR_CLOUD_SQL_HOST,DB_PASSWORD=YOUR_PASSWORD,API_TOKEN_SECRET=your_secret"

# 3. Obtener URL
gcloud run services describe product-api --region us-central1 --format 'value(status.url)'
```

## Opción 2: GKE (Alta Disponibilidad)

```bash
# 1. Crear cluster
gcloud container clusters create-auto product-api-cluster --region us-central1

# 2. Crear secrets
kubectl create secret generic product-api-secrets \
  --from-literal=db-password=YOUR_DB_PASSWORD \
  --from-literal=jwt-secret=YOUR_JWT_SECRET

# 3. Deploy PostgreSQL
kubectl apply -f gcp/postgres-deployment.yaml

# 4. Build, push y deploy API
docker build -t gcr.io/YOUR_PROJECT_ID/product-api:latest .
docker push gcr.io/YOUR_PROJECT_ID/product-api:latest
kubectl apply -f gcp/api-deployment.yaml

# 5. Obtener IP
kubectl get service product-api-service
```

## URLs después del deploy

- **Health Check**: `https://YOUR-URL/actuator/health`
- **Swagger UI**: `https://YOUR-URL/swagger-ui.html`
- **API Docs**: `https://YOUR-URL/api-docs`

## Comandos útiles

```bash
# Ver logs (Cloud Run)
gcloud run services logs read product-api --region us-central1 --limit 50

# Ver logs (GKE)
kubectl logs -l app=product-api --tail=100

# Escalar (GKE)
kubectl scale deployment product-api --replicas=5

# Eliminar (Cloud Run)
gcloud run services delete product-api --region us-central1

# Eliminar (GKE)
kubectl delete -f gcp/
gcloud container clusters delete product-api-cluster --region us-central1
```

## Costos estimados

- **Cloud Run**: ~$9/mes (para tráfico bajo)
- **GKE**: ~$100/mes (cluster + recursos)

**Recomendación**: Empieza con Cloud Run, es más simple y barato.
