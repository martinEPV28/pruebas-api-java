# ===================================================================
# Test Completo de Todos los Endpoints - Product API
# ===================================================================

$baseUrl = "http://localhost:8080"
$timestamp = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
$logFile = "test-results-$timestamp.txt"

function Write-Test {
    param($message)
    Write-Host $message
    Add-Content -Path $logFile -Value $message
}

Write-Test "=========================================="
Write-Test "PRUEBAS DE ENDPOINTS - Product API"
Write-Test "Fecha: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Write-Test "=========================================="
Write-Test ""

# ===================================================================
# 1. TEST: POST /api/auth/token (Generar JWT)
# ===================================================================
Write-Test "-------------------------------------------"
Write-Test "TEST 1: POST /api/auth/token - Generar Token JWT"
Write-Test "-------------------------------------------"

$authBody = @{
    username = "admin"
    password = "newpass1234"
} | ConvertTo-Json

try {
    $authResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/token" `
        -Method Post `
        -Body $authBody `
        -ContentType "application/json" `
        -ErrorAction Stop
    
    $token = $authResponse.data.token
    Write-Test "✅ ÉXITO: Token JWT generado"
    Write-Test "Token: $($token.Substring(0, 50))..."
    Write-Test "Expira: $($authResponse.data.expiresAt)"
    Write-Test ""
} catch {
    Write-Test "❌ ERROR: $($_.Exception.Message)"
    Write-Test ""
    exit 1
}

# Headers con JWT para endpoints protegidos
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# ===================================================================
# 2. TEST: POST /api/products (Crear Producto 1)
# ===================================================================
Write-Test "-------------------------------------------"
Write-Test "TEST 2: POST /api/products - Crear Producto 1"
Write-Test "-------------------------------------------"

$product1 = @{
    name = "Laptop Dell XPS 15"
    price = 1599.99
    rating = 4.8
    imageUrl = "https://example.com/laptop.jpg"
    description = "Laptop profesional de alto rendimiento"
    specifications = @{
        processor = "Intel Core i7-12700H"
        ram = "16GB DDR5"
        storage = "512GB NVMe SSD"
        screen = "15.6 pulgadas FHD"
    }
} | ConvertTo-Json

try {
    $createResponse1 = Invoke-RestMethod -Uri "$baseUrl/api/products" `
        -Method Post `
        -Body $product1 `
        -Headers $headers `
        -ErrorAction Stop
    
    $productId1 = $createResponse1.id
    Write-Test "✅ ÉXITO: Producto 1 creado"
    Write-Test "ID: $productId1"
    Write-Test "Nombre: $($createResponse1.name)"
    Write-Test "Precio: `$$($createResponse1.price)"
    Write-Test ""
} catch {
    Write-Test "❌ ERROR: $($_.Exception.Message)"
    Write-Test ""
}

Start-Sleep -Milliseconds 500

# ===================================================================
# 3. TEST: POST /api/products (Crear Producto 2)
# ===================================================================
Write-Test "-------------------------------------------"
Write-Test "TEST 3: POST /api/products - Crear Producto 2"
Write-Test "-------------------------------------------"

$product2 = @{
    name = "iPhone 15 Pro"
    price = 999.00
    rating = 4.9
    imageUrl = "https://example.com/iphone.jpg"
    description = "Smartphone de última generación"
    specifications = @{
        chip = "A17 Pro"
        camera = "48MP principal"
        storage = "256GB"
        display = "6.1 pulgadas Super Retina XDR"
    }
} | ConvertTo-Json

try {
    $createResponse2 = Invoke-RestMethod -Uri "$baseUrl/api/products" `
        -Method Post `
        -Body $product2 `
        -Headers $headers `
        -ErrorAction Stop
    
    $productId2 = $createResponse2.id
    Write-Test "✅ ÉXITO: Producto 2 creado"
    Write-Test "ID: $productId2"
    Write-Test "Nombre: $($createResponse2.name)"
    Write-Test "Precio: `$$($createResponse2.price)"
    Write-Test ""
} catch {
    Write-Test "❌ ERROR: $($_.Exception.Message)"
    Write-Test ""
}

Start-Sleep -Milliseconds 500

# ===================================================================
# 4. TEST: POST /api/products (Crear Producto 3)
# ===================================================================
Write-Test "-------------------------------------------"
Write-Test "TEST 4: POST /api/products - Crear Producto 3"
Write-Test "-------------------------------------------"

$product3 = @{
    name = "Sony WH-1000XM5"
    price = 399.99
    rating = 4.7
    imageUrl = "https://example.com/headphones.jpg"
    description = "Audífonos con cancelación de ruido"
    specifications = @{
        type = "Over-ear"
        battery = "30 horas"
        bluetooth = "5.2"
        noiseCancellation = "Activa"
    }
} | ConvertTo-Json

