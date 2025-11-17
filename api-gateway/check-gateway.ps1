# Script kiểm tra API Gateway
Write-Host "Checking API Gateway status..." -ForegroundColor Cyan

$maxAttempts = 12
$attempt = 0
$success = $false

while ($attempt -lt $maxAttempts -and -not $success) {
    $attempt++
    Write-Host "Attempt $attempt/$maxAttempts..." -ForegroundColor Yellow
    
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8084/actuator/health" -UseBasicParsing -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            Write-Host ""
            Write-Host "API Gateway is RUNNING!" -ForegroundColor Green
            Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor Green
            Write-Host "Response: $($response.Content)" -ForegroundColor Green
            Write-Host ""
            Write-Host "Gateway URL: http://localhost:8084" -ForegroundColor Cyan
            Write-Host "Health Check: http://localhost:8084/actuator/health" -ForegroundColor Cyan
            Write-Host "Routes Info: http://localhost:8084/actuator/gateway/routes" -ForegroundColor Cyan
            $success = $true
            break
        }
    }
    catch {
        if ($attempt -lt $maxAttempts) {
            Write-Host "Still starting... waiting 10 seconds" -ForegroundColor Yellow
            Start-Sleep -Seconds 10
        }
        else {
            Write-Host ""
            Write-Host "API Gateway is not responding after $maxAttempts attempts" -ForegroundColor Red
            Write-Host ""
            Write-Host "Possible issues:" -ForegroundColor Yellow
            Write-Host "1. Gateway is still building (first time can take 2-3 minutes)" -ForegroundColor Yellow
            Write-Host "2. Port 8084 is already in use" -ForegroundColor Yellow
            Write-Host "3. There was an error during startup" -ForegroundColor Yellow
            Write-Host ""
            Write-Host "To check manually:" -ForegroundColor Cyan
            Write-Host "  curl http://localhost:8084/actuator/health" -ForegroundColor White
        }
    }
}

if (-not $success) {
    Write-Host ""
    Write-Host "Tips:" -ForegroundColor Cyan
    Write-Host "- Make sure port 8084 is not in use" -ForegroundColor White
    Write-Host "- Check if Maven build completed successfully" -ForegroundColor White
    Write-Host "- Try running: netstat -ano | findstr :8084" -ForegroundColor White
}
