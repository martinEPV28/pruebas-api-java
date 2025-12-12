param(
    [int]$RequestsPerSecond = 1200,
    [int]$DurationSeconds = 30,
    [int]$Threads = 10
)

Write-Host "=== Iniciando prueba de carga ===" -ForegroundColor Cyan
Write-Host "Objetivo: $RequestsPerSecond req/s durante $DurationSeconds segundos" -ForegroundColor Yellow

# Obtener token primero
Write-Host "`nObteniendo token JWT..." -ForegroundColor Green
$body = @{username="admin"; password="password"; ttlSeconds=3600} | ConvertTo-Json
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/token" -Method POST -Body $body -ContentType "application/json"
    $token_data = $response.Content | ConvertFrom-Json
    $token = $token_data.data.token
    Write-Host "Token obtenido correctamente" -ForegroundColor Green
} catch {
    Write-Host "Error obteniendo token: $_" -ForegroundColor Red
    exit 1
}

# Calcular requests por thread
$requestsPerThread = [math]::Ceiling($RequestsPerSecond * $DurationSeconds / $Threads)
$delayMs = [math]::Floor(1000 * $Threads / $RequestsPerSecond)

Write-Host "`nConfiguración:" -ForegroundColor Yellow
Write-Host "  - Threads: $Threads"
Write-Host "  - Requests por thread: $requestsPerThread"
Write-Host "  - Delay entre requests: $delayMs ms"
Write-Host "  - Total requests esperados: $($requestsPerThread * $Threads)"

$global:successCount = 0
$global:errorCount = 0
$global:responseTimes = @()
$global:lock = [object]::new()

$scriptBlock = {
    param($token, $requests, $delayMs)
    
    $headers = @{Authorization="Bearer $token"}
    $localSuccess = 0
    $localErrors = 0
    $localTimes = @()
    
    for ($i = 0; $i -lt $requests; $i++) {
        $sw = [System.Diagnostics.Stopwatch]::StartNew()
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:8080/api/products" -Method GET -Headers $headers -TimeoutSec 10
            if ($response.StatusCode -eq 200) {
                $localSuccess++
            } else {
                $localErrors++
            }
        } catch {
            $localErrors++
        }
        $sw.Stop()
        $localTimes += $sw.ElapsedMilliseconds
        
        if ($delayMs -gt 0) {
            Start-Sleep -Milliseconds $delayMs
        }
    }
    
    return @{
        Success = $localSuccess
        Errors = $localErrors
        Times = $localTimes
    }
}

Write-Host "`nIniciando prueba..." -ForegroundColor Green
$startTime = Get-Date
$jobs = @()

for ($i = 0; $i -lt $Threads; $i++) {
    $jobs += Start-Job -ScriptBlock $scriptBlock -ArgumentList $token, $requestsPerThread, $delayMs
}

# Esperar a que terminen los jobs
$jobs | Wait-Job | Out-Null

# Recopilar resultados
foreach ($job in $jobs) {
    $result = Receive-Job -Job $job
    $global:successCount += $result.Success
    $global:errorCount += $result.Errors
    $global:responseTimes += $result.Times
    Remove-Job -Job $job
}

$endTime = Get-Date
$totalDuration = ($endTime - $startTime).TotalSeconds

Write-Host "`n=== Resultados ===" -ForegroundColor Cyan
Write-Host "Duración real: $([math]::Round($totalDuration, 2)) segundos" -ForegroundColor Yellow
Write-Host "Total requests: $($global:successCount + $global:errorCount)"
Write-Host "Exitosos: $global:successCount" -ForegroundColor Green
Write-Host "Errores: $global:errorCount" -ForegroundColor $(if($global:errorCount -gt 0){"Red"}else{"Green"})
Write-Host "Req/s real: $([math]::Round(($global:successCount + $global:errorCount) / $totalDuration, 2))"

if ($global:responseTimes.Count -gt 0) {
    $sortedTimes = $global:responseTimes | Sort-Object
    $avgTime = ($global:responseTimes | Measure-Object -Average).Average
    $minTime = ($global:responseTimes | Measure-Object -Minimum).Minimum
    $maxTime = ($global:responseTimes | Measure-Object -Maximum).Maximum
    $p50 = $sortedTimes[[math]::Floor($sortedTimes.Count * 0.50)]
    $p95 = $sortedTimes[[math]::Floor($sortedTimes.Count * 0.95)]
    $p99 = $sortedTimes[[math]::Floor($sortedTimes.Count * 0.99)]
    
    Write-Host "`nTiempos de respuesta (ms):" -ForegroundColor Yellow
    Write-Host "  - Min: $minTime ms"
    Write-Host "  - Avg: $([math]::Round($avgTime, 2)) ms"
    Write-Host "  - P50: $p50 ms"
    Write-Host "  - P95: $p95 ms"
    Write-Host "  - P99: $p99 ms"
    Write-Host "  - Max: $maxTime ms"
}

$successRate = [math]::Round(($global:successCount / ($global:successCount + $global:errorCount)) * 100, 2)
Write-Host "`nTasa de éxito: $successRate%" -ForegroundColor $(if($successRate -ge 99){"Green"}elseif($successRate -ge 95){"Yellow"}else{"Red"})

if ($successRate -ge 99 -and (($global:successCount + $global:errorCount) / $totalDuration) -ge $RequestsPerSecond) {
    Write-Host "`n✅ PRUEBA EXITOSA: El sistema soporta $RequestsPerSecond+ req/s" -ForegroundColor Green
} else {
    Write-Host "`n⚠️  ADVERTENCIA: El sistema no alcanzó el objetivo" -ForegroundColor Yellow
}
