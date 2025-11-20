# 🐳 Hướng dẫn Deploy dự án lên Docker Compose

## 📋 Tổng quan

Dự án này bao gồm:
- **8 Microservices** (Spring Boot)
- **8 MySQL Databases**
- **1 API Gateway** (Spring Cloud Gateway)
- **1 UI Service** (Spring Boot + Thymeleaf)

## 🏗️ Kiến trúc

```
┌─────────────┐
│ UI Service  │ (Port 8080)
│  (Thymeleaf)│
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ API Gateway │ (Port 8084)
└──────┬──────┘
       │
       ├──► Cost Payment Service (8081)
       ├──► Group Management Service (8082)
       ├──► User Account Service (8083)
       ├──► Vehicle Service (8085)
       ├──► Reservation Service (8086)
       ├──► Reservation Admin Service (8087)
       ├──► AI Service (8088)
       └──► Legal Contract Service (8089)
```

## 📦 Yêu cầu

- **Docker** >= 20.10
- **Docker Compose** >= 2.0
- **RAM**: Tối thiểu 8GB (khuyến nghị 16GB)
- **Disk**: Tối thiểu 10GB trống

## 🚀 Các bước triển khai

### Bước 1: Kiểm tra Docker

```powershell
# Kiểm tra Docker đã cài đặt
docker --version
docker-compose --version

# Kiểm tra Docker đang chạy
docker ps
```

### Bước 2: Chuẩn bị môi trường

```powershell
# Di chuyển đến thư mục dự án
cd D:\Merge\EV-Co-ownership-Cost-sharing-System

# Kiểm tra file docker-compose.yml tồn tại
Test-Path docker-compose.yml
```

### Bước 3: Build và khởi động tất cả services

```powershell
# Build và khởi động tất cả services
docker-compose up -d --build

# Hoặc build từng service (nếu cần)
docker-compose build
docker-compose up -d
```

### Bước 4: Kiểm tra trạng thái

```powershell
# Xem trạng thái tất cả containers
docker-compose ps

# Xem logs của tất cả services
docker-compose logs -f

# Xem logs của một service cụ thể
docker-compose logs -f api-gateway
docker-compose logs -f ui-service
```

### Bước 5: Kiểm tra health

```powershell
# Kiểm tra API Gateway
Invoke-WebRequest -Uri "http://localhost:8084/actuator/health" -UseBasicParsing

# Kiểm tra UI Service
Invoke-WebRequest -Uri "http://localhost:8080" -UseBasicParsing

# Kiểm tra các services
Invoke-WebRequest -Uri "http://localhost:8084/api/ai/health" -UseBasicParsing
Invoke-WebRequest -Uri "http://localhost:8084/api/admin/reservations" -UseBasicParsing
```

## 📝 Các lệnh Docker Compose thường dùng

### Khởi động và dừng

```powershell
# Khởi động tất cả services
docker-compose up -d

# Dừng tất cả services
docker-compose stop

# Dừng và xóa containers
docker-compose down

# Dừng, xóa containers và volumes (⚠️ XÓA DỮ LIỆU)
docker-compose down -v
```

### Rebuild

```powershell
# Rebuild một service cụ thể
docker-compose build api-gateway
docker-compose up -d api-gateway

# Rebuild tất cả services
docker-compose build --no-cache
docker-compose up -d
```

### Xem logs

```powershell
# Xem logs real-time
docker-compose logs -f

# Xem logs của một service
docker-compose logs -f ui-service

# Xem logs với số dòng giới hạn
docker-compose logs --tail=100 api-gateway
```

### Kiểm tra và debug

```powershell
# Vào trong container
docker-compose exec api-gateway sh
docker-compose exec ui-service sh

# Kiểm tra network
docker network inspect ev-co-ownership-cost-sharing-system_ev-network

# Kiểm tra volumes
docker volume ls
```

## 🔧 Cấu hình

### Ports

| Service | Port | URL |
|---------|------|-----|
| UI Service | 8080 | http://localhost:8080 |
| API Gateway | 8084 | http://localhost:8084 |
| Cost Payment | 8081 | http://localhost:8081 |
| Group Management | 8082 | http://localhost:8082 |
| User Account | 8083 | http://localhost:8083 |
| Vehicle Service | 8085 | http://localhost:8085 |
| Reservation Service | 8086 | http://localhost:8086 |
| Reservation Admin | 8087 | http://localhost:8087 |
| AI Service | 8088 | http://localhost:8088 |
| Legal Contract | 8089 | http://localhost:8089 |

