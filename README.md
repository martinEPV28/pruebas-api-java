# 🛍️ Product API - Gestión de Productos con JWT

Aplicación Spring Boot 3.5.1 para gestionar productos con **autenticación JWT**, **almacenamiento en archivos JSON** y **caché Redis**.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.1-green?logo=spring)](https://spring.io/projects/spring-boot)
[![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)](https://redis.io/)

---

## ✨ Características

- ✅ **Autenticación JWT** - Tokens seguros con HS512
- ✅ **Almacenamiento JSON** - Sin base de datos (ideal para MVP)
- ✅ **Caché Redis** - TTL 10 minutos para optimizar performance
- ✅ **API RESTful** - Endpoints completos con Swagger UI
- ✅ **BCrypt** - Hashing seguro de contraseñas
- ✅ **Tests completos** - 21 unit tests incluidos
- ✅ **Docker Ready** - Container para GCP Cloud Run
- ✅ **Java 21** - Última versión LTS

---

## 🚀 Inicio Rápido

### Requisitos

```
Java 21+
Maven 3.9+
Redis 7 (opcional para caché)
```

### Ejecutar Localmente

**Con Maven:**
```bash
mvn spring-boot:run
```

**Con Docker Compose (con Redis):**
```bash
docker-compose up -d
```

**Acceder:**
- Swagger: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

---

## 🔐 Autenticación

### Usuarios Pre-creados

```
admin / password     (ADMIN, USER)
user / 123456        (USER)
```

### Login en Swagger

1. Click en **Authorize** 🔓
2. POST `/api/auth/token`:
   ```json
   {"username": "admin", "password": "password"}
   ```
3. Copia el token y autentica

---

## 📚 Endpoints Principales

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/token` | Obtener JWT token | ❌ |
| GET | `/api/products` | Listar productos (cached) | ✅ |
| GET | `/api/products/{id}` | Obtener producto | ✅ |
| POST | `/api/products` | Crear producto | ✅ |
| PUT | `/api/products/{id}` | Actualizar | ✅ |
| DELETE | `/api/products/{id}` | Eliminar | ✅ |
| GET | `/swagger-ui.html` | Documentación | ❌ |

---

## 💾 Almacenamiento

### Productos (data/products.json)
```json
{
  "id": 1,
  "name": "Laptop Gaming",
  "price": 1599.99,
  "rating": 4.8,
  "createdAt": "2025-12-12T23:30:00",
  "updatedAt": "2025-12-12T23:30:00"
}
```

**Características:**
- Auto-increment ID
- Timestamps automáticos
- Serialización Jackson con LocalDateTime
- Thread-safe con ConcurrentHashMap

### Usuarios (data/users.json)

Pre-creados con contraseñas BCrypt. Ver archivos para cambiar.

---

## ⚡ Caché Redis

Configuración automática con:
- **TTL:** 10 minutos
- **Serializer:** Jackson con soporte LocalDateTime
- **Estrategia:** `@Cacheable` en GET, `@CacheEvict` en POST/PUT/DELETE

---

## 🏗️ Arquitectura

```
HTTP Client
    ↓
TokenAuthFilter (JWT validation)
    ↓
REST Controllers
    ↓
Services (lógica)
    ↓
Repositories (persistencia)
    ├─ FileProductRepository → data/products.json
    └─ FileUserRepository → data/users.json
    ↓
Redis Cache (TTL 10 min)
```

**Ver detalles:** [ARQUITECTURA-FINAL.md](ARQUITECTURA-FINAL.md)

---

## 🧪 Testing

```bash
# Ejecutar tests
mvn test

# Test específico
mvn test -Dtest=ProductControllerTest

# Con coverage
mvn jacoco:report
```

**Incluye:**
- 4 AuthControllerTests
- 7 ProductControllerTests  
- 10 JwtUtilTests

---

## ☁️ GCP Cloud Run

```bash
# Build y push
gcloud builds submit --tag gcr.io/PROJECT/product-api

# Deploy
gcloud run deploy product-api \
  --image gcr.io/PROJECT/product-api \
  --platform managed \
  --region us-central1 \
  --memory 512Mi \
  --set-env-vars REDIS_HOST=redis-host
```

**Ver:** [docs/DEPLOY-GCP.md](docs/DEPLOY-GCP.md)

---

## 📊 Stack Tecnológico

| Componente | Versión | Rol |
|-----------|---------|-----|
| Java | 21 LTS | Runtime |
| Spring Boot | 3.5.1 | Framework |
| Redis | 7 | Cache |
| JWT (JJWT) | 0.11.5 | Autenticación |
| BCrypt | 6.4.2 | Hash contraseñas |
| Jackson | 2.15.2 | JSON serialization |
| Springdoc | 2.1.0 | Swagger UI |

---

## ⚙️ Configuración

### application.yml

```yaml
server:
  port: 8080

spring:
  cache:
    type: redis
  data:
    redis:
      host: localhost
      port: 6379

product:
  storage:
    file: data/products.json

user:
  storage:
    file: data/users.json
```

### Variables de Entorno

```bash
REDIS_HOST=localhost          # Redis host
REDIS_PORT=6379              # Redis port
API_TOKEN_SECRET=mysecret    # JWT secret
SERVER_PORT=8080             # Server port
```

---

## 📖 Documentación Completa

- **[ARQUITECTURA-FINAL.md](ARQUITECTURA-FINAL.md)** - Diseño completo del sistema
- **[RESUMEN-TECNICO.md](RESUMEN-TECNICO.md)** - Especificaciones técnicas
- **[docs/DEPLOY-GCP.md](docs/DEPLOY-GCP.md)** - Guía de despliegue

---

## ⚠️ Limitaciones

### ✅ Ideal Para
- MVPs y prototipado
- Bajo tráfico
- Testing y demos
- Desarrollo local

### ⚠️ No Para Producción
- Alto tráfico / high concurrency
- Datos críticos sin respaldo
- Múltiples servidores
- Transacciones ACID complejas

### 🚀 Para Producción Real
1. PostgreSQL + JPA
2. Redis Cluster
3. Monitoring (Prometheus/Grafana)
4. Logging distribuido (ELK)

---

## 🤝 Soporte

- 📧 Email: support@example.com
- 🐛 Issues: GitHub Issues
- 💡 Discussions: GitHub Discussions

---

<div align="center">

**⭐ Si te gustó este proyecto, no olvides dar una estrella!**

Hecho con ❤️ por GitHub Copilot

**Status:** ✅ Producción Lista | **Versión:** 1.0.0

</div>
|--------|----------|-------------|
| GET | `/api/products` | Listar todos |
| GET | `/api/products/{id}` | Obtener por ID |
| POST | `/api/products` | Crear nuevo |
| PUT | `/api/products/{id}` | Actualizar |
| DELETE | `/api/products/{id}` | Eliminar |

## 🔄 Ciclo de Desarrollo

1. **Compilar**: `mvn clean compile`
2. **Tests**: `mvn test`
3. **Build**: `mvn package -DskipTests`
4. **Ejecutar**: `java -jar target/product-api-0.0.1-SNAPSHOT.jar`
5. **Verificar**: http://localhost:8080/swagger-ui.html

## 🎯 Stack Tecnológico

- **Framework**: Spring Boot 3.5.1
- **Lenguaje**: Java 21 LTS
- **Base de Datos**: PostgreSQL
- **ORM**: JPA/Hibernate
- **Build**: Maven 3.9.11
- **Testing**: JUnit 5 + Mockito
- **API Docs**: Springdoc OpenAPI 3.0
- **Server**: Tomcat 10.1.42

## 📄 Licencia

MIT

## 👤 Autor

Martin Desarrollo

---

**Última actualización**: 2025-12-11
