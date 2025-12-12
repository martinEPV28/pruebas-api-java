# 🚀 Despliegue en GCP - Product API

## 📋 Opción Recomendada: Cloud Run (Simple + Alta Disponibilidad)

**Cloud Run** es serverless, escala automáticamente de 0 a N instancias, y solo pagas por uso.

---

## 🔧 Pre-requisitos

1. **Cuenta GCP** con proyecto creado
2. **gcloud CLI** instalado: https://cloud.google.com/sdk/docs/install
3. **Docker** instalado localmente

---

## 🎯 Opción 1: Cloud Run (Recomendado - MÁS SIMPLE)

### Paso 1: Configurar GCP

```bash
# Login a GCP
gcloud auth login

# Configurar proyecto (reemplaza PROJECT_ID con tu proyecto)
gcloud config set project YOUR_PROJECT_ID

# Habilitar APIs necesarias
gcloud services enable run.googleapis.com
gcloud services enable containerregistry.googleapis.com
gcloud services enable sqladmin.googleapis.com
```

### Paso 2: Crear PostgreSQL en Cloud SQL

```bash
# Crear instancia PostgreSQL
gcloud sql instances create product-api-db \
  --database-version=POSTGRES_15 \
  --tier=db-f1-micro \
  --region=us-central1 \
  --root-password=YOUR_SECURE_PASSWORD

# Crear base de datos
gcloud sql databases create product_api \
  --instance=product-api-db

# Crear usuario
gcloud sql users create apiuser \
  --instance=product-api-db \
  --password=YOUR_USER_PASSWORD
```

### Paso 3: Build y Push de la Imagen

```bash
# Configurar Docker para GCP
gcloud auth configure-docker

# Build de imagen (desde raíz del proyecto)
docker build -t gcr.io/YOUR_PROJECT_ID/product-api:latest .

# Push a Google Container Registry
docker push gcr.io/YOUR_PROJECT_ID/product-api:latest
```

### Paso 4: Deploy a Cloud Run

```bash
# Deploy con variables de entorno
gcloud run deploy product-api \
  --image gcr.io/YOUR_PROJECT_ID/product-api:latest \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --min-instances 1 \
  --max-instances 10 \
  --memory 512Mi \
  --cpu 1 \
  --port 8080 \
  --set-env-vars "SERVER_PORT=8080" \
  --set-env-vars "DB_HOST=/cloudsql/YOUR_PROJECT_ID:us-central1:product-api-db" \
  --set-env-vars "DB_PORT=5432" \
  --set-env-vars "DB_NAME=product_api" \
  --set-env-vars "DB_USERNAME=apiuser" \
  --set-env-vars "DB_PASSWORD=YOUR_USER_PASSWORD" \
  --set-env-vars "API_TOKEN_SECRET=your_jwt_secret_key_change_this" \
  --add-cloudsql-instances YOUR_PROJECT_ID:us-central1:product-api-db

# Obtener URL de la aplicación
gcloud run services describe product-api --region us-central1 --format 'value(status.url)'
```

### Paso 5: Verificar

```bash
# URL devuelta por el comando anterior
URL=$(gcloud run services describe product-api --region us-central1 --format 'value(status.url)')

# Test health check
curl $URL/actuator/health

# Test Swagger
echo "Swagger UI: $URL/swagger-ui.html"
```

---

## 🎯 Opción 2: Google Kubernetes Engine (GKE) - Alta Disponibilidad

### Paso 1: Crear Cluster GKE

```bash
# Crear cluster autopilot (más simple, managed)
gcloud container clusters create-auto product-api-cluster \
  --region us-central1 \
  --project YOUR_PROJECT_ID

# Conectar kubectl al cluster
gcloud container clusters get-credentials product-api-cluster \
  --region us-central1
```

### Paso 2: Crear ConfigMap y Secret

```bash
# Crear secret para DB password
kubectl create secret generic product-api-secrets \
  --from-literal=db-password=YOUR_USER_PASSWORD \
  --from-literal=jwt-secret=your_jwt_secret_key_change_this

# Crear ConfigMap
kubectl create configmap product-api-config \
  --from-literal=SERVER_PORT=8080 \
  --from-literal=DB_HOST=product-api-db-service \
  --from-literal=DB_PORT=5432 \
  --from-literal=DB_NAME=product_api \
  --from-literal=DB_USERNAME=apiuser
```

### Paso 3: Deploy PostgreSQL

```bash
# Aplicar deployment de PostgreSQL
kubectl apply -f gcp/postgres-deployment.yaml
```

### Paso 4: Deploy API

```bash
# Build y push imagen
docker build -t gcr.io/YOUR_PROJECT_ID/product-api:latest .
docker push gcr.io/YOUR_PROJECT_ID/product-api:latest

# Aplicar deployment
kubectl apply -f gcp/api-deployment.yaml

# Ver status
kubectl get pods
kubectl get services
```

### Paso 5: Obtener IP Externa

```bash
# Ver IP externa del LoadBalancer
kubectl get service product-api-service

# Esperar a que tenga EXTERNAL-IP (puede tardar 2-3 minutos)
EXTERNAL_IP=$(kubectl get service product-api-service -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

echo "API URL: http://$EXTERNAL_IP"
echo "Swagger: http://$EXTERNAL_IP/swagger-ui.html"
```

---

## 📊 Comparación de Opciones

