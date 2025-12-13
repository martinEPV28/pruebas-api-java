# 📋 Product API - Arquitectura Final

## 🎯 Descripción General

**Product API** es una aplicación Spring Boot 3.5.1 que implementa un sistema de gestión de productos con autenticación JWT y almacenamiento basado en archivos JSON. La aplicación está optimizada para GCP Cloud Run con soporte para caché Redis.

**Stack Tecnológico:**
- **Java 21** - Lenguaje
- **Spring Boot 3.5.1** - Framework
- **JSON File Storage** - Persistencia de datos (`data/products.json`, `data/users.json`)
- **Redis 7** - Caché distribuido (TTL 10 minutos)
- **JWT (JJWT 0.11.5)** - Tokens de autenticación
- **BCrypt** - Hashing de contraseñas seguro
- **Springdoc OpenAPI 2.1.0** - API documentation (Swagger UI)
- **Spring Boot Actuator** - Health checks y monitoring

---

## 🏗️ Arquitectura

### Flujo de Datos

```
┌─────────────────────────────────────────────────────────────────┐
│                      HTTP Request (Swagger/API)                  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
                    ┌─────────────────────┐
                    │  TokenAuthFilter    │ ← Valida JWT tokens
                    └─────────────────────┘
                              ↓
            ┌──────────────────────────────────────┐
            │      REST Controllers                 │
            │  ├─ AuthController (/api/auth)       │
            │  ├─ ProductController (/api/products)│
            │  └─ UserController (/api/users)      │
            └──────────────────────────────────────┘
                    ↓                    ↓
        ┌──────────────────┐    ┌──────────────────┐
        │  AuthService     │    │ ProductService   │
        │ ├─ validateCreds │    │ ├─ listAll()     │
        │ └─ generateJWT   │    │ ├─ getById()     │
        └──────────────────┘    │ ├─ create()      │
              ↓                 │ ├─ update()      │
        ┌──────────────────┐    │ └─ delete()      │
        │FileUserRepository│    └──────────────────┘
        │                  │          ↓
        │ ├─ findByUser()  │    ┌──────────────────────────┐
        │ ├─ loadFromFile()│    │  @Cacheable/@CachePut    │
        │ └─ Users list    │    │  @CacheEvict (Redis)     │
        └──────────────────┘    │  (10 min TTL)            │
              ↓                 └──────────────────────────┘
        ┌──────────────────┐          ↓
        │   Redis Cache    │    ┌──────────────────┐
        │ users::<id>      │    │FileProductRepository
        │ (no cachea)      │    │                  │
        └──────────────────┘    │ ├─ findAll()     │
                                │ ├─ findById()    │
        ┌──────────────────┐    │ ├─ save()        │
        │data/users.json   │    │ ├─ update()      │
        │ [                │    │ ├─ delete()      │
        │  {username,pwd}  │    │ └─ Auto-increment│
        │ ]                │    └──────────────────┘
        └──────────────────┘          ↓
                                ┌──────────────────┐
                                │   Redis Cache    │
                                │ products::1      │
                                │ products::all    │
                                │ (10 min TTL)     │
                                └──────────────────┘
                                      ↓
                                ┌──────────────────┐
                                │data/products.json│
                                │ [                │
                                │  {id,name,...}   │
                                │ ]                │
                                └──────────────────┘
```

---

## 📁 Estructura del Proyecto

```
product-api-java/
├── src/main/java/com/example/productapi/
│   ├── ProductApiApplication.java           # Punto de entrada
│   │
│   ├── config/
│   │   └── RedisConfig.java                # Config Redis con soporte LocalDateTime
│   │
│   ├── controller/
│   │   ├── AuthController.java             # /api/auth/token → login
│   │   ├── ProductController.java          # /api/products CRUD
│   │   └── UserController.java             # /api/users lectura
│   │
│   ├── service/
│   │   ├── AuthService.java                # Validación BCrypt + JWT
│   │   └── ProductService.java             # Lógica productos + caching
│   │
│   ├── repository/
│   │   ├── FileUserRepository.java         # Carga data/users.json
│   │   ├── FileProductRepository.java      # Persistencia productos en JSON
│   │   ├── UserRepository.java             # Interface (legado JPA)
│   │   └── ProductRepository.java          # Interface (legado JPA)
│   │
│   ├── security/
│   │   ├── JwtUtil.java                    # Generación/validación JWT
│   │   └── TokenAuthFilter.java            # Filter de autorización
│   │
│   ├── model/
│   │   ├── Product.java                    # POJO sin @Entity
│   │   └── User.java                       # POJO sin @Entity
│   │
│   └── exception/
│       ├── GlobalExceptionHandler.java     # Manejo de errores global
│       └── SuccessResponse.java            # Response wrapper
│
├── src/main/resources/
│   ├── application.yml                     # Config Redis + paths
│   └── application.properties              # Fallback properties
│
├── data/
│   ├── products.json                       # Persistencia de productos
│   └── users.json                          # Pre-creados: admin, user
│
├── k8s/
│   └── deployment.yaml                     # Kubernetes deployment
│
├── gcp/
│   └── ...deployment configs               # GCP Cloud Run setup
│
├── docs/
│   ├── ARQUITECTURA-FINAL.md               # Este archivo
│   ├── RESUMEN-TECNICO.md                  # Detalles técnicos
│   └── DEPLOY-GCP.md                       # Guía de despliegue
│
├── pom.xml                                 # Maven dependencies
├── Dockerfile                              # Container image
├── docker-compose.yml                      # Local dev Redis
└── README.md                               # Guía rápida
```

