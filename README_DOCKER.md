# 🐳 Docker Deployment - EV Co-ownership Cost-sharing System

## 📦 Tổng quan

Dự án được containerized với Docker Compose, bao gồm:

- **10 Services**: 8 Microservices + 1 API Gateway + 1 UI Service
- **8 MySQL Databases**: Mỗi service có database riêng
- **1 Network**: Tất cả services giao tiếp qua Docker network

## 🏗️ Kiến trúc

```
                    ┌──────────────┐
                    │  UI Service  │
                    │   (8080)     │
                    └──────┬───────┘
                           │
                    ┌──────▼───────┐
                    │ API Gateway  │
                    │   (8084)     │
                    └──────┬───────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   ┌────▼────┐      ┌──────▼──────┐    ┌─────▼─────┐
   │ Cost    │      │ Group       │    │ User      │
   │ Payment │      │ Management  │    │ Account   │
   │ (8081)  │      │ (8082)      │    │ (8083)    │
   └─────────┘      └─────────────┘    └───────────┘
   
   ┌─────────┐      ┌─────────────┐    ┌───────────┐
   │ Vehicle │      │ Reservation │    │ Reservation│
   │ (8085)  │      │ (8086)      │    │ Admin     │
   └─────────┘      └─────────────┘    │ (8087)    │
                                       └───────────┘
   
   ┌─────────┐      ┌─────────────┐
   │ AI      │      │ Legal       │
   │ (8088)  │      │ Contract    │
   └─────────┘      │ (8089)      │
                    └─────────────┘
```

## 🚀 Quick Start

### Bước 1: Khởi động

```powershell
.\docker-start.ps1
```

Hoặc:

```powershell
docker-compose up -d --build
```

### Bước 2: Kiểm tra

```powershell
.\docker-check.ps1
```

### Bước 3: Truy cập

- **UI**: http://localhost:8080
- **API Gateway**: http://localhost:8084

## 📋 Services và Ports

| Service | Port | Database | Database Port |
|---------|------|----------|---------------|
| UI Service | 8080 | - | - |
| API Gateway | 8084 | - | - |
| Cost Payment | 8081 | Cost_Payment_DB | 3306 |
| Group Management | 8082 | Group_Management_DB | 3307 |
| User Account | 8083 | CoOwnershipDB | 3308 |
| Vehicle Service | 8085 | vehicle_management | 3309 |
| Reservation Service | 8086 | co_ownership_booking | 3310 |
| Reservation Admin | 8087 | co_ownership_booking_admin | 3311 |
| AI Service | 8088 | ai_ev | 3312 |
| Legal Contract | 8089 | legal_contract | 3313 |

## 🔧 Cấu hình

### Environment Variables

Tất cả cấu hình được đặt trong `docker-compose.yml`:

- **Database URLs**: Tự động sử dụng service names (ví dụ: `payment-mysql:3306`)
- **Service URLs**: Tự động sử dụng service names (ví dụ: `http://api-gateway:8084`)

### Thay đổi mật khẩu

Sửa trong `docker-compose.yml`:

```yaml
environment:
  MYSQL_ROOT_PASSWORD: your-secure-password
```

## 📝 Lệnh thường dùng

```powershell
# Khởi động
docker-compose up -d

# Dừng
docker-compose stop

# Xem logs
docker-compose logs -f

# Rebuild
docker-compose build --no-cache
docker-compose up -d

# Xóa tất cả
docker-compose down -v
```

## 🐛 Troubleshooting

Xem file `DOCKER_DEPLOYMENT_GUIDE.md` để biết chi tiết về troubleshooting.

## 📚 Tài liệu

- `DOCKER_DEPLOYMENT_GUIDE.md` - Hướng dẫn chi tiết
- `QUICK_START_DOCKER.md` - Quick start guide

