# Comandos de Utilidad - Product API

## Compilar el Proyecto

### Compilar sin ejecutar tests
```powershell
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean compile
```

### Compilar y ejecutar todos los tests
```powershell
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean test
```

### Compilar sin ejecutar tests (silencioso)
```powershell
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean compile -q
```

## Empaquetar (Build)

### Empaquetar saltando tests
```powershell
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean package -DskipTests
```

### Empaquetar ejecutando tests
```powershell
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean package
```

## Validar Errores

### Compilar y mostrar todos los errores
```powershell
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean compile 2>&1
```

### Buscar errores específicamente
```powershell
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean compile 2>&1 | findstr /I "error"
```

### Validar sintaxis Java
```powershell
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd -X clean compile
```

### Verificar dependencias
```powershell
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd dependency:tree
```

## Iniciar el Servicio

### Ejecutar el JAR empaquetado
```powershell
java -jar target/product-api-0.0.1-SNAPSHOT.jar
```

### Ejecutar con variables de entorno personalizadas
```powershell
$env:DB_HOST="localhost"; `
$env:DB_PORT="5432"; `
$env:DB_NAME="product_api"; `
$env:DB_USERNAME="postgres"; `
$env:DB_PASSWORD="MartinDesarrollo28"; `
java -jar target/product-api-0.0.1-SNAPSHOT.jar
```

### Ejecutar con puerto personalizado
```powershell
$env:SERVER_PORT="9090"; `
java -jar target/product-api-0.0.1-SNAPSHOT.jar
```

## Verificar el Servicio

### Verificar que el servicio está corriendo (desde otra terminal)
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing
```

### Acceder a Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### Acceder a OpenAPI JSON
```
http://localhost:8080/api-docs
```

### Listar productos (API)
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/products" -UseBasicParsing
```

## Limpiar

### Limpiar archivos generados
```powershell
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean
```

### Eliminar JAR empaquetado
```powershell
Remove-Item target\product-api-0.0.1-SNAPSHOT.jar
```

### Eliminar todo el target
```powershell
Remove-Item -Recurse target\
```

## Ciclo Completo Recomendado

```powershell
# 1. Compilar y validar
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean compile

# 2. Ejecutar tests
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd test

# 3. Empaquetar
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd package -DskipTests

# 4. Ejecutar
java -jar target/product-api-0.0.1-SNAPSHOT.jar
```

## Cargar Variables de Entorno desde .env

### Opción 1: Manualmente en PowerShell
```powershell
Get-Content .env | ForEach-Object {
    if (-not $_.StartsWith('#') -and $_ -ne '') {
        [Environment]::SetEnvironmentVariable($_.Split('=')[0], $_.Split('=')[1])
    }
}
java -jar target/product-api-0.0.1-SNAPSHOT.jar
```

### Opción 2: Ejecutar script (build-and-run.ps1)
```powershell
.\build-and-run.ps1
```

## Logs y Debugging

### Ejecutar con logs detallados
```powershell
java -jar target/product-api-0.0.1-SNAPSHOT.jar --debug
```

### Ver logs de Hibernate
```powershell
$env:LOG_LEVEL="DEBUG"; `
java -jar target/product-api-0.0.1-SNAPSHOT.jar
```

## Troubleshooting

### Error: "no main manifest attribute"
**Solución**: Asegurar que spring-boot-maven-plugin tiene la configuración `repackage`
```powershell
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd clean package -DskipTests
```

### Error: "Could not determine recommended JdbcType for Java type 'java.util.Map'"
**Solución**: Asegurar que `@JdbcTypeCode(SqlTypes.JSON)` está presente en Product.java

### Error: Connection to database refused
**Solución**: Verificar que PostgreSQL está corriendo
```powershell
Test-NetConnection -ComputerName localhost -Port 5432
```

### Maven no encontrado
**Solución**: Usar el wrapper
```powershell
.mvn\wrapper\apache-maven-3.9.11\bin\mvn.cmd --version
```
