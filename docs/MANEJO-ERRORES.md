# Manejo de Errores - Product API

## 📋 Visión General

La API utiliza un sistema centralizado de manejo de excepciones que devuelve respuestas JSON estandarizadas para todos los errores.

## 🏗️ Arquitectura

```
Controller
    ↓
Exception ocurre
    ↓
GlobalExceptionHandler captura
    ↓
Genera ErrorResponse en JSON
    ↓
Cliente recibe respuesta estructurada
```

## 📄 Estructura de Respuesta de Error

### Formato Base
```json
{
  "status": 404,
  "message": "Product no encontrado con id: '99'",
  "error": "Recurso no encontrado",
  "timestamp": "2025-12-11T21:15:30",
  "path": "/api/products/99"
}
```

### Con Detalles de Validación
```json
{
  "status": 400,
  "message": "Validación fallida",
  "error": "Hay errores en los datos enviados",
  "timestamp": "2025-12-11T21:15:30",
  "path": "/api/products",
  "details": [
    {
      "field": "name",
      "message": "no puede estar vacío",
      "rejectedValue": ""
    },
    {
      "field": "price",
      "message": "debe ser mayor que 0",
      "rejectedValue": -10
    }
  ]
}
```

## 🔍 Códigos de Estado HTTP

| Código | Situación | Ejemplo |
|--------|-----------|---------|
| **200** | OK | GET exitoso, recurso actualizado |
| **201** | Created | POST exitoso, recurso creado |
| **204** | No Content | DELETE exitoso |
| **400** | Bad Request | Datos inválidos, validación fallida |
| **404** | Not Found | Recurso no existe |
| **500** | Internal Server Error | Error no controlado en servidor |

## 🛡️ Excepciones Manejadas

### 1. ResourceNotFoundException (404)
**Cuándo ocurre:**
- Intentar obtener un producto que no existe
- Intentar actualizar un producto inexistente
- Intentar eliminar un producto inexistente

**Ejemplo:**
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/products/999" -UseBasicParsing
```

**Respuesta:**
```json
{
  "status": 404,
  "message": "Product no encontrado con id: '999'",
  "error": "Recurso no encontrado",
  "timestamp": "2025-12-11T21:15:30",
  "path": "/api/products/999"
}
```

### 2. MethodArgumentNotValidException (400)
**Cuándo ocurre:**
- Campos obligatorios vacíos
- Valores inválidos (ej: precio negativo)
- Tipos de datos incorrectos

**Ejemplo:**
```powershell
$body = @{
    name = ""
    price = -10
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/api/products" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body
```

**Respuesta:**
```json
{
  "status": 400,
  "message": "Validación fallida",
  "error": "Hay errores en los datos enviados",
  "timestamp": "2025-12-11T21:15:30",
  "path": "/api/products",
  "details": [
    {
      "field": "name",
      "message": "no puede estar vacío",
      "rejectedValue": ""
    },
    {
      "field": "price",
      "message": "debe ser mayor que 0",
      "rejectedValue": -10
    }
  ]
}
```

### 3. IllegalArgumentException (400)
**Cuándo ocurre:**
- Argumentos inválidos en métodos
- Lógica de negocio violada

**Respuesta:**
```json
{
  "status": 400,
  "message": "Descripción del argumento inválido",
  "error": "Argumento inválido",
  "timestamp": "2025-12-11T21:15:30",
  "path": "/api/products"
}
```

### 4. Exception (500)
**Cuándo ocurre:**
- Error de base de datos
- Error no controlado en servidor
- Excepción desconocida

**Respuesta:**
```json
{
  "status": 500,
  "message": "Error: Connection refused",
  "error": "Error desconocido",
  "timestamp": "2025-12-11T21:15:30",
  "path": "/api/products"
}
```

## 🔐 Crear Excepciones Personalizadas

### Pasos para agregar una nueva excepción:

#### 1. Crear clase de excepción
```java
// src/main/java/com/example/productapi/exception/CustomException.java
public class CustomException extends RuntimeException {
    public CustomException(String message) {
        super(message);
    }
}
```

#### 2. Agregar handler en GlobalExceptionHandler
```java
@ExceptionHandler(CustomException.class)
public ResponseEntity<ErrorResponse> handleCustomException(
        CustomException ex,
        ServletWebRequest request) {
    
    ErrorResponse error = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        ex.getMessage(),
        "Descripción del error",
        request.getRequest().getRequestURI()
    );
    
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
}
```

#### 3. Lanzar la excepción en el servicio
```java
public void someMethod() {
    if (condition) {
        throw new CustomException("Mensaje de error");
    }
}
```

## 📝 Ejemplo Completo: Validación en POST

```powershell
# Producto válido
$validProduct = @{
    name = "Laptop"
    imageUrl = "https://example.com/laptop.jpg"
    description = "Laptop Gaming"
    price = 1500
    rating = 4.5
    specifications = @{
        cpu = "Intel i7"
        ram = "16GB"
    }
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/api/products" `
    -Method POST `
    -ContentType "application/json" `
    -Body $validProduct

# Respuesta exitosa (201 Created)
# {
#   "id": 1,
#   "name": "Laptop",
#   ...
# }

# Producto inválido
$invalidProduct = @{
    name = ""
    price = -500
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/api/products" `
    -Method POST `
    -ContentType "application/json" `
    -Body $invalidProduct

# Respuesta con errores (400 Bad Request)
# {
#   "status": 400,
#   "message": "Validación fallida",
#   "error": "Hay errores en los datos enviados",
#   "details": [...]
# }
```

## 🧪 Testing de Errores

### Verificar 404
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/products/999" `
    -ErrorAction SilentlyContinue
```

### Verificar 400
```powershell
$body = '{"name":"","price":-10}' 
Invoke-WebRequest -Uri "http://localhost:8080/api/products" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body `
    -ErrorAction SilentlyContinue
```

### Ver respuesta completa
```powershell
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/products/999" `
    -ErrorAction SilentlyContinue

$response.Content | ConvertFrom-Json | ConvertTo-Json
```

## 📚 Archivos Relacionados

- `GlobalExceptionHandler.java` - Manejador centralizado de excepciones
- `ErrorResponse.java` - Clase para respuestas de error
- `ResourceNotFoundException.java` - Excepción personalizada
- `ProductController.java` - Controller que usa las excepciones
- `ProductService.java` - Service que lanza excepciones

## 💡 Mejores Prácticas

✅ **Hacer:**
- Lanzar excepciones específicas (ResourceNotFoundException)
- Proporcionar mensajes de error descriptivos
- Incluir detalles de validación por campo
- Loguear errores críticos

❌ **No hacer:**
- Devolver códigos de error en el cuerpo (200 con error dentro)
- Usar excepciones para control de flujo
- Exponer detalles internos del servidor
- Ignorar excepciones silenciosamente
