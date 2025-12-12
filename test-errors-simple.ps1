# Script simplificado de pruebas de error responses

$baseUrl = "http://localhost:8080/api/products"

Write-Host "`n=== PRUEBAS DE RESPUESTAS DE ERROR JSON ===" -ForegroundColor Cyan

# Funcion para manejar respuestas de error
function Test-Endpoint {
    param(
        [string]$Method,
        [string]$Endpoint,
        [string]$Body,
        [string]$TestName
    )
    
    Write-Host "`n[$TestName]" -ForegroundColor Yellow
    Write-Host "$Method $Endpoint" -ForegroundColor Gray
    
    try {
        $params = @{
            Uri = $Endpoint
            Method = $Method
            Headers = @{"Content-Type" = "application/json"}
            UseBasicParsing = $true
        }
        
        if ($Body) {
            $params['Body'] = $Body
        }
        
        $response = Invoke-WebRequest @params
        Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
        Write-Host "Response:" -ForegroundColor Green
        $response.Content | Write-Host -ForegroundColor Green
    }
    catch {
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode.Value__
            Write-Host "Status: $statusCode" -ForegroundColor Red
            
            try {
                $stream = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($stream)
                $content = $reader.ReadToEnd()
                Write-Host "Response:" -ForegroundColor Red
                Write-Host $content -ForegroundColor Red
            }
            catch {
                Write-Host "No response body available" -ForegroundColor Red
            }
        }
        else {
            Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

# Test 1: GET producto no encontrado
Test-Endpoint -Method "GET" -Endpoint "$baseUrl/999" -TestName "TEST 1: GET producto no encontrado (404)"

# Test 2: POST con datos invalidos
$invalidProduct = @{
    name = ""
    price = 100.00
    imageUrl = "https://example.com/image.jpg"
} | ConvertTo-Json

Test-Endpoint -Method "POST" -Endpoint "$baseUrl" -Body $invalidProduct -TestName "TEST 2: POST datos invalidos (400)"

# Test 3: POST producto valido
$validProduct = @{
    name = "Test Product"
    price = 99.99
    imageUrl = "https://example.com/product.jpg"
    description = "Test product"
    rating = 4.8
} | ConvertTo-Json

Write-Host "`n[TEST 3] POST producto valido (201)" -ForegroundColor Yellow
Write-Host "POST $baseUrl" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri $baseUrl -Method Post -Headers @{"Content-Type" = "application/json"} -Body $validProduct -UseBasicParsing
    $productId = ($response.Content | ConvertFrom-Json).id
    Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Producto creado con ID: $productId" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Green
    Write-Host $response.Content -ForegroundColor Green
}
catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    $productId = $null
}

# Test 4: PUT actualizar
if ($productId) {
    $updateProduct = @{
        name = "Updated Product"
        price = 149.99
    } | ConvertTo-Json
    
    Test-Endpoint -Method "PUT" -Endpoint "$baseUrl/$productId" -Body $updateProduct -TestName "TEST 4: PUT actualizar (200)"
}

# Test 5: DELETE producto
if ($productId) {
    Test-Endpoint -Method "DELETE" -Endpoint "$baseUrl/$productId" -TestName "TEST 5: DELETE eliminar (204)"
}

# Test 6: DELETE producto inexistente
Test-Endpoint -Method "DELETE" -Endpoint "$baseUrl/999" -TestName "TEST 6: DELETE no encontrado (404)"

Write-Host "`n=== PRUEBAS COMPLETADAS ===" -ForegroundColor Cyan
Write-Host "Los errores se manejan automaticamente con respuestas JSON estandarizadas" -ForegroundColor Green
