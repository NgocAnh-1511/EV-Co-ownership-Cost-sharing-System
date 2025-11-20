# Script kiểm tra trạng thái Docker Compose
Write-Host "=== Kiểm tra trạng thái Docker Compose ===" -ForegroundColor Cyan
Write-Host ""

# Kiểm tra containers
Write-Host "=== Containers ===" -ForegroundColor Yellow
docker-compose ps

Write-Host ""
Write-Host "=== Health Checks ===" -ForegroundColor Yellow

$services = @(
    @{Name="UI Service"; Port=8080; Url="http://localhost:8080"},
    @{Name="API Gateway"; Port=8084; Url="http://localhost:8084/actuator/health"},
    @{Name="Cost Payment Service"; Port=8081; Url="http://localhost:8081/actuator/health"},
    @{Name="Group Management Service"; Port=8082; Url="http://localhost:8082/actuator/health"},
    @{Name="User Account Service"; Port=8083; Url="http://localhost:8083/actuator/health"},
    @{Name="Vehicle Service"; Port=8085; Url="http://localhost:8085/actuator/health"},
    @{Name="Reservation Service"; Port=8086; Url="http://localhost:8086/actuator/health"},
    @{Name="Reservation Admin Service"; Port=8087; Url="http://localhost:8087/actuator/health"},
    @{Name="AI Service"; Port=8088; Url="http://localhost:8088/api/ai/health"},
    @{Name="Legal Contract Service"; Port=8089; Url="http://localhost:8089/actuator/health"}
)

foreach ($service in $services) {
    Write-Host "Kiểm tra $($service.Name)..." -NoNewline
    try {
        $response = Invoke-WebRequest -Uri $service.Url -Method GET -TimeoutSec 2 -UseBasicParsing -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Host " ✓" -ForegroundColor Green
        } else {
            Write-Host " ⚠ HTTP $($response.StatusCode)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host " ✗" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=== Resource Usage ===" -ForegroundColor Yellow
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}"

Write-Host ""
Write-Host "=== Network ===" -ForegroundColor Yellow
docker network inspect ev-co-ownership-cost-sharing-system_ev-network --format '{{range .Containers}}{{.Name}} {{end}}' 2>$null

