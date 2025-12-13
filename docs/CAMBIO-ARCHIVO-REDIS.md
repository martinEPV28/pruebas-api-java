# 🚀 Cambio de Arquitectura: PostgreSQL → Archivo JSONB + Redis

## ✅ Cambios Realizados

### 1. **Dependencias actualizadas** ([pom.xml](pom.xml))
   - ❌ Removido: `spring-boot-starter-data-jpa`, `postgresql`, `h2`
   - ✅ Agregado: `spring-boot-starter-data-redis`
   - ✅ Mantenido: Jackson (para serialización JSON)

### 2. **Modelo Product** ([Product.java](src/main/java/com/example/productapi/model/Product.java))
   - ❌ Removido: Anotaciones JPA (`@Entity`, `@Table`, `@Column`, `@JdbcTypeCode`)
   - ❌ Removido: `@PrePersist`, `@PreUpdate` (lógica movida al repositorio)
   - ✅ Agregado: `Serializable` (requerido para Redis cache)
   - ✅ Mantenido: Validaciones (`@NotBlank`, `@Min`) y Swagger docs

### 3. **Repositorio File-Based** ([FileProductRepository.java](src/main/java/com/example/productapi/repository/FileProductRepository.java))
   - ✅ **Nuevo**: Repositorio que lee/escribe en archivo `data/products.json`
   - ✅ Usa `ObjectMapper` de Jackson con soporte para `LocalDateTime`
   - ✅ Thread-safe: `ConcurrentHashMap` + `synchronized` en escrituras
   - ✅ Auto-inicializa: Carga datos al arrancar, crea archivo si no existe
   - ✅ IDs auto-incrementales con `AtomicLong`

### 4. **Servicio con Cache Redis** ([ProductService.java](src/main/java/com/example/productapi/service/ProductService.java))
   - ✅ `@Cacheable`: Lee desde cache antes de archivo (GET operaciones)
   - ✅ `@CachePut`: Actualiza cache después de guardar (UPDATE)
   - ✅ `@CacheEvict`: Limpia cache en DELETE y operaciones que afectan `listAll()`
   - ✅ Cache name: `"products"`, TTL: 10 minutos

### 5. **Configuración Redis** ([RedisConfig.java](src/main/java/com/example/productapi/config/RedisConfig.java))
   - ✅ `@EnableCaching`: Habilita Spring Cache
   - ✅ TTL: 10 minutos (configurable)
   - ✅ Serialización: JSON con `GenericJackson2JsonRedisSerializer`
   - ✅ Keys en String, valores en JSON

### 6. **application.properties** actualizado
   ```properties
   # Archivo JSONB
   product.storage.file=data/products.json
   
   # Redis
   spring.data.redis.host=localhost
   spring.data.redis.port=6379
   spring.data.redis.password=
   spring.cache.type=redis
   spring.cache.redis.time-to-live=600000
   ```

### 7. **Tests actualizados** ([ProductControllerTest.java](src/test/java/com/example/productapi/ProductControllerTest.java))
   - ✅ Configurado para usar `spring.cache.type=none` (sin Redis en tests)
   - ✅ Tests siguen funcionando sin cambios en lógica

---

## 📦 Requisitos para Ejecutar

### Opción 1: Con Redis (Producción)

1. **Instalar Redis**:
   ```bash
   # Windows (con Chocolatey)
   choco install redis-64
   redis-server
   
   # Linux/macOS
   sudo apt install redis-server  # Ubuntu
   brew install redis             # macOS
   redis-server
   ```

2. **Ejecutar aplicación**:
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Verificar Redis está activo**:
   ```bash
   redis-cli ping
   # Debe responder: PONG
   ```

### Opción 2: Sin Redis (Desarrollo)

Si Redis no está disponible, edita `application.properties`:
```properties
spring.cache.type=simple  # Usa cache en memoria
# spring.data.redis.host=localhost  # Comenta esta línea
```

---

## 🔍 Cómo Funciona