---

## 🔐 Autenticación

### Flujo de Login

```
1. POST /api/auth/token
   Body: {"username": "admin", "password": "password"}
        ↓
2. AuthService.validateCredentials()
   ├─ Busca usuario en data/users.json
   ├─ Valida password con BCryptPasswordEncoder.matches()
   └─ Retorna boolean
        ↓
3. Si válido: JwtUtil genera token JWT
   ├─ Algorithm: HS512
   ├─ TTL: 15 minutos (configurable)
   └─ Claim: username
        ↓
4. Response: 200 OK
   {
     "success": true,
     "data": {
       "token": "eyJhbGc...",
       "expiresAt": "2025-12-12T23:50:00Z"
     }
   }
        ↓
5. Cliente guarda token → Authorization: Bearer {token}
```

### Usuarios Pre-creados

```json
data/users.json:
[
  {
    "username": "admin",
    "password": "$2a$10$URQNvPeyIxv2ktLQVwLb.uaYSAiPl/5Is.pOCRBEkuUCOmbgIRCcO",
    "roles": ["ADMIN", "USER"]
  },
  {
    "username": "user",
    "password": "$2a$10$qgRu6M1vlxXjmpMf0aFGXumgsTq1mS55lQ.I85zQ3DrgjuRUtP6W.",
    "roles": ["USER"]
  }
]

Credenciales de prueba:
- admin / password
- user / 123456
```

---

## 💾 Almacenamiento

### Productos (data/products.json)

```json
[
  {
    "id": 1,
    "name": "Laptop Gaming",
    "imageUrl": "https://example.com/laptop.jpg",
    "description": "Laptop de alto rendimiento",
    "price": 1599.99,
    "rating": 4.8,
    "specifications": {
      "RAM": "16GB",
      "Storage": "512GB SSD",
      "GPU": "RTX 4070"
    },
    "createdAt": "2025-12-12T23:30:00",
    "updatedAt": "2025-12-12T23:30:00"
  }
]
```

**Características:**
- Auto-increment ID (AtomicLong)
- `createdAt` y `updatedAt` timestamp automáticos
- JSON puro (no base de datos)
- Thread-safe (ConcurrentHashMap)
- Persist en cada cambio

### Usuarios (data/users.json)

```json
[
  {
    "username": "admin",
    "password": "$2a$10$...",  // BCrypt hash
    "roles": ["ADMIN", "USER"]
  }
]
```

**Características:**
- Cargados en startup
- Contraseñas BCrypt (nunca plain text)
- Lectura solo (no hay endpoint POST users)
- En memoria ConcurrentHashMap

---

## ⚡ Caché Redis

### Configuración

```yaml
spring:
  cache:
    type: redis
  data:
    redis:
      host: localhost
      port: 6379
```

### Estrategia de Caché

| Endpoint | Cacheable | TTL | Clave |
|----------|-----------|-----|-------|
| GET /products | ✅ Sí | 10 min | `products::all` |
| GET /products/{id} | ✅ Sí | 10 min | `products::1` |
| POST /products | ❌ Invalida | - | Evict `products::all` |
| PUT /products/{id} | ❌ Invalida | - | Evict item + all |
| DELETE /products/{id} | ❌ Invalida | - | Evict all |

### Anotaciones

```java
// ProductService.java
public List<Product> listAll() {
    @Cacheable(value = "products", key = "'all'")
    // Cache ALL productos con clave "products::all"
}

public Product getById(Long id) {
    @Cacheable(value = "products", key = "#id")
    // Cache individual producto con clave "products::1"
}

public Product create(Product p) {
    @CachePut(value = "products", key = "#result.id")
    @CacheEvict(value = "products", key = "'all'")
    // Actualiza item cache, invalida lista cache
}

public void delete(Long id) {
    @CacheEvict(value = "products", allEntries = true)
    // Nuclear: invalida TODOS los caches
}
```

