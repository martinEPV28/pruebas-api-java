# powershell_http_stress_test.ps1
# Simple concurrent load generator using .NET HttpClient and PowerShell runspaces.
# Usage example:
#   .\powershell_http_stress_test.ps1 -Url http://localhost:8080/api/products -DurationSeconds 30 -Concurrency 200

param(
    [string]$Url = "http://localhost:8080/api/products",
    [int]$Concurrency = 200,
    [int]$DurationSeconds = 30
)

Write-Host "Starting PowerShell HTTP stress test against $Url" -ForegroundColor Cyan

Add-Type -AssemblyName System.Net.Http

$cts = [System.Threading.CancellationTokenSource]::new()
$token = $cts.Token

# Create HttpClient (shared)
$handler = New-Object System.Net.Http.HttpClientHandler
$handler.AutomaticDecompression = [System.Net.DecompressionMethods]::GZip -bor [System.Net.DecompressionMethods]::Deflate
$client = [System.Net.Http.HttpClient]::new($handler)
$client.Timeout = [System.TimeSpan]::FromSeconds(10)

$success = 0
$fail = 0

function Run-Worker {
    param($id)
    while (-not $token.IsCancellationRequested) {
        try {
            $resp = $client.GetAsync($Url).GetAwaiter().GetResult()
            if ($resp.IsSuccessStatusCode) { [System.Threading.Interlocked]::Increment([ref]$success) | Out-Null }
            else { [System.Threading.Interlocked]::Increment([ref]$fail) | Out-Null }
            # No delay - attempt to reach high QPS
        } catch {
            [System.Threading.Interlocked]::Increment([ref]$fail) | Out-Null
        }
    }
}

# Start workers
$jobs = @()
for ($i=0; $i -lt $Concurrency; $i++) {
    $ps = [PowerShell]::Create()
    $ps.AddScript({ param($workerId, $scriptBlock) & $scriptBlock }).AddArgument($i).AddArgument({ Run-Worker $using:i }) | Out-Null
    $ps.Runspace = [runspacefactory]::CreateRunspace()
    $ps.Runspace.Open()
    $async = $ps.BeginInvoke()
    $jobs += @{ ps=$ps; async=$async }
}

Write-Host "Workers started: $Concurrency. Running for $DurationSeconds seconds..." -ForegroundColor Green
Start-Sleep -Seconds $DurationSeconds

# Stop
$cts.Cancel()

# Give workers a moment to finish
Start-Sleep -Seconds 1

Write-Host "Success: $success  Fail: $fail" -ForegroundColor Cyan

foreach ($j in $jobs) {
    try { $j.ps.EndInvoke($j.async) } catch { }
    try { $j.ps.Runspace.Close(); $j.ps.Dispose() } catch { }
}

Write-Host "Test finished." -ForegroundColor Cyan