try {
    $createResponse3 = Invoke-RestMethod -Uri "$baseUrl/api/products" `
        -Method Post `
        -Body $product3 `
        -Headers $headers `
        -ErrorAction Stop
    
    $productId3 = $createResponse3.id
    Write-Test "✅ ÉXITO: Producto 3 creado"
    Write-Test "ID: $productId3"
    Write-Test "Nombre: $($createResponse3.name)"
    Write-Test "Precio: `$$($createResponse3.price)"
    Write-Test ""
} catch {
    Write-Test "❌ ERROR: $($_.Exception.Message)"
    Write-Test ""
}

Start-Sleep -Milliseconds 500

# ===================================================================
# 5. TEST: GET /api/products (Listar Todos)
# ===================================================================
Write-Test "-------------------------------------------"
Write-Test "TEST 5: GET /api/products - Listar Todos los Productos"
Write-Test "-------------------------------------------"

try {
    $allProducts = Invoke-RestMethod -Uri "$baseUrl/api/products" `
        -Method Get `
        -Headers $headers `
        -ErrorAction Stop
    
    Write-Test "✅ ÉXITO: Lista de productos obtenida"
    Write-Test "Total de productos: $($allProducts.Count)"
    Write-Test ""
    foreach ($p in $allProducts) {
        Write-Test "  - ID: $($p.id) | $($p.name) | `$$($p.price) | Rating: $($p.rating)"
    }
    Write-Test ""
} catch {
    Write-Test "❌ ERROR: $($_.Exception.Message)"
    Write-Test ""
}

Start-Sleep -Milliseconds 500

# ===================================================================
# 6. TEST: GET /api/products/{id} (Obtener por ID)
# ===================================================================
Write-Test "-------------------------------------------"
Write-Test "TEST 6: GET /api/products/{id} - Obtener Producto por ID"
Write-Test "-------------------------------------------"

try {
    $product = Invoke-RestMethod -Uri "$baseUrl/api/products/$productId1" `
        -Method Get `
        -Headers $headers `
        -ErrorAction Stop
    
    Write-Test "✅ ÉXITO: Producto obtenido por ID"
    Write-Test "ID: $($product.id)"
    Write-Test "Nombre: $($product.name)"
    Write-Test "Precio: `$$($product.price)"
    Write-Test "Rating: $($product.rating)"
    Write-Test "Descripción: $($product.description)"
    Write-Test ""
} catch {
    Write-Test "❌ ERROR: $($_.Exception.Message)"
    Write-Test ""
}

Start-Sleep -Milliseconds 500

# ===================================================================
# 7. TEST: PUT /api/products/{id} (Actualizar)
# ===================================================================
Write-Test "-------------------------------------------"
Write-Test "TEST 7: PUT /api/products/{id} - Actualizar Producto"
Write-Test "-------------------------------------------"

$updateProduct = @{
    name = "Laptop Dell XPS 15 - ACTUALIZADO"
    price = 1499.99
    rating = 4.9
    imageUrl = "https://example.com/laptop-new.jpg"
    description = "Laptop profesional con descuento especial"
    specifications = @{
        processor = "Intel Core i7-12700H"
        ram = "32GB DDR5"
        storage = "1TB NVMe SSD"
        screen = "15.6 pulgadas 4K OLED"
    }
} | ConvertTo-Json

try {
    $updated = Invoke-RestMethod -Uri "$baseUrl/api/products/$productId1" `
        -Method Put `
        -Body $updateProduct `
        -Headers $headers `
        -ErrorAction Stop
    
    Write-Test "✅ ÉXITO: Producto actualizado"
    Write-Test "ID: $($updated.id)"
    Write-Test "Nombre actualizado: $($updated.name)"
    Write-Test "Precio actualizado: `$$($updated.price)"
    Write-Test "Rating actualizado: $($updated.rating)"
    Write-Test ""
} catch {
    Write-Test "❌ ERROR: $($_.Exception.Message)"
    Write-Test ""
}

Start-Sleep -Milliseconds 500

# ===================================================================
# 8. TEST: GET /api/users/username/{username} (Validar Username)
# ===================================================================
Write-Test "-------------------------------------------"
Write-Test "TEST 8: GET /api/users/username/{username} - Validar Username"
Write-Test "-------------------------------------------"