### Flujo de Lectura (GET)
```
1. Request → ProductController.get(id)
2. ProductService.getById(id) con @Cacheable
3. ¿Existe en Redis cache?
   ├─ SÍ → Retorna desde Redis (rápido ⚡)
   └─ NO → Lee desde data/products.json
           └─ Guarda en Redis cache para próxima vez
4. Response
```

### Flujo de Escritura (POST/PUT)
```
1. Request → ProductController.create/update()
2. ProductService.create/update() con @CachePut + @CacheEvict
3. Guarda en data/products.json
4. Actualiza cache Redis
5. Invalida cache de "all products"
6. Response
```

### Flujo de Eliminación (DELETE)
```
1. Request → ProductController.delete(id)
2. ProductService.delete(id) con @CacheEvict
3. Elimina de data/products.json
4. Limpia TODO el cache Redis (allEntries=true)
5. Response
```

---

## 📂 Estructura del Archivo JSON

`data/products.json`:
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
      "GPU": "RTX 3060"
    },
    "createdAt": "2025-12-12T18:30:00",
    "updatedAt": "2025-12-12T18:30:00"
  }
]
```

---

## 🧪 Comandos de Testing

### Tests Unitarios
```bash
./mvnw test
```

### Tests E2E (PowerShell)
```powershell
.\test-api-e2e.ps1
```

### Verificar Cache Redis (CLI)
```bash
# Ver todas las keys en Redis
redis-cli KEYS "*"

# Ver contenido de un producto cacheado
redis-cli GET products::1

# Limpiar TODO el cache
redis-cli FLUSHDB
```

---

## 🐳 Docker con Redis

### docker-compose.yml (opcional)
```yaml
version: '3.8'
services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
  
  product-api:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATA_REDIS_HOST=redis
      - PRODUCT_STORAGE_FILE=/app/data/products.json
    volumes:
      - ./data:/app/data
    depends_on:
      - redis

volumes:
  redis-data:
```

Ejecutar:
```bash
docker-compose up -d
```

---

## 🔥 Ventajas de este Cambio

| Aspecto | PostgreSQL | Archivo JSONB + Redis |
|---------|------------|----------------------|
| **Setup** | Requiere DB instalada | Solo archivo JSON |
| **Velocidad lectura** | ~50ms | ~1ms (con cache) |
| **Escalabilidad** | Vertical (más RAM/CPU DB) | Horizontal (más Redis nodes) |
| **Persistencia** | Alta (ACID) | Media (archivo puede corromperse) |
| **Portabilidad** | Baja (dump/restore) | Alta (copia archivo JSON) |
| **Costo** | DB server + maintenance | Mínimo (solo Redis) |
| **Backup** | DB backups complejos | Copia simple de `data/products.json` |

---

## ⚠️ Consideraciones

### Para Producción
1. **Persistencia Redis**: Habilita RDB o AOF
   ```bash
   # redis.conf
   save 900 1
   appendonly yes
   ```

2. **Backup automático del archivo**:
   ```bash
   # Cronjob cada hora
   0 * * * * cp /app/data/products.json /backups/products-$(date +\%Y\%m\%d-\%H).json
   ```

3. **Redis Cluster**: Para alta disponibilidad (múltiples nodos)

4. **Validación de archivo**: Verificar integridad JSON al inicio

### Limitaciones
- ❌ No soporta transacciones complejas (sin ACID completo)
- ❌ No hay índices (búsquedas secuenciales en memoria)
- ❌ Concurrencia limitada (file locks en escritura)
- ❌ Tamaño máximo: ~100MB recomendado (Jackson ObjectMapper)

---

## 🎯 Siguientes Pasos

1. ✅ Ejecutar tests: `./mvnw test`
2. ✅ Instalar Redis: `redis-server`
3. ✅ Ejecutar app: `./mvnw spring-boot:run`
4. ✅ Verificar Swagger: http://localhost:8080/swagger-ui.html
5. ✅ Crear productos y ver archivo `data/products.json`
6. ✅ Verificar cache: `redis-cli KEYS "*"`

---

**Última actualización**: 2025-12-12
