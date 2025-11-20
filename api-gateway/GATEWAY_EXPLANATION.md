# 🌉 API Gateway & Spring Cloud Gateway - Giải thích chi tiết

## 📖 1. API Gateway là gì?

### Ví dụ thực tế: Tòa nhà văn phòng

Hãy tưởng tượng bạn có một tòa nhà văn phòng với nhiều phòng ban:
- **Phòng A**: Phòng Kế toán (Cost Payment Service - Port 8081)
- **Phòng B**: Phòng Nhân sự (Group Management Service - Port 8082)
- **Phòng C**: Phòng Bảo vệ (User Account Service - Port 8083)

**KHÔNG có API Gateway (Trước đây):**
```
Khách hàng → Phải biết chính xác phòng nào ở đâu
- Muốn thanh toán? → Phải đến trực tiếp Phòng A (8081)
- Muốn xem nhóm? → Phải đến trực tiếp Phòng B (8082)
- Muốn đăng nhập? → Phải đến trực tiếp Phòng C (8083)
```

**CÓ API Gateway (Bây giờ):**
```
Khách hàng → Chỉ cần đến LỄ TÂN (API Gateway - Port 8084)
- Muốn thanh toán? → Lễ tân tự động chuyển đến Phòng A
- Muốn xem nhóm? → Lễ tân tự động chuyển đến Phòng B
- Muốn đăng nhập? → Lễ tân tự động chuyển đến Phòng C
```

### Lợi ích của API Gateway:

1. **Một điểm vào duy nhất**: Khách hàng chỉ cần biết địa chỉ Gateway
2. **Bảo mật**: Gateway có thể kiểm tra quyền truy cập trước khi chuyển tiếp
3. **Quản lý tập trung**: Dễ dàng thêm logging, monitoring, rate limiting
4. **Ẩn chi tiết**: Khách hàng không cần biết service nào chạy ở port nào

---

## 🔧 2. Spring Cloud Gateway là gì?

**Spring Cloud Gateway** là một API Gateway được xây dựng trên Spring Framework, đặc biệt cho các ứng dụng microservices.

### Đặc điểm:
- ✅ Reactive (non-blocking) - Hiệu suất cao
- ✅ Dễ cấu hình qua YAML hoặc Java code
- ✅ Hỗ trợ nhiều tính năng: routing, filtering, load balancing
- ✅ Tích hợp tốt với Spring Boot

---

## 🎯 3. Cách hoạt động trong dự án của bạn

### Kiến trúc TRƯỚC (không có Gateway):

```
┌─────────────┐
│   Browser   │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────┐
│      UI Service (Port 8080)         │
│                                     │
│  ┌──────────────────────────────┐  │
│  │  CostPaymentClient            │  │
│  │  → http://localhost:8081     │  │
│  └──────────────────────────────┘  │
│                                     │
│  ┌──────────────────────────────┐  │
│  │  GroupManagementClient        │  │
│  │  → http://localhost:8082     │  │
│  └──────────────────────────────┘  │
│                                     │
│  ┌──────────────────────────────┐  │
│  │  UserAccountClient           │  │
│  │  → http://localhost:8083     │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
       │              │              │
       ▼              ▼              ▼
┌──────────┐  ┌──────────┐  ┌──────────┐
│ Cost     │  │ Group    │  │ User     │
│ Service  │  │ Service  │  │ Service  │
│ :8081    │  │ :8082    │  │ :8083    │
└──────────┘  └──────────┘  └──────────┘
```

**Vấn đề:**
- UI Service phải biết địa chỉ của TẤT CẢ các service
- Khó quản lý khi có nhiều service
- Khó thêm bảo mật, logging tập trung

### Kiến trúc SAU (có Gateway):

```
┌─────────────┐
│   Browser   │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────┐
│      UI Service (Port 8080)         │
│                                     │
│  TẤT CẢ request đều gửi đến:        │
│  → http://api-gateway:8084/api/... │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│   API Gateway (Port 8084)           │
│                                     │
│  ┌──────────────────────────────┐  │
│  │  Routing Rules:              │  │
│  │                              │  │
│  │  /api/costs/**    → 8081     │  │
│  │  /api/groups/**   → 8082     │  │
│  │  /api/auth/**     → 8083     │  │
│  └──────────────────────────────┘  │
└──────┬──────────────────────────────┘
       │
       ├──────────────┬──────────────┐
       ▼              ▼              ▼
┌──────────┐  ┌──────────┐  ┌──────────┐
│ Cost     │  │ Group    │  │ User     │
│ Service  │  │ Service  │  │ Service  │
│ :8081    │  │ :8082    │  │ :8083    │
└──────────┘  └──────────┘  └──────────┘
```

