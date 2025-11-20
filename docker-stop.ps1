# Script dừng Docker Compose
Write-Host "=== Dừng EV Co-ownership System ===" -ForegroundColor Cyan
Write-Host ""

$confirm = Read-Host "Bạn có chắc muốn dừng tất cả services? (y/n)"
if ($confirm -eq "y" -or $confirm -eq "Y") {
    Write-Host "Đang dừng services..." -ForegroundColor Yellow
    docker-compose stop
    
    Write-Host ""
    Write-Host "✓ Đã dừng tất cả services" -ForegroundColor Green
    Write-Host ""
    Write-Host "Để xóa containers: docker-compose down" -ForegroundColor Yellow
    Write-Host "Để xóa cả volumes: docker-compose down -v" -ForegroundColor Yellow
} else {
    Write-Host "Đã hủy" -ForegroundColor Yellow
}