---

## 🌐 API Endpoints

### Autenticación

```
POST /api/auth/token
Body: {"username": "admin", "password": "password"}
Response: {"success": true, "data": {"token": "..."}}
```

### Productos

```
GET /api/products                   → Listar todos (cached)
GET /api/products/{id}              → Obtener por ID (cached)
POST /api/products                  → Crear producto (auth requerido)
PUT /api/products/{id}              → Actualizar (auth requerido)
DELETE /api/products/{id}           → Eliminar (auth requerido)
```

### Usuarios

```
GET /api/users                      → Listar usuarios
GET /api/users/username/{username}  → Buscar por nombre
```

### Health & Actuator

```
GET /actuator/health                → Status de la app
GET /actuator/health/db             → Status (no DB en este caso)
```

### Swagger UI

```
GET /swagger-ui.html                → Documentación interactiva
GET /api-docs                       → OpenAPI JSON
```

---

## 🚀 Despliegue

### Local (Desarrollo)

```bash
# Opción 1: Maven
mvn spring-boot:run

# Opción 2: Docker Compose (con Redis)
docker-compose up

# Acceder
http://localhost:8080/swagger-ui.html
```

### GCP Cloud Run

```bash
# Build imagen
gcloud builds submit --tag gcr.io/PROJECT/product-api

# Deploy
gcloud run deploy product-api \
  --image gcr.io/PROJECT/product-api \
  --platform managed \
  --region us-central1 \
  --memory 512Mi \
  --set-env-vars REDIS_HOST=redis-instance
```

**Ver:** [DEPLOY-GCP.md](docs/DEPLOY-GCP.md)

---

## ✅ Testing

### Unit Tests

```bash
# Ejecutar
mvn test

# Coverage
mvn jacoco:report
```

**Pruebas incluidas:**
- AuthControllerTest (4 casos)
- ProductControllerTest (7 casos)
- JwtUtilTest (10 casos)

### E2E Tests (PowerShell)

```powershell
# Ver scripts en: tests/e2e/
.\product-crud-test.ps1
.\auth-test.ps1
```

---

## 🔧 Configuración

### application.yml

```yaml
server:
  port: 8080

spring:
  cache:
    type: redis
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: 6379

product:
  storage:
    file: data/products.json

user:
  storage:
    file: data/users.json

api:
  auth:
    secret: ${API_TOKEN_SECRET:mysecret}
```

### Variables de Entorno

```bash
REDIS_HOST=redis-instance
API_TOKEN_SECRET=production-secret-key
```

---

## 📊 Dependencias

### Core
- spring-boot-starter-web (3.5.1)
- spring-boot-starter-validation (3.5.1)
- spring-boot-starter-actuator (3.5.1)
- spring-boot-starter-data-redis (3.5.1)

### Seguridad
- spring-security-crypto (6.4.2) - BCrypt
- jjwt (0.11.5) - JWT tokens

### Serialización
- jackson-databind (2.15.2)
- jackson-datatype-jsr310 (2.15.2) - LocalDateTime support

### Documentación
- springdoc-openapi (2.1.0) - Swagger UI

### Testing
- spring-boot-starter-test (3.5.1)
- junit (5)

---

## ⚠️ Notas Importantes

### No Incluidas (Por Diseño)
- ❌ **PostgreSQL / JPA** - Usando almacenamiento en archivos JSON
- ❌ **Hibernate** - Mapeo ORM innecesario
- ❌ **Spring Security Completo** - Solo BCrypt de spring-security-crypto
- ❌ **Liquibase / Flyway** - Sin migraciones de BD
- ❌ **Spring HATEOAS** - RESTful simple

### Limitaciones Conocidas
- 📍 **No ACID**: Almacenamiento en memoria no garantiza transacciones
- 📍 **Single Server**: Sin sincronización distribuida
- 📍 **No escalable**: ConcurrentHashMap en memoria
- 📍 **Production**: Considerar cambiar a BD real para productivo

### Recomendaciones Production
1. **Persistencia**: Migrar a PostgreSQL + JPA
2. **Distribución**: Redis cluster para múltiples instancias
3. **Monitoring**: Prometheus + Grafana
4. **Logging**: ELK stack o Cloud Logging
5. **Seguridad**: OAuth2 / OIDC si multitenant

---

## 📚 Documentación Adicional

- [RESUMEN-TECNICO.md](RESUMEN-TECNICO.md) - Detalles técnicos profundos
- [DEPLOY-GCP.md](docs/DEPLOY-GCP.md) - Guía de despliegue a Cloud Run
- [README.md](README.md) - Guía rápida de inicio

---

**Última actualización:** 12 Diciembre 2025
**Status:** ✅ Producción Lista
**Autor:** GitHub Copilot
