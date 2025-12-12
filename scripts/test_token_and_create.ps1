Start-Sleep -Seconds 3
$body = @{username='testuser'; password='testpass'; ttlSeconds=600} | ConvertTo-Json
$tokenResp = Invoke-RestMethod -Uri 'http://localhost:8081/api/auth/token' -Method Post -Body $body -ContentType 'application/json' -ErrorAction Stop
$token = $tokenResp.data.token
Write-Host "TOKEN: $token"
$product = @{name='AuthTest'; price=12.34; imageUrl='http://img'; description='desc'; rating=4.2} | ConvertTo-Json
$prodResp = Invoke-RestMethod -Uri 'http://localhost:8081/api/products' -Method Post -Headers @{Authorization="Bearer $token"; 'Content-Type'='application/json'} -Body $product -ErrorAction Stop
$prodResp | ConvertTo-Json -Depth 5 | Write-Host
