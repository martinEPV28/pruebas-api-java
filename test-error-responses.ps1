# Script de prueba para respuestas de error JSON del Product API
# Tests para validar manejo de errores en diferentes escenarios

$baseUrl = "http://localhost:8080/api/products"
$headers = @{"Content-Type" = "application/json"}

Write-Host "`n=== PRUEBAS DE RESPUESTAS DE ERROR JSON ===" -ForegroundColor Cyan

# Test 1: GET producto no encontrado (404)
Write-Host "`n[TEST 1] GET - Producto no encontrado (404)" -ForegroundColor Yellow
Write-Host "URL: GET $baseUrl/999" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/999" -Method Get -Headers $headers -ErrorAction Stop
} catch {
    $statusCode = $_.Exception.Response.StatusCode.Value__
    $body = $_.Exception.Response.Content.ReadAsStream() | ForEach-Object { $sr = [System.IO.StreamReader]::new($_); $sr.ReadToEnd() }
    Write-Host "Status: $statusCode" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Green
    $body | ConvertFrom-Json | ConvertTo-Json -Depth 10 | Write-Host -ForegroundColor Green
}

# Test 2: POST con datos invalidos (400)
Write-Host "`n[TEST 2] POST - Datos invalidos (400)" -ForegroundColor Yellow
Write-Host "URL: POST $baseUrl" -ForegroundColor Gray

$invalidProduct = @{
    name = ""
    price = 100.00
    imageUrl = "https://example.com/image.jpg"
    description = "Product without name"
    rating = 4.5
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri $baseUrl -Method Post -Headers $headers -Body $invalidProduct -ErrorAction Stop
} catch {
    $statusCode = $_.Exception.Response.StatusCode.Value__
    $body = $_.Exception.Response.Content.ReadAsStream() | ForEach-Object { $sr = [System.IO.StreamReader]::new($_); $sr.ReadToEnd() }
    Write-Host "Status: $statusCode" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Green
    $body | ConvertFrom-Json | ConvertTo-Json -Depth 10 | Write-Host -ForegroundColor Green
}

# Test 3: POST producto valido (201)
Write-Host "`n[TEST 3] POST - Producto valido (201)" -ForegroundColor Yellow
Write-Host "URL: POST $baseUrl" -ForegroundColor Gray

$validProduct = @{
    name = "Test Product"
    price = 99.99
    imageUrl = "https://example.com/product.jpg"
    description = "A test product for error handling validation"
    rating = 4.8
    specifications = @{
        color = "blue"
        size = "medium"
    }
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri $baseUrl -Method Post -Headers $headers -Body $validProduct
    $productId = ($response.Content | ConvertFrom-Json).id
    Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Producto creado con ID: $productId" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Green
    $response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10 | Write-Host -ForegroundColor Green
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: PUT actualizar producto (200)
Write-Host "`n[TEST 4] PUT - Actualizar producto (200)" -ForegroundColor Yellow
Write-Host "URL: PUT $baseUrl/{id}" -ForegroundColor Gray

if ($productId) {
    $updateProduct = @{
        name = "Updated Test Product"
        price = 149.99
        description = "Updated description"
    } | ConvertTo-Json

    try {
        $response = Invoke-WebRequest -Uri "$baseUrl/$productId" -Method Put -Headers $headers -Body $updateProduct
        Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
        Write-Host "Response:" -ForegroundColor Green
        $response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10 | Write-Host -ForegroundColor Green
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.Value__
        $body = $_.Exception.Response.Content.ReadAsStream() | ForEach-Object { $sr = [System.IO.StreamReader]::new($_); $sr.ReadToEnd() }
        Write-Host "Status: $statusCode" -ForegroundColor Red
        Write-Host "Error:" -ForegroundColor Red
        $body | ConvertFrom-Json | ConvertTo-Json -Depth 10 | Write-Host -ForegroundColor Red
    }
}

# Test 5: DELETE producto (204)
Write-Host "`n[TEST 5] DELETE - Eliminar producto (204)" -ForegroundColor Yellow
Write-Host "URL: DELETE $baseUrl/{id}" -ForegroundColor Gray

if ($productId) {
    try {
        $response = Invoke-WebRequest -Uri "$baseUrl/$productId" -Method Delete -Headers $headers
        Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
        Write-Host "Producto eliminado exitosamente" -ForegroundColor Green
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.Value__
        Write-Host "Status: $statusCode" -ForegroundColor Red
    }
}

# Test 6: DELETE producto inexistente (404)
Write-Host "`n[TEST 6] DELETE - Producto inexistente (404)" -ForegroundColor Yellow
Write-Host "URL: DELETE $baseUrl/999" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/999" -Method Delete -Headers $headers -ErrorAction Stop
} catch {
    $statusCode = $_.Exception.Response.StatusCode.Value__
    $body = $_.Exception.Response.Content.ReadAsStream() | ForEach-Object { $sr = [System.IO.StreamReader]::new($_); $sr.ReadToEnd() }
    Write-Host "Status: $statusCode" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Green
    $body | ConvertFrom-Json | ConvertTo-Json -Depth 10 | Write-Host -ForegroundColor Green
}

Write-Host "`n=== PRUEBAS COMPLETADAS ===" -ForegroundColor Cyan
