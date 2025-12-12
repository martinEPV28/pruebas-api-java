# 🏗️ Arquitectura del Sistema - Product API

## 📐 Visión General

Product API es una aplicación REST construida siguiendo el patrón **MVC (Model-View-Controller)** con arquitectura por capas.

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENTE                                  │
│  (Postman, Navegador, App Frontend, test-all-endpoints.ps1)    │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 │ HTTP/HTTPS
                 │
┌────────────────▼────────────────────────────────────────────────┐
│                     CAPA DE PRESENTACIÓN                         │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  🎮 Controllers (REST API)                                │  │
│  │  • ProductController    - CRUD productos                  │  │
│  │  • AuthController       - Autenticación JWT               │  │
│  │  • UserController       - Validación usuarios             │  │
│  │  • OpenApiController    - Documentación Swagger           │  │
│  └───────────────────────────────────────────────────────────┘  │
│                             │                                    │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  🔒 Security Filter Chain                                 │  │
│  │  • JwtAuthenticationFilter - Valida tokens                │  │
│  │  • JwtUtil                 - Genera/valida JWT            │  │
│  └───────────────────────────────────────────────────────────┘  │
│                             │                                    │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  ⚠️ Global Exception Handler                              │  │
│  │  • ResourceNotFoundException (404)                        │  │
│  │  • ValidationException (400)                              │  │
│  │  • Unauthorized (401)                                     │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────┬───────────────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────────────┐
│                     CAPA DE NEGOCIO                              │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  ⚙️ Services (Lógica de negocio)                          │  │
│  │  • ProductService  - Validaciones y reglas de productos   │  │
│  │  • AuthService     - Validación de credenciales           │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────┬───────────────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────────────┐
│                     CAPA DE PERSISTENCIA                         │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  🗄️ Repositories (JPA/Hibernate)                          │  │
│  │  • ProductRepository  - CRUD en tabla products            │  │
│  │  • UserRepository     - Consultas en tabla users          │  │
│  └───────────────────────────────────────────────────────────┘  │
│                             │                                    │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  📊 Models/Entities (JPA Entities)                        │  │
│  │  • Product  - id, name, imageUrl, description, price...  │  │
│  │  • User     - id, username, password_hash, created_at    │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────┬───────────────────────────────────────────────┘
                  │
                  │ JDBC
                  │
┌─────────────────▼───────────────────────────────────────────────┐
│                      BASE DE DATOS                               │
│  🐘 PostgreSQL 12+                                               │
│                                                                  │
│  Tablas:                                                         │
│  • products (id, name, image_url, description, price, rating,   │
│              specifications, features, created_at, updated_at)  │
│  • users (id, username, password_hash, created_at, updated_at)  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 🔐 Flujo de Autenticación JWT

```
┌─────────┐                                        ┌──────────────┐
│ Cliente │                                        │   Servidor   │
└────┬────┘                                        └──────┬───────┘
     │                                                    │
     │ 1. POST /api/auth/token                           │
     │    { username, password }                         │
     ├──────────────────────────────────────────────────►│
     │                                                    │
     │                        2. AuthService valida      │
     │                           contra BD (users table) │
     │                                      │             │
     │                                      ▼             │
     │                           ┌──────────────────┐    │
     │                           │  BCrypt verify   │    │
     │                           └──────────────────┘    │
     │                                      │             │
     │                        3. JwtUtil.generateToken() │
     │                           (ttl = 900s / 15min)    │
     │                                                    │
     │ 4. 200 OK: { token: "eyJhbGc...", expiresAt }    │
     │◄───────────────────────────────────────────────────┤
     │                                                    │
     │                                                    │
     │ 5. GET /api/products                              │
     │    Authorization: Bearer eyJhbGc...               │
     ├──────────────────────────────────────────────────►│
     │                                                    │
     │                   6. JwtAuthenticationFilter      │
     │                      • Extrae token del header    │
     │                      • JwtUtil.validateToken()    │
     │                      • JwtUtil.parseToken()       │
     │                      • Crea SecurityContext       │
     │                                                    │
     │                   7. Procesa request normal       │
     │                                                    │
     │ 8. 200 OK: [lista de productos]                  │
     │◄───────────────────────────────────────────────────┤
     │                                                    │
```

