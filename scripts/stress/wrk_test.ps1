# wrk_test.ps1
# Requires `wrk` installed on the machine. Example install on Windows via scoop: `scoop install wrk`.

param(
    [string]$Url = "http://localhost:8080/api/products",
    [int]$DurationSeconds = 30,
    [int]$Threads = 4,
    [int]$Connections = 200,
    [int]$Rate = 1000
)

if (-not (Get-Command wrk -ErrorAction SilentlyContinue)) {
    Write-Host "wrk not found. Install it or use the PowerShell stress script." -ForegroundColor Yellow
    exit 1
}

Write-Host "Running wrk against $Url for $DurationSeconds seconds ($Threads threads, $Connections connections, target rate $Rate req/s)"

# Example: -R sets a fixed rate when supported (wrk2). If using wrk original, omit -R.
wrk -t $Threads -c $Connections -d ${DurationSeconds}s --latency -R $Rate $Url