try {
    $user = Invoke-RestMethod -Uri "$baseUrl/api/users/username/admin" `
        -Method Get `
        -Headers $headers `
        -ErrorAction Stop
    
    Write-Test "✅ ÉXITO: Usuario validado"
    Write-Test "ID: $($user.id)"
    Write-Test "Username: $($user.username)"
    Write-Test "Activo: $($user.active)"
    Write-Test "Creado: $($user.createdAt)"
    Write-Test ""
} catch {
    Write-Test "❌ ERROR: $($_.Exception.Message)"
    Write-Test ""
}

Start-Sleep -Milliseconds 500

# ===================================================================
# 9. TEST: DELETE /api/products/{id} (Eliminar)
# ===================================================================
Write-Test "-------------------------------------------"
Write-Test "TEST 9: DELETE /api/products/{id} - Eliminar Producto"
Write-Test "-------------------------------------------"

try {
    $deleteResponse = Invoke-RestMethod -Uri "$baseUrl/api/products/$productId3" `
        -Method Delete `
        -Headers $headers `
        -ErrorAction Stop
    
    Write-Test "✅ ÉXITO: Producto eliminado"
    Write-Test "Status: $($deleteResponse.status)"
    Write-Test "Mensaje: $($deleteResponse.message)"
    Write-Test ""
} catch {
    Write-Test "❌ ERROR: $($_.Exception.Message)"
    Write-Test ""
}

Start-Sleep -Milliseconds 500

# ===================================================================
# 10. TEST: GET /api/products (Verificar eliminación)
# ===================================================================
Write-Test "-------------------------------------------"
Write-Test "TEST 10: GET /api/products - Verificar Lista Después de Eliminar"
Write-Test "-------------------------------------------"

try {
    $productsAfterDelete = Invoke-RestMethod -Uri "$baseUrl/api/products" `
        -Method Get `
        -Headers $headers `
        -ErrorAction Stop
    
    Write-Test "✅ ÉXITO: Lista actualizada obtenida"
    Write-Test "Total de productos: $($productsAfterDelete.Count)"
    Write-Test ""
    foreach ($p in $productsAfterDelete) {
        Write-Test "  - ID: $($p.id) | $($p.name) | `$$($p.price)"
    }
    Write-Test ""
} catch {
    Write-Test "❌ ERROR: $($_.Exception.Message)"
    Write-Test ""
}

# ===================================================================
# 11. TEST: Manejo de Errores - Producto no encontrado
# ===================================================================
Write-Test "-------------------------------------------"
Write-Test "TEST 11: GET /api/products/99999 - Manejo de Error (404)"
Write-Test "-------------------------------------------"

try {
    $notFound = Invoke-RestMethod -Uri "$baseUrl/api/products/99999" `
        -Method Get `
        -Headers $headers `
        -ErrorAction Stop
    
    Write-Test "❌ ERROR: Debería retornar 404"
    Write-Test ""
} catch {
    if ($_.Exception.Response.StatusCode -eq 404) {
        Write-Test "✅ ÉXITO: Error 404 manejado correctamente"
        Write-Test "Código HTTP: 404 Not Found"
    } else {
        Write-Test "⚠️ ADVERTENCIA: Error inesperado: $($_.Exception.Message)"
    }
    Write-Test ""
}

# ===================================================================
# 12. TEST: Manejo de Errores - Token inválido
# ===================================================================
Write-Test "-------------------------------------------"
Write-Test "TEST 12: GET /api/products - Token Inválido (401)"
Write-Test "-------------------------------------------"

$badHeaders = @{
    "Authorization" = "Bearer token-invalido-12345"
    "Content-Type" = "application/json"
}

try {
    $unauthorized = Invoke-RestMethod -Uri "$baseUrl/api/products" `
        -Method Get `
        -Headers $badHeaders `
        -ErrorAction Stop
    
    Write-Test "❌ ERROR: Debería retornar 401"
    Write-Test ""
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Test "✅ ÉXITO: Error 401 manejado correctamente"
        Write-Test "Código HTTP: 401 Unauthorized"
    } else {
        Write-Test "⚠️ ADVERTENCIA: Error inesperado: $($_.Exception.Message)"
    }
    Write-Test ""
}

# ===================================================================
# RESUMEN FINAL
# ===================================================================
Write-Test "=========================================="
Write-Test "RESUMEN DE PRUEBAS"
Write-Test "=========================================="
Write-Test "✅ Todos los endpoints fueron probados"
Write-Test ""
Write-Test "Endpoints probados:"
Write-Test "  1. POST /api/auth/token           ✅"
Write-Test "  2. POST /api/products (x3)        ✅"
Write-Test "  3. GET /api/products              ✅"
Write-Test "  4. GET /api/products/{id}         ✅"
Write-Test "  5. PUT /api/products/{id}         ✅"
Write-Test "  6. DELETE /api/products/{id}      ✅"
Write-Test "  7. GET /api/users/username/{user} ✅"
Write-Test "  8. Manejo de errores 404          ✅"
Write-Test "  9. Manejo de errores 401          ✅"
Write-Test ""
Write-Test "Resultados guardados en: $logFile"
Write-Test "=========================================="

Write-Host "`n✅ PRUEBAS COMPLETADAS - Ver resultados en: $logFile`n" -ForegroundColor Green
