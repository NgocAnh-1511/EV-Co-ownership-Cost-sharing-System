# Script khởi động Docker Compose
Write-Host "=== Khởi động EV Co-ownership System với Docker Compose ===" -ForegroundColor Cyan
Write-Host ""

# Kiểm tra Docker
Write-Host "Kiểm tra Docker..." -ForegroundColor Yellow
try {
    $dockerVersion = docker --version
    $composeVersion = docker-compose --version
    Write-Host "✓ Docker: $dockerVersion" -ForegroundColor Green
    Write-Host "✓ Docker Compose: $composeVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Docker chưa được cài đặt hoặc chưa chạy!" -ForegroundColor Red
    Write-Host "Vui lòng cài đặt Docker Desktop: https://www.docker.com/products/docker-desktop" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "Đang build và khởi động tất cả services..." -ForegroundColor Yellow
Write-Host "Quá trình này có thể mất 5-10 phút lần đầu tiên..." -ForegroundColor Yellow
Write-Host ""

# Build và khởi động
docker-compose up -d --build

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✓ Đã khởi động tất cả services!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Đang đợi services khởi động hoàn toàn (30 giây)..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
    
    Write-Host ""
    Write-Host "=== Trạng thái Services ===" -ForegroundColor Cyan
    docker-compose ps
    
    Write-Host ""
    Write-Host "=== URLs ===" -ForegroundColor Cyan
    Write-Host "UI Service:        http://localhost:8080" -ForegroundColor White
    Write-Host "API Gateway:       http://localhost:8084" -ForegroundColor White
    Write-Host "Gateway Health:    http://localhost:8084/actuator/health" -ForegroundColor White
    Write-Host ""
    
    Write-Host "=== Kiểm tra Health ===" -ForegroundColor Cyan
    Write-Host "Đang kiểm tra API Gateway..." -ForegroundColor Yellow
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8084/actuator/health" -UseBasicParsing -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            Write-Host "✓ API Gateway đang chạy" -ForegroundColor Green
        }
    } catch {
        Write-Host "⚠ API Gateway chưa sẵn sàng, vui lòng đợi thêm vài phút" -ForegroundColor Yellow
    }
    
    Write-Host ""
    Write-Host "=== Lệnh hữu ích ===" -ForegroundColor Cyan
    Write-Host "Xem logs:          docker-compose logs -f" -ForegroundColor White
    Write-Host "Xem trạng thái:    docker-compose ps" -ForegroundColor White
    Write-Host "Dừng services:     docker-compose stop" -ForegroundColor White
    Write-Host "Xóa tất cả:        docker-compose down -v" -ForegroundColor White
    Write-Host ""
    
} else {
    Write-Host ""
    Write-Host "✗ Có lỗi xảy ra khi khởi động!" -ForegroundColor Red
    Write-Host "Kiểm tra logs: docker-compose logs" -ForegroundColor Yellow
    exit 1
}

