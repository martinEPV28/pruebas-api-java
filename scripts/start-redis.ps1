# Starts Redis via Docker in the project root
param(
  [string]$Image = "redis:7-alpine",
  [int]$Port = 6379
)

Write-Host "Starting Redis container..."
if (-not (Test-Path "$PSScriptRoot\..\redis-data")) {
  New-Item -ItemType Directory -Path "$PSScriptRoot\..\redis-data" | Out-Null
}

docker run -d --name productapi-redis -p ${Port}:6379 -v "$PSScriptRoot\..\redis-data:/data" $Image redis-server --appendonly yes

Write-Host "Redis running on localhost:${Port}"