### Databases

| Database | Port | Container Name |
|----------|------|----------------|
| Payment MySQL | 3306 | payment-mysql |
| Group MySQL | 3307 | group-mysql |
| User MySQL | 3308 | user-mysql |
| Vehicle MySQL | 3309 | vehicle-mysql |
| Reservation MySQL | 3310 | reservation-mysql |
| Reservation Admin MySQL | 3311 | reservation-admin-mysql |
| AI MySQL | 3312 | ai-mysql |
| Legal MySQL | 3313 | legal-mysql |

## 🐛 Xử lý sự cố

### Service không khởi động

```powershell
# Kiểm tra logs
docker-compose logs service-name

# Kiểm tra trạng thái
docker-compose ps

# Restart service
docker-compose restart service-name
```

### Database connection error

```powershell
# Kiểm tra database đã sẵn sàng
docker-compose exec payment-mysql mysqladmin ping -h localhost -u root -ppassword

# Kiểm tra database đã được tạo
docker-compose exec payment-mysql mysql -u root -ppassword -e "SHOW DATABASES;"
```

### Port đã được sử dụng

```powershell
# Kiểm tra port đang được sử dụng
netstat -ano | findstr :8080
netstat -ano | findstr :8084

# Dừng service đang sử dụng port hoặc thay đổi port trong docker-compose.yml
```

### Rebuild từ đầu

```powershell
# Dừng và xóa tất cả
docker-compose down -v

# Xóa images
docker-compose down --rmi all

# Build lại từ đầu
docker-compose build --no-cache
docker-compose up -d
```

## 📊 Monitoring

### Xem resource usage

```powershell
# Xem CPU và memory usage
docker stats

# Xem disk usage
docker system df
```

### Health checks

Tất cả services đều có health check endpoints:
- API Gateway: `http://localhost:8084/actuator/health`
- UI Service: `http://localhost:8080/actuator/health`
- Các services khác: `http://localhost:PORT/actuator/health`

## 🔐 Bảo mật

### Thay đổi mật khẩu database

1. Sửa file `docker-compose.yml`:
```yaml
environment:
  MYSQL_ROOT_PASSWORD: your-secure-password
```

2. Rebuild và restart:
```powershell
docker-compose down -v
docker-compose up -d --build
```

### Environment variables

Tất cả sensitive data nên được đặt trong `.env` file:

```env
MYSQL_ROOT_PASSWORD=your-secure-password
JWT_SECRET=your-jwt-secret
```

Sau đó sử dụng trong `docker-compose.yml`:
```yaml
environment:
  MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
```

## 📈 Scaling

### Scale một service

```powershell
# Scale UI Service lên 3 instances
docker-compose up -d --scale ui-service=3
```

**Lưu ý**: Cần cấu hình load balancer cho các instances.

## 🧹 Cleanup

### Xóa tất cả containers và volumes

```powershell
# Dừng và xóa containers, networks
docker-compose down

# Xóa cả volumes (⚠️ XÓA DỮ LIỆU)
docker-compose down -v

# Xóa images
docker-compose down --rmi all

# Xóa tất cả (containers, volumes, images)
docker-compose down -v --rmi all
```

### Xóa unused resources

```powershell
# Xóa unused containers, networks, images
docker system prune

# Xóa cả volumes
docker system prune -a --volumes
```

## ✅ Checklist sau khi deploy

- [ ] Tất cả containers đang chạy: `docker-compose ps`
- [ ] UI Service accessible: http://localhost:8080
- [ ] API Gateway health check: http://localhost:8084/actuator/health
- [ ] Tất cả databases đã được tạo
- [ ] Có thể đăng nhập vào hệ thống
- [ ] Các trang admin hoạt động bình thường
- [ ] API endpoints trả về dữ liệu đúng

## 📞 Hỗ trợ

Nếu gặp vấn đề:
1. Kiểm tra logs: `docker-compose logs -f`
2. Kiểm tra health checks
3. Kiểm tra network: `docker network inspect ev-network`
4. Kiểm tra database connections

## 🎯 Quick Start

```powershell
# 1. Build và khởi động
docker-compose up -d --build

# 2. Đợi 2-3 phút để tất cả services khởi động

# 3. Kiểm tra
docker-compose ps

# 4. Truy cập
# UI: http://localhost:8080
# API Gateway: http://localhost:8084
```

---

**Chúc bạn deploy thành công! 🚀**