**Lợi ích:**
- UI Service chỉ cần biết địa chỉ Gateway
- Gateway tự động route đến service đúng
- Dễ quản lý, bảo mật, monitoring

---

## 📝 4. Ví dụ cụ thể trong dự án

### Ví dụ 1: Lấy danh sách chi phí

**TRƯỚC (không có Gateway):**

```java
// Trong CostPaymentClient.java
@Value("${microservices.cost-payment.url:http://localhost:8081}")
private String costPaymentUrl;

public List<CostDto> getAllCosts() {
    // Gọi TRỰC TIẾP đến Cost Service
    return restTemplate.exchange(
        costPaymentUrl + "/api/costs",  // http://localhost:8081/api/costs
        HttpMethod.GET,
        null,
        ...
    );
}
```

**SAU (có Gateway):**

```java
// Trong CostPaymentClient.java
@Value("${microservices.cost-payment.url:http://localhost:8084}")  // Gateway!
private String costPaymentUrl;

public List<CostDto> getAllCosts() {
    // Gọi đến Gateway, Gateway tự động chuyển đến Cost Service
    return restTemplate.exchange(
        costPaymentUrl + "/api/costs",  // http://localhost:8084/api/costs
        HttpMethod.GET,
        null,
        ...
    );
}
```

**Luồng hoạt động:**

```
1. UI Service gọi: GET http://localhost:8084/api/costs
                    │
                    ▼
2. API Gateway nhận request
                    │
                    ▼
3. Gateway kiểm tra routing rules:
   - Path = /api/costs/**
   - Match với rule: cost-payment-service
                    │
                    ▼
4. Gateway forward request đến:
   http://cost-payment-service:8081/api/costs
                    │
                    ▼
5. Cost Service xử lý và trả về response
                    │
                    ▼
6. Gateway nhận response và trả về cho UI Service
```

### Ví dụ 2: Tạo nhóm mới

**Request từ UI:**
```
POST http://localhost:8084/api/groups
Body: {
  "name": "Nhóm Xe Điện ABC",
  "description": "Nhóm đồng sở hữu xe điện"
}
```

**Luồng hoạt động:**

```
1. UI Service → API Gateway (8084)
   POST /api/groups
   
2. Gateway kiểm tra:
   - Path = /api/groups/**
   - Match với: group-management-service
   
3. Gateway forward đến:
   POST http://group-management-service:8082/api/groups
   
4. Group Service xử lý và trả về:
   {
     "groupId": 1,
     "name": "Nhóm Xe Điện ABC",
     ...
   }
   
5. Gateway trả về response cho UI Service
```

---

## ⚙️ 5. Cấu hình trong dự án

### File: `api-gateway/src/main/resources/application.yml`

```yaml
spring:
  cloud:
    gateway:
      routes:
        # Route 1: Cost Payment Service
        - id: cost-payment-service
          uri: http://cost-payment-service:8081
          predicates:
            - Path=/api/costs/**, /api/payments/**, ...
          # Khi có request đến /api/costs/**, 
          # Gateway sẽ forward đến cost-payment-service:8081
        
        # Route 2: Group Management Service
        - id: group-management-service
          uri: http://group-management-service:8082
          predicates:
            - Path=/api/groups/**, /api/votes/**
          # Khi có request đến /api/groups/**, 
          # Gateway sẽ forward đến group-management-service:8082
```

### Giải thích từng phần:

1. **`id`**: Tên định danh của route (tùy chọn, dùng để quản lý)
2. **`uri`**: Địa chỉ service đích mà Gateway sẽ forward request đến
3. **`predicates`**: Điều kiện để match route này
   - `Path=/api/costs/**`: Nếu path bắt đầu bằng `/api/costs/`, dùng route này
   - `**`: Match tất cả các path con

---

## 🔍 6. Cách kiểm tra Gateway hoạt động

### Bước 1: Khởi động Gateway

```bash
cd api-gateway
mvn spring-boot:run
```

