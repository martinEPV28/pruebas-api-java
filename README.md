# Product API (Java - Spring Boot)

Proyecto ejemplo que implementa una pequeña API RESTful para devolver detalles de productos y permitir comparaciones.

## Características
- Java 21 LTS + Spring Boot 3.5.1
- Endpoints: listar, obtener, crear y eliminar productos
- Validación de entrada y manejo global de errores
- Datos en memoria (simulación con repositorio en memoria) — fácil de cambiar a base real (H2, PostgreSQL, etc.)
- Documentación OpenAPI (Swagger UI) disponible en `/swagger-ui.html` o `/api/docs`
- Dockerfile incluido
- Manifiesto Kubernetes para despliegue en GKE
- Tests unitarios con JUnit (incluidos)

## Requisitos
- **Java:** OpenJDK 21 LTS o superior
- **Maven:** 3.8.0+
- **Docker:** (opcional, para contenización)

## Ejecutar localmente

```bash
mvn clean package
java -jar target/product-api-0.0.1-SNAPSHOT.jar
```

La API quedará disponible en `http://localhost:8080/api/products`

## Docker
Construir imagen (con Java 21):
```bash
docker build -t gcr.io/PROJECT_ID/product-api:latest .
```

**Nota:** El `Dockerfile` incluido usa una imagen base con Java 21. Actualiza si necesitas una versión diferente.

## Despliegue a GCP (GKE) - resumen
1. Crear imagen y subir a Container Registry / Artifact Registry.
2. Crear clúster GKE.
3. Aplicar el manifiesto `k8s/deployment.yaml`.
4. Exponer servicio con LoadBalancer o Ingress según convenga.

## Validación en Visual Studio Code
- Abra la carpeta del proyecto en VS Code.
- Use la extensión 'Java Extension Pack' para importar y ejecutar el proyecto.
- Ejecutar tests: `mvn test`.

## Historial de cambios
- **v0.0.1** (Java 21 LTS): Migrado desde Java 17 a Java 21 LTS con Spring Boot 3.5.1. Todos los tests pasados, compilación sin errores.
- Rama de migración: `appmod/java-migration-20251210221522`

