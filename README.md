# Product API - Java Spring Boot

API REST para gestión de productos con PostgreSQL, construida con Spring Boot 3.5.1 y Java 21.

## ✨ Características
- ✅ Java 21 LTS + Spring Boot 3.5.1
- ✅ PostgreSQL con JPA/Hibernate
- ✅ Variables de entorno configurables
- ✅ Swagger/OpenAPI 3.0 integrado
- ✅ Tests unitarios con JUnit 5
- ✅ Script de automatización (PowerShell)
- ✅ Dockerfile incluido
- ✅ Manejo global de excepciones
- ✅ Validación de datos

## 📋 Requisitos
- **Java:** OpenJDK 21 LTS o superior
- **Maven:** 3.9.11 (incluido en el proyecto)
- **PostgreSQL:** 12+ en localhost:5432
- **Docker:** (opcional)

## 🚀 Quick Start

### Opción 1: Script Automático (Recomendado)
```powershell
# Compilar, tests y ejecutar
.\build-and-run.ps1

# Sin tests (más rápido)
.\build-and-run.ps1 -SkipTests

# Solo ejecutar
.\build-and-run.ps1 -OnlyRun
```

### Opción 2: Comandos Manuales
```powershell
# Compilar y empaquetar
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean package -DskipTests

# Ejecutar
java -jar target/product-api-0.0.1-SNAPSHOT.jar
```

## 🔗 Acceder a la API

| Recurso | URL |
|---------|-----|
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **OpenAPI JSON** | http://localhost:8080/api-docs |
| **Health Check** | http://localhost:8080/actuator/health |
| **Productos** | http://localhost:8080/api/products |

## ⚙️ Configuración

### Variables de Entorno
Crear archivo `.env` en la raíz:
```env
SERVER_PORT=8080
DB_HOST=localhost
DB_PORT=5432
DB_NAME=product_api
DB_USERNAME=postgres
DB_PASSWORD=MartinDesarrollo28
```

O usar el template:
```bash
cp .env.example .env
```

### Base de Datos
Crear base de datos PostgreSQL:
```sql
CREATE DATABASE product_api;
```

## 📁 Estructura del Proyecto

```
├── docs/                          # 📚 Documentación
│   ├── COMANDOS.md               # Referencia completa
│   ├── QUICK-START.md            # Guía rápida  
│   └── REFERENCIA-RAPIDA.txt     # Cheat sheet
├── src/main/java/com/example/productapi/
│   ├── config/OpenApiConfig.java
│   ├── controller/ProductController.java
│   ├── service/ProductService.java
│   ├── repository/ProductRepository.java
│   ├── model/Product.java
│   ├── exception/GlobalExceptionHandler.java
│   └── ProductApiApplication.java
├── src/test/java/...ProductControllerTest.java
├── src/main/resources/application.yml
├── .env.example
├── build-and-run.ps1              # Script automatización
├── pom.xml
├── Dockerfile
└── README.md
```

## 🛠️ Comandos Principales

```powershell
# Compilar
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean compile

# Tests
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd test

# Build
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean package -DskipTests

# Ejecutar
java -jar target/product-api-0.0.1-SNAPSHOT.jar

# Ver más comandos
# docs/COMANDOS.md
```

## 📚 Documentación

- [QUICK-START.md](docs/QUICK-START.md) - Guía de inicio rápido
- [COMANDOS.md](docs/COMANDOS.md) - Referencia completa de comandos
- [REFERENCIA-RAPIDA.txt](docs/REFERENCIA-RAPIDA.txt) - Cheat sheet visual

## 🐛 Troubleshooting

### Error: "Connection refused" (Base de datos)
```powershell
Test-NetConnection -ComputerName localhost -Port 5432
```

### Error: "no main manifest attribute"
```powershell
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean package -DskipTests
```

### Error: Compilation
```powershell
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean compile
```

## 🐳 Docker

Construir imagen:
```bash
docker build -t product-api:1.0 .
docker run -p 8080:8080 -e DB_HOST=host.docker.internal product-api:1.0
```

## 📝 API Endpoints

| Método | Endpoint | Descripción |
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
