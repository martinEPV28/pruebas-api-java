# 🎯 Resumen Técnico - Product API

## 📋 Descripción General

**Product API** es una aplicación REST para gestión de productos con autenticación JWT, construida con Spring Boot 3.5.1 y Java 21.

---

## 🏗️ Arquitectura

### Patrón: MVC por Capas

```
Cliente HTTP
    ↓
Controller (REST Endpoints)
    ↓
Service (Lógica de Negocio)
    ↓
Repository (JPA)
    ↓
PostgreSQL
```

### Componentes Principales

| Capa | Clase | Responsabilidad |
|------|-------|-----------------|
| **Presentación** | `ProductController` | Expone endpoints REST CRUD |
| | `AuthController` | Genera tokens JWT |
| **Seguridad** | `JwtAuthenticationFilter` | Valida tokens en cada request |
| | `JwtUtil` | Genera y valida JWT |
| **Negocio** | `ProductService` | Validaciones y reglas |
| | `AuthService` | Valida credenciales con BCrypt |
| **Persistencia** | `ProductRepository` | Operaciones CRUD con JPA |
| | `UserRepository` | Consultas de usuarios |
| **Modelo** | `Product`, `User` | Entidades JPA |

---

## 🔐 Seguridad JWT (Sin Spring Security)

### ¿Por qué JWT y no Sessions?

- **Stateless**: No guarda sesiones en servidor (escalable)
- **Distribuido**: Token funciona en múltiples servidores
- **Mobile-friendly**: Ideal para apps móviles

### Flujo de Autenticación

```
1. Usuario envía username + password
   POST /api/auth/token
   
2. AuthService valida contra BD (users table)
   BCrypt.matches(password, password_hash)
   
3. Si válido → JwtUtil genera token (TTL: 15 min)
   Firma: HMAC-SHA256 con secret key
   
4. Cliente recibe token:
   { "token": "eyJhbGc...", "expiresAt": 1234567890 }
   
5. En cada request posterior:
   Header: Authorization: Bearer eyJhbGc...
   
6. JwtAuthenticationFilter valida token:
   ✓ Firma correcta
   ✓ No expirado
   ✓ Formato válido
```

### Dependencia Clave

```xml
<!-- Solo crypto, NO Spring Security completo -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
```

**¿Por qué solo crypto?**
- Solo necesitamos BCrypt para hashear passwords
- No queremos form login, sessions, CSRF de Spring Security
- JWT manejado manualmente con más control

---

## 📊 Modelo de Datos

### Tabla: products

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | SERIAL | PK, auto-increment |
| `name` | VARCHAR(255) | Nombre del producto |
| `image_url` | TEXT | URL imagen |
| `description` | TEXT | Descripción |
| `price` | DECIMAL(10,2) | Precio (≥ 0) |
| `rating` | DECIMAL(2,1) | Calificación (0-5) |
| `specifications` | JSONB | Especificaciones técnicas |
| `features` | JSONB | Características |
| `created_at` | TIMESTAMP | Fecha creación |
| `updated_at` | TIMESTAMP | Última actualización |

### Tabla: users

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | SERIAL | PK |
| `username` | VARCHAR(50) | Único, NOT NULL |
| `password_hash` | VARCHAR(255) | BCrypt hash |
| `created_at` | TIMESTAMP | Fecha registro |

---

## 🚀 Endpoints de la API

### Autenticación

```http
POST /api/auth/token
Body: { "username": "admin", "password": "newpass1234" }
→ 200: { "token": "eyJhbGc...", "expiresAt": 1234567890 }
→ 401: Credenciales inválidas
```

### Productos (requieren JWT)

```http
GET    /api/products           → Lista todos
GET    /api/products/{id}      → Obtiene uno
POST   /api/products           → Crea nuevo
PUT    /api/products/{id}      → Actualiza
DELETE /api/products/{id}      → Elimina

Header: Authorization: Bearer <token>
```

### Validación Usuarios

```http
GET /api/users/username/{username}
→ 200: { "id": 1, "username": "admin", "exists": true }
→ 404: Usuario no encontrado
```

---

## 🛠️ Stack Tecnológico

### Backend
- **Java 21 LTS** - Lenguaje (última versión de soporte largo)
- **Spring Boot 3.5.1** - Framework principal
- **Spring Data JPA** - ORM / Hibernate
- **PostgreSQL 12+** - Base de datos relacional

### Seguridad
- **JWT (JJWT 0.12.3)** - JSON Web Tokens
- **BCrypt** - Hashing de passwords (spring-security-crypto)

### Documentación
- **OpenAPI 3.0** - Especificación estándar
- **Swagger UI** - Interfaz interactiva en `/swagger-ui.html`
- **Custom Generator** - `OpenApiController` manual

### Testing
- **JUnit 5** - Tests unitarios (21 tests)
- **Mockito** - Mocking de dependencias
- **PowerShell** - Tests E2E (12 tests)

### Build & Deploy
- **Maven 3.9.11** - Build tool (wrapper incluido)
- **Docker** - Containerización
- **HikariCP** - Connection pooling

---

## 💡 Decisiones Técnicas Clave

### 1. ¿Por qué NO Springdoc OpenAPI?

**Problema:** Springdoc tiene bugs con JSON en entidades JPA
- `specifications` y `features` (JSONB) causaban crashes
- Error: "Schema generation failed for JSONB fields"

**Solución:** Generador OpenAPI manual (`OpenApiController`)
- Control total del JSON generado
- Sin dependencias problemáticas
- Swagger UI funciona perfecto

### 2. ¿Por qué solo spring-security-crypto?

**No necesitamos:**
- Form login ❌
- Sessions ❌
- CSRF protection ❌
- Remember-me ❌