| Característica | Cloud Run | GKE |
|----------------|-----------|-----|
| **Simplicidad** | ⭐⭐⭐⭐⭐ Muy simple | ⭐⭐⭐ Moderado |
| **Costo** | 💰 Paga por uso | 💰💰 Cluster siempre corriendo |
| **Escalado** | ✅ Automático (0-N) | ✅ Automático (manual config) |
| **Alta disponibilidad** | ✅ Sí (multi-zona) | ✅ Sí (multi-zona) |
| **Startup** | ⚡ Rápido (5 min) | ⏱️ Medio (15 min) |
| **Gestión** | 🤖 Serverless | 🔧 Requiere gestión |
| **Recomendado para** | MVP, demos, apps pequeñas | Producción enterprise |

---

## 🔒 Seguridad Adicional

### Autenticación en Cloud Run

```bash
# Deploy con autenticación requerida
gcloud run deploy product-api \
  --image gcr.io/YOUR_PROJECT_ID/product-api:latest \
  --no-allow-unauthenticated

# Dar acceso a usuarios específicos
gcloud run services add-iam-policy-binding product-api \
  --region us-central1 \
  --member="user:EMAIL@example.com" \
  --role="roles/run.invoker"
```

### HTTPS Automático

Cloud Run proporciona HTTPS automáticamente con certificado SSL gestionado.

---

## 🧪 Test de Alta Disponibilidad

### Test 1: Escalar manualmente (GKE)

```bash
# Escalar a 5 réplicas
kubectl scale deployment product-api --replicas=5

# Ver pods
kubectl get pods -w
```

### Test 2: Test de carga

```bash
# Instalar hey (load testing)
go install github.com/rakyll/hey@latest

# Test con 1000 requests, 50 concurrent
hey -n 1000 -c 50 https://YOUR-CLOUD-RUN-URL.run.app/actuator/health

# Cloud Run escalará automáticamente
```

---

## 💰 Estimación de Costos (USD/mes)

### Cloud Run (Recomendado)
```
Asumiendo:
- 100,000 requests/mes
- 500ms promedio por request
- 512MB RAM

CPU: $0.024 x 50,000 vCPU-seconds/mes = $1.20
RAM: $0.0025 x 25,000 GiB-seconds/mes = $0.06
Requests: $0.40 por millón = $0.04
Cloud SQL (db-f1-micro): $7.67

TOTAL: ~$9/mes
```

### GKE Autopilot
```
Cluster autopilot: ~$75/mes (mínimo)
Cloud SQL: $7.67/mes
LoadBalancer: $18/mes

TOTAL: ~$100/mes
```

**Recomendación:** Cloud Run para comenzar, migrar a GKE si creces.

---

## 🚨 Troubleshooting

### Error: "Connection refused" a PostgreSQL

```bash
# Verificar Cloud SQL Proxy
kubectl logs deployment/product-api | grep -i sql

# Verificar secret
kubectl get secret product-api-secrets -o yaml
```

### Error: "Container failed to start"

```bash
# Ver logs
gcloud run services logs read product-api --region us-central1 --limit 50

# O en GKE
kubectl logs -l app=product-api --tail=100
```

### Error: Out of Memory

```bash
# Aumentar memoria en Cloud Run
gcloud run services update product-api \
  --memory 1Gi \
  --region us-central1
```

---

## 📈 Monitoreo

### Cloud Run

```bash
# Ver métricas en consola
gcloud run services describe product-api \
  --region us-central1 \
  --format yaml

# URL del dashboard
echo "https://console.cloud.google.com/run/detail/us-central1/product-api/metrics"
```

### GKE

```bash
# Instalar metrics
kubectl top pods
kubectl top nodes

# Ver logs en tiempo real
kubectl logs -f deployment/product-api
```

---

## 🎯 Comandos Útiles

### Cloud Run

```bash
# Ver todas las revisiones
gcloud run revisions list --service product-api --region us-central1

# Rollback a versión anterior
gcloud run services update-traffic product-api \
  --to-revisions REVISION_NAME=100 \
  --region us-central1

# Ver variables de entorno
gcloud run services describe product-api \
  --region us-central1 \
  --format="get(spec.template.spec.containers[0].env)"

# Eliminar servicio
gcloud run services delete product-api --region us-central1
```

### GKE

```bash
# Ver todos los recursos
kubectl get all

# Describir pod
kubectl describe pod <pod-name>

# Port forward para debug
kubectl port-forward svc/product-api-service 8080:80

# Eliminar todo
kubectl delete -f gcp/
```

---

## ✅ Checklist de Despliegue

- [ ] Cuenta GCP creada y configurada
- [ ] gcloud CLI instalado y autenticado
- [ ] Proyecto GCP creado
- [ ] APIs habilitadas (Cloud Run, Container Registry)
- [ ] Cloud SQL PostgreSQL creado
- [ ] Base de datos y usuario configurados
- [ ] Imagen Docker construida y pusheada
- [ ] Variables de entorno configuradas
- [ ] Servicio desplegado
- [ ] Health check funcionando
- [ ] Swagger UI accesible
- [ ] JWT funcionando
- [ ] CRUD de productos funcionando

---

## 📚 Recursos Adicionales

- [Cloud Run Docs](https://cloud.google.com/run/docs)
- [GKE Docs](https://cloud.google.com/kubernetes-engine/docs)
- [Cloud SQL Docs](https://cloud.google.com/sql/docs)
- [Best Practices](https://cloud.google.com/run/docs/best-practices)

---

**Última actualización**: 2025-12-12
