# Test simple para DELETE con respuesta exitosa

$baseUrl = "http://localhost:8080/api/products"

Write-Host "`n=== TEST DELETE CON RESPUESTA EXITOSA ===" -ForegroundColor Cyan

# Crear producto
Write-Host "`n1. Crear producto..." -ForegroundColor Yellow
$product = @{name='Delete Test';price=50;imageUrl='http://test.jpg';description='test';rating=3.5} | ConvertTo-Json
$r = Invoke-WebRequest -Uri $baseUrl -Method Post -Headers @{'Content-Type'='application/json'} -Body $product -UseBasicParsing
$id = ($r.Content | ConvertFrom-Json).id
Write-Host "   ID: $id" -ForegroundColor Green

# Eliminar producto
Write-Host "`n2. Eliminar producto $id..." -ForegroundColor Yellow
$r = Invoke-WebRequest -Uri "$baseUrl/$id" -Method Delete -Headers @{'Content-Type'='application/json'} -UseBasicParsing
Write-Host "   Status: $($r.StatusCode)" -ForegroundColor Green
Write-Host "   Response:" -ForegroundColor Green
$r.Content | ConvertFrom-Json | ConvertTo-Json | Write-Host -ForegroundColor Green

# Intentar eliminar inexistente
Write-Host "`n3. Intentar eliminar inexistente (999)..." -ForegroundColor Yellow
try {
    Invoke-WebRequest -Uri "$baseUrl/999" -Method Delete -Headers @{'Content-Type'='application/json'} -UseBasicParsing -ErrorAction Stop
} catch {
    $status = $_.Exception.Response.StatusCode.Value__
    $stream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    $body = $reader.ReadToEnd()
    Write-Host "   Status: $status" -ForegroundColor Red
    Write-Host "   Error Response:" -ForegroundColor Red
    $body | ConvertFrom-Json | ConvertTo-Json | Write-Host -ForegroundColor Red
}

Write-Host "`n=== TEST COMPLETADO ===" -ForegroundColor Cyan
