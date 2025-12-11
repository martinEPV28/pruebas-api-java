# Product API (Java - Spring Boot)

Proyecto ejemplo que implementa una pequeña API RESTful para devolver detalles de productos y permitir comparaciones.

## Características
- Java 17 + Spring Boot
- Endpoints: listar, obtener, crear y eliminar productos
- Validación de entrada y manejo global de errores
- Datos en memoria (simulación con repositorio en memoria) — fácil de cambiar a base real (H2, PostgreSQL, etc.)
- Documentación OpenAPI (Swagger UI) disponible en `/swagger-ui.html` o `/api/docs`
- Dockerfile incluido
- Manifiesto Kubernetes para despliegue en GKE
- Tests unitarios con JUnit (incluidos)

## Ejecutar localmente
Requisitos: JDK 17, Maven

```bash
mvn clean package
java -jar target/product-api-0.0.1-SNAPSHOT.jar
```

La API quedará disponible en `http://localhost:8080/api/products`

## Docker
Construir imagen:
```bash
docker build -t gcr.io/PROJECT_ID/product-api:latest .
```

## Despliegue a GCP (GKE) - resumen
1. Crear imagen y subir a Container Registry / Artifact Registry.
2. Crear clúster GKE.
3. Aplicar el manifiesto `k8s/deployment.yaml`.
4. Exponer servicio con LoadBalancer o Ingress según convenga.

## Validación en Visual Studio Code
- Abra la carpeta del proyecto en VS Code.
- Use la extensión 'Java Extension Pack' para importar y ejecutar el proyecto.
- Ejecutar tests: `mvn test`.