---

## 📊 Flujo CRUD de Productos

### GET /api/products
```
Cliente → ProductController.list()
        → ProductService.listAll()
        → ProductRepository.findAll()
        → PostgreSQL: SELECT * FROM products
        ← List<Product>
        ← 200 OK + JSON
```

### POST /api/products
```
Cliente → ProductController.create(@RequestBody Product)
        → Validación (@Validated)
        → ProductService.create(product)
        → ProductRepository.save(product)
        → PostgreSQL: INSERT INTO products...
        ← Product (con ID generado)
        ← 201 CREATED + Location header
```

### PUT /api/products/{id}
```
Cliente → ProductController.update(@PathVariable id, @RequestBody Product)
        → ProductService.update(id, product)
        → ProductRepository.findById(id)
        → ¿Existe? NO → throw ResourceNotFoundException (404)
                   SÍ → ProductRepository.save(updatedProduct)
                       → PostgreSQL: UPDATE products SET...
        ← Product actualizado
        ← 200 OK + JSON
```

### DELETE /api/products/{id}
```
Cliente → ProductController.delete(@PathVariable id)
        → ProductService.delete(id)
        → ProductRepository.findById(id)
        → ¿Existe? NO → throw ResourceNotFoundException (404)
                   SÍ → ProductRepository.deleteById(id)
                       → PostgreSQL: DELETE FROM products WHERE id=?
        ← SuccessResponse
        ← 200 OK + mensaje de éxito
```

---

## 🔄 Manejo de Excepciones

```
                     ┌─────────────────────┐
                     │  GlobalException    │
                     │     Handler         │
                     │   (@RestController  │
                     │    Advice)          │
                     └──────────┬──────────┘
                                │
              ┌─────────────────┼─────────────────┐
              │                 │                 │
              ▼                 ▼                 ▼
    ┌──────────────────┐ ┌──────────────┐ ┌─────────────────┐
    │ Resource         │ │ Validation   │ │ Any Exception   │
    │ NotFoundException│ │ Exception    │ │ (Catch-all)     │
    │                  │ │              │ │                 │
    │ → 404 NOT_FOUND  │ │ → 400 BAD_   │ │ → 500 INTERNAL_ │
    │   ErrorResponse  │ │   REQUEST    │ │   SERVER_ERROR  │
    └──────────────────┘ └──────────────┘ └─────────────────┘
```

**ErrorResponse Structure:**
```json
{
  "timestamp": "2025-12-12T17:30:45.123",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: 999",
  "path": "/api/products/999"
}
```

---

## 🛠️ Stack Tecnológico

### Backend Framework
- **Spring Boot 3.5.1**: Framework principal
- **Spring Web**: REST API
- **Spring Data JPA**: ORM
- **Spring Security**: Autenticación/Autorización

### Base de Datos
- **PostgreSQL 12+**: Base de datos relacional
- **Hibernate**: Implementación JPA
- **HikariCP**: Connection pooling

### Seguridad
- **JWT (JSON Web Tokens)**: Autenticación stateless
- **JJWT 0.12.3**: Librería JWT para Java
- **BCrypt**: Hashing de passwords

### Testing
- **JUnit 5**: Framework de testing
- **Mockito**: Mocking de dependencias
- **PowerShell**: Scripts de pruebas E2E

### Documentación
- **OpenAPI 3.0**: Especificación API
- **Swagger UI**: Interfaz interactiva
- **Springdoc**: Generación automática docs

### Build & Deploy
- **Maven 3.9.11**: Build tool
- **Docker**: Containerización
- **Java 21 LTS**: Lenguaje

---

## 📦 Modelo de Datos