**Solo necesitamos:**
- BCrypt para passwords ✅
- JWT manejado manualmente ✅

### 3. ¿Por qué JSONB en PostgreSQL?

- Almacena `specifications` y `features` como JSON nativo
- Permite queries dentro del JSON: `specifications->>'ram'`
- Más flexible que columnas individuales
- Indexable para performance

---

## 📈 Flujo de una Petición

### Ejemplo: Crear Producto

```
1. Cliente HTTP POST
   URL: http://localhost:8080/api/products
   Headers: 
     Authorization: Bearer eyJhbGc...
     Content-Type: application/json
   Body:
     {
       "name": "Laptop Gaming",
       "price": 1599.99,
       "rating": 4.8,
       "specifications": { "ram": "16GB", "cpu": "i7" }
     }
     
2. JwtAuthenticationFilter
   ✓ Extrae token del header
   ✓ Valida firma y expiración
   ✓ Crea SecurityContext con username
   
3. ProductController.create()
   ✓ Valida @RequestBody con Bean Validation
   ✓ Llama ProductService.create()
   
4. ProductService.create()
   ✓ Validaciones de negocio:
     - price >= 0
     - rating entre 0-5
     - name no vacío
   ✓ Llama ProductRepository.save()
   
5. ProductRepository (JPA)
   ✓ Hibernate genera SQL:
     INSERT INTO products (name, price, rating, specifications)
     VALUES ('Laptop Gaming', 1599.99, 4.8, '{"ram":"16GB",...}')
   ✓ PostgreSQL ejecuta y retorna ID generado
   
6. Respuesta al Cliente
   Status: 201 CREATED
   Headers:
     Location: /api/products/15
   Body:
     {
       "id": 15,
       "name": "Laptop Gaming",
       "price": 1599.99,
       ...
     }
```

---

## 🔍 Manejo de Errores

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // 404 Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    → 404: { "error": "Product not found with id: 999" }
    
    // 400 Bad Request (validación)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    → 400: { "error": "price: must be greater than 0" }
    
    // 401 Unauthorized (JWT inválido)
    @ExceptionHandler(UnauthorizedException.class)
    → 401: { "error": "Invalid or expired token" }
    
    // 500 Internal Server Error (catch-all)
    @ExceptionHandler(Exception.class)
    → 500: { "error": "Internal server error" }
}
```

---

## 🧪 Testing

### Cobertura de Tests

| Tipo | Cantidad | Herramienta | Cobertura |
|------|----------|-------------|-----------|
| **Unitarios** | 21 | JUnit 5 + Mockito | Controllers, Services, JWT |
| **E2E** | 12 | PowerShell | Todos los endpoints |
| **Total** | 33 | - | 100% endpoints |

### Tests Unitarios (JUnit)

```java
@Test
@DisplayName("GET /api/products/{id} - Debe retornar producto")
public void testGetProduct() {
    // Arrange: Mockear dependencias
    when(productService.getById(1L))
        .thenReturn(Optional.of(product));
    
    // Act: Ejecutar método
    ResponseEntity<Product> response = controller.get(1L);
    
    // Assert: Verificar resultado
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Laptop", response.getBody().getName());
    verify(productService).getById(1L);
}
```

### Tests E2E (PowerShell)

```powershell
# test-all-endpoints.ps1
# 1. Genera JWT
# 2. Crea 3 productos
# 3. Lista todos
# 4. Actualiza uno
# 5. Elimina uno
# 6. Valida errores 401/404
# → Genera evidencia en test-results-*.txt
```

---

## 📊 Métricas

### Performance

- **Startup**: ~3.5 segundos
- **Request (cached)**: ~20ms
- **Request (DB query)**: ~50-100ms
- **Connection pool**: HikariCP (10 conexiones)

### Tamaño

- **JAR**: ~55 MB
- **Docker image**: ~350 MB
- **RAM usage**: ~512 MB

---

## 🚀 Despliegue

### Opción 1: JAR Standalone

```bash
java -jar product-api-0.0.1-SNAPSHOT.jar
```

### Opción 2: Docker

```bash
docker build -t product-api:1.0 .
docker run -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_PASSWORD=xxx \
  product-api:1.0
```

### Opción 3: Cloud (Azure/AWS)

- Azure App Service
- AWS Elastic Beanstalk
- GCP App Engine

---

## ✅ Ventajas de la Arquitectura

1. **Stateless** - Escalable horizontalmente
2. **Desacoplada** - Frontend/Backend independientes
3. **RESTful** - Estándares HTTP
4. **Documentada** - Swagger UI integrado
5. **Testeada** - 33 tests automatizados
6. **Segura** - JWT + BCrypt
7. **Versionada** - Git + Maven versioning
8. **Containerizada** - Docker ready

---

## 📚 Documentación Completa

- [README.md](../README.md) - Guía principal
- [ARQUITECTURA.md](ARQUITECTURA.md) - Diagramas detallados
- [PRUEBAS.md](PRUEBAS.md) - Suite de tests
- [ENTREGABLES.md](ENTREGABLES.md) - Checklist entrega
- [Swagger UI](http://localhost:8080/swagger-ui.html) - Docs interactivos

---

## 🎓 Conceptos para Explicar

### Para nivel técnico:
- Autenticación stateless con JWT
- ORM con JPA/Hibernate
- Repository pattern
- Dependency injection
- Exception handling global
- Testing con mocks

### Para nivel negocio:
- API REST para gestión de productos
- Seguridad con tokens temporales
- CRUD completo (Crear, Leer, Actualizar, Eliminar)
- Documentación automática con Swagger
- Tests automatizados garantizan calidad

---

**Última actualización**: 2025-12-12
