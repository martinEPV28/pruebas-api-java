# Prueba DELETE con error generico

Write-Host "=== PRUEBA DELETE CON ERROR GENERICO ===" -ForegroundColor Cyan

# Test 1: Crear un producto
Write-Host "`n[PASO 1] Crear producto..." -ForegroundColor Yellow
$newProduct = @{
    name = "Test Delete Product"
    price = 99.99
    imageUrl = "https://example.com/test.jpg"
    description = "Product to test delete error handling"
    rating = 4.5
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/products" -Method Post `
        -Headers @{"Content-Type" = "application/json"} -Body $newProduct -UseBasicParsing
    $productId = ($response.Content | ConvertFrom-Json).id
    Write-Host "Producto creado con ID: $productId" -ForegroundColor Green
}
catch {
    Write-Host "Error al crear producto: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# Test 2: Eliminar el producto
Write-Host "`n[PASO 2] Eliminar producto $productId..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/products/$productId" `
        -Method Delete -Headers @{"Content-Type" = "application/json"} -UseBasicParsing
    Write-Host "Eliminacion exitosa" -ForegroundColor Green
    Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
}
catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Intentar eliminar producto inexistente
Write-Host "`n[PASO 3] Intentar eliminar producto inexistente (999)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/products/999" `
        -Method Delete -Headers @{"Content-Type" = "application/json"} -UseBasicParsing -ErrorAction Stop
    Write-Host "ERROR: Deberia haber lanzado excepcion" -ForegroundColor Red
}
catch {
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.Value__
        Write-Host "Status: $statusCode" -ForegroundColor Green
        
        try {
            $stream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream)
            $content = $reader.ReadToEnd()
            Write-Host "Response JSON (error generico):" -ForegroundColor Green
            $content | ConvertFrom-Json | ConvertTo-Json -Depth 5 | Write-Host -ForegroundColor Green
        }
        catch {
            Write-Host "No se pudo leer la respuesta" -ForegroundColor Red
        }
    }
}

Write-Host "`n=== PRUEBA COMPLETADA ===" -ForegroundColor Cyan
Write-Host "El DELETE ahora retorna error generico sin exponer detalles" -ForegroundColor Green
