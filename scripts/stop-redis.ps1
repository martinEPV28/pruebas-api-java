# Stops and removes the Redis Docker container
Write-Host "Stopping Redis container..."
docker stop productapi-redis | Out-Null
Write-Host "Removing Redis container..."
docker rm productapi-redis | Out-Null
Write-Host "Done."