# 🚀 Hướng dẫn Triển khai API Gateway

## 📋 Mục lục
1. [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
2. [Cài đặt Local](#cài-đặt-local)
3. [Triển khai với Docker](#triển-khai-với-docker)
4. [Kiểm tra hoạt động](#kiểm-tra-hoạt-động)
5. [Troubleshooting](#troubleshooting)

---

## 🔧 Yêu cầu hệ thống

- Java 21+
- Maven 3.6+
- Docker & Docker Compose (nếu dùng Docker)

---

## 💻 Cài đặt Local

### Bước 1: Build project

```bash
cd api-gateway
mvn clean install
```

### Bước 2: Chạy Gateway

```bash
mvn spring-boot:run
```

Hoặc chạy JAR file:
```bash
java -jar target/api-gateway-0.0.1-SNAPSHOT.jar
```

### Bước 3: Kiểm tra Gateway đang chạy

Mở browser hoặc dùng curl:
```bash
curl http://localhost:8084/actuator/health
```

Kết quả mong đợi:
```json
{
  "status": "UP"
}
```

### Bước 4: Đảm bảo các service khác đang chạy

Gateway cần các service sau đang chạy:
- ✅ Cost Payment Service (port 8081)
- ✅ Group Management Service (port 8082)
- ✅ User Account Service (port 8083)

---

## 🐳 Triển khai với Docker

### Bước 1: Build Docker image

```bash
cd api-gateway
docker build -t api-gateway:latest .
```

### Bước 2: Chạy với Docker Compose

```bash
# Từ thư mục gốc của project
docker-compose up api-gateway
```

Hoặc chạy tất cả services:
```bash
docker-compose up
```

### Bước 3: Kiểm tra trong Docker

```bash
# Xem logs
docker logs api-gateway

# Kiểm tra container đang chạy
docker ps | grep api-gateway
```

---

## ✅ Kiểm tra hoạt động

### Test 1: Health Check

```bash
curl http://localhost:8084/actuator/health
```

### Test 2: Xem danh sách routes

```bash
curl http://localhost:8084/actuator/gateway/routes
```

Kết quả sẽ hiển thị tất cả routes đã cấu hình.

### Test 3: Test routing đến Cost Service

**Điều kiện:** Cost Payment Service phải đang chạy trên port 8081

```bash
# Gọi qua Gateway
curl http://localhost:8084/api/costs

# Gateway sẽ tự động forward đến:
# http://cost-payment-service:8081/api/costs
# (hoặc http://localhost:8081/api/costs nếu chạy local)
```

### Test 4: Test routing đến Group Service

**Điều kiện:** Group Management Service phải đang chạy trên port 8082

```bash
# Gọi qua Gateway
curl http://localhost:8084/api/groups

# Gateway sẽ tự động forward đến:
# http://group-management-service:8082/api/groups
```

### Test 5: Test với UI Service

1. Đảm bảo UI Service đã cấu hình để dùng Gateway:
   ```properties
   # application.properties
   microservices.cost-payment.url=http://localhost:8084
   microservices.group-management.url=http://localhost:8084
   ```

2. Khởi động UI Service:
   ```bash
   cd ui-service
   mvn spring-boot:run
   ```

3. Mở browser: `http://localhost:8080/admin/costs`
4. Kiểm tra xem có lấy được dữ liệu không

---

## 🐛 Troubleshooting

### Vấn đề 1: Gateway không khởi động được

**Lỗi:**
```
Port 8084 already in use
```

**Giải pháp:**
```bash
# Tìm process đang dùng port 8084
# Windows:
netstat -ano | findstr :8084

# Linux/Mac:
lsof -i :8084

# Kill process hoặc đổi port trong application.yml
```

### Vấn đề 2: Gateway không route được

**Lỗi:**
```
503 Service Unavailable
```

**Nguyên nhân:**
- Service đích không đang chạy
- URI trong config sai
- Network không kết nối được (Docker)

**Giải pháp:**
1. Kiểm tra service đích có đang chạy không:
   ```bash
   curl http://localhost:8081/actuator/health  # Cost Service
   curl http://localhost:8082/actuator/health  # Group Service
   ```

2. Kiểm tra config trong `application.yml`:
   ```yaml
   uri: http://cost-payment-service:8081  # Đúng cho Docker
   # hoặc
   uri: http://localhost:8081  # Đúng cho local
   ```

3. Kiểm tra Docker network:
   ```bash
   docker network ls
   docker network inspect ev-network
   ```

### Vấn đề 3: 404 Not Found

**Nguyên nhân:**
- Path không match với predicates
- Service đích không có endpoint đó

**Giải pháp:**
1. Kiểm tra path có đúng format không:
   ```bash
   # Đúng:
   curl http://localhost:8084/api/costs
   
   # Sai:
   curl http://localhost:8084/costs  # Thiếu /api
   ```

2. Kiểm tra predicates trong config:
   ```yaml
   predicates:
     - Path=/api/costs/**  # Phải match với path trong request
   ```

### Vấn đề 4: CORS Error

**Lỗi:**
```
Access to XMLHttpRequest has been blocked by CORS policy
```

**Giải pháp:**
Gateway đã có CORS config, nhưng nếu vẫn lỗi, kiểm tra:
1. CORS config trong `application.yml`:
   ```yaml
   globalcors:
     cors-configurations:
       '[/**]':
         allowedOrigins: "*"  # Cho phép tất cả origins
   ```

2. Nếu cần giới hạn origins:
   ```yaml
   allowedOrigins:
     - "http://localhost:8080"
     - "http://localhost:3000"
   ```

### Vấn đề 5: Connection Refused trong Docker

**Lỗi:**
```
Connection refused: cost-payment-service:8081
```

**Nguyên nhân:**
- Service chưa sẵn sàng khi Gateway khởi động
- Service không trong cùng Docker network

**Giải pháp:**
1. Thêm `depends_on` trong docker-compose.yml:
   ```yaml
   api-gateway:
     depends_on:
       - cost-payment-service
       - group-management-service
   ```

2. Đảm bảo tất cả services trong cùng network:
   ```yaml
   networks:
     - ev-network
   ```

3. Kiểm tra service name đúng:
   ```yaml
   # Trong docker-compose.yml
   cost-payment-service:
     container_name: cost-payment-service  # Phải đúng
   
   # Trong application.yml của Gateway
   uri: http://cost-payment-service:8081  # Dùng container_name
   ```

---

## 📊 Monitoring

### Xem logs của Gateway

**Local:**
```bash
# Logs sẽ hiển thị trong console khi chạy mvn spring-boot:run
```

**Docker:**
```bash
docker logs -f api-gateway
```

### Xem metrics

```bash
# Health
curl http://localhost:8084/actuator/health

# Routes
curl http://localhost:8084/actuator/gateway/routes

# Route details
curl http://localhost:8084/actuator/gateway/routes/{routeId}
```

---

## 🔄 Cập nhật cấu hình

### Thêm route mới

1. Mở `application.yml`
2. Thêm route mới:
   ```yaml
   routes:
     - id: new-service
       uri: http://new-service:8089
       predicates:
         - Path=/api/new/**
   ```
3. Restart Gateway

### Thay đổi port

1. Sửa `server.port` trong `application.yml`
2. Cập nhật `docker-compose.yml` nếu cần
3. Restart Gateway

---

## 📝 Checklist triển khai

- [ ] Java 21+ đã cài đặt
- [ ] Maven đã cài đặt
- [ ] Build project thành công: `mvn clean install`
- [ ] Gateway khởi động được: `mvn spring-boot:run`
- [ ] Health check OK: `curl http://localhost:8084/actuator/health`
- [ ] Các service khác đang chạy (8081, 8082, 8083)
- [ ] Test routing thành công
- [ ] UI Service cấu hình đúng để dùng Gateway
- [ ] Test end-to-end từ browser

---

## 🎯 Next Steps

Sau khi Gateway hoạt động ổn định, có thể thêm:

1. **Authentication Filter**: Kiểm tra JWT token
2. **Rate Limiting**: Giới hạn số request
3. **Load Balancing**: Phân tải giữa nhiều instance
4. **Circuit Breaker**: Xử lý khi service down
5. **Request/Response Logging**: Log tất cả request

---

**Chúc bạn triển khai thành công! 🎉**

