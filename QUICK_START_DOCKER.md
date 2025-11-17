# 🚀 Quick Start - Docker Compose

## ⚡ Khởi động nhanh

### 1. Khởi động tất cả services

```powershell
# Cách 1: Sử dụng script (khuyến nghị)
.\docker-start.ps1

# Cách 2: Sử dụng Docker Compose trực tiếp
docker-compose up -d --build
```

### 2. Đợi services khởi động

Đợi **2-3 phút** để tất cả services khởi động hoàn toàn.

### 3. Kiểm tra trạng thái

```powershell
# Kiểm tra containers
docker-compose ps

# Hoặc sử dụng script
.\docker-check.ps1
```

### 4. Truy cập ứng dụng

- **UI Service**: http://localhost:8080
- **API Gateway**: http://localhost:8084
- **API Gateway Health**: http://localhost:8084/actuator/health

## 📋 Các lệnh cơ bản

```powershell
# Khởi động
docker-compose up -d

# Dừng
docker-compose stop
# hoặc
.\docker-stop.ps1

# Xem logs
docker-compose logs -f

# Xem logs của một service
docker-compose logs -f ui-service

# Rebuild một service
docker-compose build ui-service
docker-compose up -d ui-service

# Xóa tất cả (⚠️ XÓA DỮ LIỆU)
docker-compose down -v
```

## 🔍 Troubleshooting

### Service không khởi động

```powershell
# Xem logs
docker-compose logs service-name

# Restart service
docker-compose restart service-name
```

### Port đã được sử dụng

Kiểm tra và dừng service đang sử dụng port, hoặc thay đổi port trong `docker-compose.yml`.

### Database connection error

Đợi thêm vài phút để databases khởi động hoàn toàn.

## 📚 Tài liệu chi tiết

Xem file `DOCKER_DEPLOYMENT_GUIDE.md` để biết thêm chi tiết.

