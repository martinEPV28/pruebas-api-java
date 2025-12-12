# Quick Start - Product API

## 🚀 Iniciar Rápidamente

### Opción 1: Usar el Script (RECOMENDADO)
```powershell
# Compilar y ejecutar
.\build-and-run.ps1

# O solo ejecutar (sin compilar)
.\build-and-run.ps1 -OnlyRun

# O solo compilar
.\build-and-run.ps1 -OnlyBuild

# Saltando tests (más rápido)
.\build-and-run.ps1 -SkipTests
```

### Opción 2: Comandos Manuales

#### 1️⃣ Compilar
```powershell
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean package -DskipTests
```

#### 2️⃣ Ejecutar
```powershell
java -jar target/product-api-0.0.1-SNAPSHOT.jar
```

---

## 📋 Comandos por Tarea

| Tarea | Comando |
|-------|---------|
| **Compilar** | `.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean compile` |
| **Tests** | `.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd test` |
| **Build** | `.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean package -DskipTests` |
| **Ejecutar** | `java -jar target/product-api-0.0.1-SNAPSHOT.jar` |
| **Limpiar** | `.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean` |
| **Ver errores** | `.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean compile 2>&1` |

---

## 🔗 URLs Principales

| Recurso | URL |
|---------|-----|
| **Swagger UI** | `http://localhost:8080/swagger-ui.html` |
| **API Docs** | `http://localhost:8080/api-docs` |
| **Health Check** | `http://localhost:8080/actuator/health` |
| **Listar Productos** | `http://localhost:8080/api/products` |

---

## 🐛 Resolver Errores Comunes

### Error: "no main manifest attribute"
```powershell
# Asegurar que se ejecuta repackage
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean package -DskipTests
```

### Error: "Connection refused" (BD)
```powershell
# Verificar que PostgreSQL está corriendo
Test-NetConnection -ComputerName localhost -Port 5432
```

### Error: "Compilation error"
```powershell
# Ver errores detallados
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean compile
```

---

## 📊 Ciclo de Desarrollo

```powershell
# 1. Compilar
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean compile

# 2. Tests
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd test

# 3. Build
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd package -DskipTests

# 4. Ejecutar
java -jar target/product-api-0.0.1-SNAPSHOT.jar

# 5. Verificar
Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing
```

---

## 💡 Tips

- **Script rápido**: Usa `.\build-and-run.ps1 -SkipTests` para saltarte tests
- **Puerto diferente**: `.\build-and-run.ps1 -Port 9090`
- **BD remota**: `.\build-and-run.ps1 -DbHost "192.168.1.100"`
- **Ver más**: Lee `docs/COMANDOS.md` para opciones avanzadas

---

## 📝 Archivos de Referencia

- `docs/COMANDOS.md` - Todos los comandos disponibles
- `build-and-run.ps1` - Script de automatización
- `.env` - Variables de entorno
- `.env.example` - Template de variables
- `application.yml` - Configuración de Spring Boot