Hoặc với Docker:
```bash
docker-compose up api-gateway
```

### Bước 2: Kiểm tra Gateway đang chạy

```bash
curl http://localhost:8084/actuator/health
```

Response:
```json
{
  "status": "UP"
}
```

### Bước 3: Xem danh sách routes

```bash
curl http://localhost:8084/actuator/gateway/routes
```

Response sẽ hiển thị tất cả các routes đã cấu hình.

### Bước 4: Test routing

**Test route đến Cost Service:**
```bash
# Gọi qua Gateway
curl http://localhost:8084/api/costs

# Gateway sẽ tự động forward đến:
# http://cost-payment-service:8081/api/costs
```

**Test route đến Group Service:**
```bash
# Gọi qua Gateway
curl http://localhost:8084/api/groups

# Gateway sẽ tự động forward đến:
# http://group-management-service:8082/api/groups
```

---

## 🎓 7. So sánh: Có và Không có Gateway

### Scenario: Lấy danh sách chi phí

**KHÔNG có Gateway:**

```
Browser
  │
  ▼
UI Service (8080)
  │
  ▼ (phải biết địa chỉ chính xác)
Cost Service (8081)
```

**CÓ Gateway:**

```
Browser
  │
  ▼
UI Service (8080)
  │
  ▼ (chỉ cần biết Gateway)
API Gateway (8084)
  │
  ▼ (Gateway tự động route)
Cost Service (8081)
```

### Lợi ích khi có Gateway:

1. **UI Service đơn giản hơn:**
   - Trước: Phải biết 5-10 địa chỉ service khác nhau
   - Sau: Chỉ cần biết 1 địa chỉ Gateway

2. **Dễ thay đổi:**
   - Trước: Muốn đổi port của Cost Service? Phải sửa UI Service
   - Sau: Chỉ cần sửa cấu hình Gateway

3. **Bảo mật tốt hơn:**
   - Trước: Mỗi service phải tự xử lý authentication
   - Sau: Gateway có thể xử lý authentication một lần cho tất cả

4. **Monitoring tập trung:**
   - Trước: Phải monitor từng service riêng
   - Sau: Monitor Gateway là đủ

---

## 🚀 8. Các tính năng nâng cao (có thể thêm sau)

### 1. Rate Limiting (Giới hạn số request)
```yaml
filters:
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenishRate: 10  # 10 requests/giây
      redis-rate-limiter.burstCapacity: 20
```

### 2. Authentication Filter
```yaml
filters:
  - name: AuthFilter
    args:
      # Kiểm tra JWT token trước khi forward
```

### 3. Load Balancing
```yaml
uri: lb://cost-payment-service  # lb = load balance
# Gateway tự động phân tải giữa nhiều instance
```

### 4. Circuit Breaker (Ngắt mạch khi service lỗi)
```yaml
filters:
  - name: CircuitBreaker
    args:
      name: costServiceCircuitBreaker
```

---

## 📚 9. Tóm tắt

### API Gateway là gì?
→ **Lễ tân** của hệ thống microservices, nhận tất cả request và tự động chuyển đến service đúng.

### Spring Cloud Gateway là gì?
→ Công cụ của Spring để xây dựng API Gateway, dễ cấu hình và hiệu suất cao.

### Trong dự án của bạn:
- **Port**: 8084
- **Chức năng**: Route request từ UI Service đến các microservice
- **Lợi ích**: Đơn giản hóa, bảo mật, dễ quản lý

### Cách sử dụng:
1. UI Service gọi: `http://api-gateway:8084/api/...`
2. Gateway tự động route đến service đúng
3. Service xử lý và trả về qua Gateway
4. Gateway trả về cho UI Service

---

## ❓ 10. Câu hỏi thường gặp

**Q: Gateway có làm chậm request không?**
A: Rất ít, Gateway được thiết kế reactive (non-blocking), độ trễ thường < 10ms.

**Q: Nếu Gateway down thì sao?**
A: Cần có backup Gateway hoặc fallback mechanism. Có thể dùng load balancer phía trước.

**Q: Gateway có thể cache response không?**
A: Có, có thể thêm cache filter để cache response.

**Q: Gateway có thể xử lý authentication không?**
A: Có, có thể thêm authentication filter để kiểm tra JWT token trước khi forward.

---

**Chúc bạn hiểu rõ về API Gateway! 🎉**