### Tabla: products
```sql
CREATE TABLE products (
  id              SERIAL PRIMARY KEY,
  name            VARCHAR(255) NOT NULL,
  image_url       TEXT,
  description     TEXT,
  price           DECIMAL(10,2) NOT NULL CHECK (price >= 0),
  rating          DECIMAL(2,1) CHECK (rating >= 0 AND rating <= 5),
  specifications  JSONB,
  features        JSONB,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Tabla: users
```sql
CREATE TABLE users (
  id            SERIAL PRIMARY KEY,
  username      VARCHAR(50) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Relaciones:**
- No hay FK en v1.0 (cada tabla es independiente)
- En versiones futuras: `products.created_by → users.id`

---

## 🚀 Proceso de Deploy

```
1. Development
   ├── Código fuente (Java 21)
   ├── Tests (JUnit + E2E)
   └── Build (Maven)
          │
          ▼
2. Build Artifact
   ├── mvn clean package -DskipTests
   └── Genera: target/product-api-0.0.1-SNAPSHOT.jar
          │
          ▼
3. Containerización (Opcional)
   ├── Docker build
   └── Imagen: product-api:1.0
          │
          ▼
4. Deployment
   ├── Opción A: Standalone JAR
   │   └── java -jar product-api.jar
   ├── Opción B: Docker Container
   │   └── docker run -p 8080:8080 product-api:1.0
   └── Opción C: Cloud Platform
       └── Azure App Service / AWS Elastic Beanstalk
          │
          ▼
5. Configuración
   ├── Variables de entorno (.env)
   ├── PostgreSQL connection
   └── JWT secret key
          │
          ▼
6. Verificación
   ├── Health check: /actuator/health
   ├── Swagger UI: /swagger-ui.html
   └── API Docs: /api-docs
```

---

## 📈 Patrones de Diseño Aplicados

### 1. MVC (Model-View-Controller)
- **Model**: `Product.java`, `User.java`
- **View**: JSON responses (REST)
- **Controller**: `ProductController.java`, `AuthController.java`

### 2. Repository Pattern
- `ProductRepository extends JpaRepository`
- Abstracción del acceso a datos

### 3. Service Layer Pattern
- `ProductService`: Lógica de negocio centralizada
- Separación de responsabilidades

### 4. DTO Pattern
- `SuccessResponse`, `ErrorResponse`
- Separación modelo interno vs API responses

### 5. Dependency Injection
- `@Autowired` en servicios y repositorios
- IoC Container de Spring

### 6. Chain of Responsibility
- Filter Chain de Spring Security
- `JwtAuthenticationFilter` → otros filtros

### 7. Singleton Pattern
- Todos los `@Component`, `@Service`, `@Repository`
- Gestionado por Spring Container

---

## 🔍 Métricas y Monitoreo

### Spring Boot Actuator
```
GET /actuator/health    - Estado de la aplicación
GET /actuator/info      - Información del build
GET /actuator/metrics   - Métricas de performance
```

### Logs
- **Framework**: SLF4J + Logback
- **Niveles**: INFO, DEBUG, WARN, ERROR
- **Salida**: Console + file (configurable)

**Ejemplo de logs:**
```
2025-12-12 17:30:45.123 INFO  [main] c.e.p.ProductApiApplication : Started in 3.456s
2025-12-12 17:31:02.567 INFO  [http-nio-8080-exec-1] c.e.p.c.AuthController : 🔐 Token generado: admin
2025-12-12 17:31:15.890 INFO  [http-nio-8080-exec-2] c.e.p.c.ProductController : 📦 Creado: Laptop
```

---

## 🎯 Principios SOLID Aplicados

### Single Responsibility
- Cada clase tiene una única responsabilidad
- `ProductController` solo maneja HTTP, `ProductService` solo lógica de negocio

### Open/Closed
- Controllers abiertos a extensión (nuevos endpoints)
- Cerrados a modificación (no cambiar endpoints existentes)

### Liskov Substitution
- `JpaRepository` puede ser reemplazado por cualquier implementación

### Interface Segregation
- Interfaces específicas por funcionalidad
- `ProductRepository` solo métodos de productos

### Dependency Inversion
- Depende de abstracciones (`JpaRepository` interface)
- No de implementaciones concretas

---

## 📚 Referencias

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/3.5.1/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [JWT.io](https://jwt.io/)
- [OpenAPI 3.0 Specification](https://swagger.io/specification/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

---

**Última actualización**: 2025-12-12
