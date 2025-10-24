# EV Co-ownership & Cost-sharing System

Hệ thống quản lý đồng sở hữu và chia sẻ chi phí xe điện được thiết kế theo kiến trúc microservice.

## Kiến trúc Hệ thống

### Microservices

1. **user-service** - Quản lý tài khoản, xác thực, tỷ lệ sở hữu
2. **reservation-service** - Quản lý đặt lịch, sử dụng xe, phân tích AI
3. **cost-payment-service** - Tính toán chi phí, thanh toán, quản lý quỹ chung
4. **financial-reporting-service** - Xuất báo cáo tài chính minh bạch
5. **group-management-service** - Quản lý nhóm đồng sở hữu, bỏ phiếu/quyết định chung
6. **vehicle-management-service** - Quản lý nhóm xe đồng sở hữu, dịch vụ xe
7. **dispute-management-service** - Theo dõi và giám sát tranh chấp
8. **legal-contract-service** - Quản lý hợp đồng pháp lý điện tử và check-in/check-out

## Services được triển khai trong dự án này

### 1. Group Management Service (Port: 8082)

**Chức năng chính:**
- Quản lý nhóm đồng sở hữu
- Quản lý thành viên nhóm và tỷ lệ sở hữu
- Hệ thống bỏ phiếu và quyết định chung
- Quản lý quỹ chung

### 2. Cost Payment Service (Port: 8083)

**Chức năng chính:**
- Quản lý chi phí và phân chia theo tỷ lệ sở hữu/sử dụng
- Xử lý thanh toán trực tuyến
- Báo cáo tài chính minh bạch
- Quản lý quỹ chung

### 3. UI Service (Port: 8080) - **GIAO DIỆN WEB**

**Chức năng chính:**
- Giao diện web để quản lý và test 2 microservices
- Dashboard tổng quan hệ thống
- Quản lý nhóm đồng sở hữu qua web interface
- Quản lý chi phí và thanh toán qua web interface
- Health check và monitoring
- Tích hợp với Group Management và Cost Payment services

## Database Schema

### Group Management Database (ev_coownership_group_db)

#### Tables:
- `co_ownership_groups` - Thông tin nhóm đồng sở hữu
- `group_members` - Thành viên nhóm và tỷ lệ sở hữu
- `group_votes` - Cuộc bỏ phiếu
- `vote_options` - Lựa chọn trong phiếu bầu
- `vote_responses` - Phản hồi bỏ phiếu
- `group_funds` - Quỹ chung
- `fund_transactions` - Giao dịch quỹ

### Cost Payment Database (ev_coownership_payment_db)

#### Tables:
- `cost_categories` - Danh mục chi phí
- `cost_items` - Chi phí cụ thể
- `cost_splits` - Phân chia chi phí
- `payments` - Thanh toán
- `financial_reports` - Báo cáo tài chính

## Cài đặt và Chạy

### Yêu cầu hệ thống
- Docker và Docker Compose
- Java 21
- Maven 3.6+

### Chạy với Docker Compose

```bash
# Clone repository
git clone <repository-url>
cd EV-Co-ownership-Cost-sharing-System

# Build và chạy tất cả services
docker-compose up --build

# Chạy ở background
docker-compose up -d --build
```

### Chạy từng service riêng lẻ

```bash
# Group Management Service
cd group-management-service
mvn clean package
java -jar target/group-management-service-0.0.1-SNAPSHOT.jar

# Cost Payment Service
cd cost-payment-service
mvn clean package
java -jar target/cost-payment-service-0.0.1-SNAPSHOT.jar
```

## Truy cập Services

- **Eureka Server**: http://localhost:8761
- **UI Service (Web Interface)**: http://localhost:8080 ⭐ **GIAO DIỆN CHÍNH**
- **Group Management Service**: http://localhost:8082
- **Cost Payment Service**: http://localhost:8083
- **Group Management API**: http://localhost:8082/api/groups
- **Cost Payment API**: http://localhost:8083/api/costs

## API Documentation

### Group Management Service

#### Tạo nhóm mới
```bash
curl -X POST http://localhost:8082/api/groups \
  -H "Content-Type: application/json" \
  -d '{
    "groupName": "EV Group 1",
    "description": "Nhóm đồng sở hữu xe điện Tesla Model 3",
    "vehicleId": "vehicle-001",
    "groupAdminId": "user-001",
    "totalOwnershipPercentage": 100.0
  }'
```

#### Thêm thành viên
```bash
curl -X POST http://localhost:8082/api/groups/1/members \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-002",
    "ownershipPercentage": 40.0,
    "role": "MEMBER"
  }'
```

### Cost Payment Service

#### Tạo chi phí mới
```bash
curl -X POST http://localhost:8083/api/costs \
  -H "Content-Type: application/json" \
  -d '{
    "groupId": "group-001",
    "vehicleId": "vehicle-001",
    "title": "Phí sạc điện tháng 1",
    "description": "Chi phí sạc điện tại trạm sạc công cộng",
    "totalAmount": 500000,
    "currency": "VND",
    "incurredDate": "2024-01-15T10:00:00"
  }'
```

#### Tạo phân chia chi phí
```bash
curl -X POST http://localhost:8083/api/costs/1/splits
```

## Monitoring và Health Checks

- **Actuator Health**: http://localhost:8082/actuator/health
- **Actuator Health**: http://localhost:8083/actuator/health
- **Metrics**: http://localhost:8082/actuator/metrics
- **Metrics**: http://localhost:8083/actuator/metrics

## Cấu hình

### Environment Variables

#### Group Management Service
- `SPRING_DATASOURCE_URL`: Database URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE`: Eureka server URL

#### Cost Payment Service
- `SPRING_DATASOURCE_URL`: Database URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE`: Eureka server URL

## Phát triển

### Thêm tính năng mới

1. Tạo entity mới trong package `entity`
2. Tạo repository trong package `repository`
3. Tạo service trong package `service`
4. Tạo controller trong package `controller`
5. Thêm tests trong package `test`

### Testing

```bash
# Chạy tests cho Group Management Service
cd group-management-service
mvn test

# Chạy tests cho Cost Payment Service
cd cost-payment-service
mvn test
```

## Troubleshooting

### Lỗi thường gặp

1. **Database connection failed**
   - Kiểm tra MySQL container đã chạy
   - Kiểm tra database credentials

2. **Service không register với Eureka**
   - Kiểm tra Eureka server đã chạy
   - Kiểm tra network configuration

3. **Port conflicts**
   - Kiểm tra port đã được sử dụng
   - Thay đổi port trong application.properties

## Đóng góp

1. Fork repository
2. Tạo feature branch
3. Commit changes
4. Push to branch
5. Tạo Pull Request

## License

MIT License
