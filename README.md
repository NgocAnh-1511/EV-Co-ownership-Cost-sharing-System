# EV Co-ownership System

Hệ thống quản lý đồng sở hữu và chia sẻ chi phí xe điện.

## 🚀 Quick Start

### Yêu cầu
- Docker & Docker Compose
- Java 21
- Maven 3.6+

### Chạy hệ thống

```bash
# Kiểm tra dự án
chmod +x check-system.sh
./check-system.sh

# Chạy hệ thống
chmod +x run.sh
./run.sh

# Hoặc chạy thủ công
docker-compose up --build
```

## 🌐 Truy cập

- **Giao diện chính**: http://localhost:8080
- **Group Management API**: http://localhost:8082/api/groups
- **Cost Payment API**: http://localhost:8083/api/costs
- **Health Check**: http://localhost:8080/health

## 📋 Tính năng

- ✅ Quản lý nhóm đồng sở hữu
- ✅ Quản lý thành viên nhóm
- ✅ Hệ thống bỏ phiếu
- ✅ Quản lý chi phí
- ✅ Phân chia chi phí
- ✅ Quản lý thanh toán
- ✅ Giao diện web
- ✅ API RESTful

## 🗄️ Database

- **Group Management DB**: Port 3310
- **Cost Payment DB**: Port 3308
- **Sample data**: Có sẵn

## 🎯 Services

- **Group Management Service**: Port 8082
- **Cost Payment Service**: Port 8083
- **UI Service**: Port 8080

---

**🎉 Hệ thống đã sẵn sàng để sử dụng!**