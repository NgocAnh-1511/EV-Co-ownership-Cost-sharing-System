# 🔧 Giải Quyết Vấn Đề Trùng Service ID - Thêm Registration ID

## 📋 Vấn Đề

Bảng `vehicleservice` sử dụng composite key `(service_id, vehicle_id)` làm primary key, điều này ngăn không cho đăng ký cùng một dịch vụ (service_id) cho cùng một xe (vehicle_id) nhiều lần. Khi đặt dịch vụ, nếu service_id và vehicle_id trùng với bản ghi đã có, sẽ bị lỗi duplicate key.

## ✅ Giải Pháp

Thay đổi cấu trúc bảng `vehicleservice` để:
- Thêm cột `registration_id` AUTO_INCREMENT làm primary key
- Bỏ composite key `(service_id, vehicle_id)` khỏi primary key
- Cho phép đăng ký cùng một dịch vụ (service_id) cho cùng một xe (vehicle_id) nhiều lần
- Chỉ kiểm tra duplicate ở application layer (không cho phép đăng ký nếu có dịch vụ đang chờ xử lý)

## 🚀 Các Bước Thực Hiện

### Bước 1: Chạy Script SQL

Chạy script SQL để thêm cột `registration_id` và thay đổi primary key:

```bash
mysql -u root -p vehicle_management < database/add_registration_id_primary_key.sql
```

Hoặc chạy trực tiếp trong MySQL Workbench:

```sql
SOURCE database/add_registration_id_primary_key.sql;
```

### Bước 2: Kiểm Tra Kết Quả

Sau khi chạy script, kiểm tra cấu trúc bảng:

```sql
-- Kiểm tra cấu trúc bảng
DESCRIBE vehicle_management.vehicleservice;

-- Kiểm tra primary key
SHOW INDEX FROM vehicle_management.vehicleservice WHERE Key_name = 'PRIMARY';

-- Kiểm tra dữ liệu
SELECT registration_id, service_id, vehicle_id, status, request_date 
FROM vehicle_management.vehicleservice 
ORDER BY registration_id DESC 
LIMIT 10;
```

### Bước 3: Khởi Động Lại Ứng Dụng

Sau khi chạy script SQL, khởi động lại ứng dụng:

```bash
# Dừng ứng dụng
# Khởi động lại VehicleServiceManagementService
cd VehicleServiceManagementService
mvn spring-boot:run
```

## 📝 Các Thay Đổi Đã Thực Hiện

### 1. Database Schema

**File**: `database/add_registration_id_primary_key.sql`

- Thêm cột `registration_id INT AUTO_INCREMENT PRIMARY KEY`
- Xóa composite key `(service_id, vehicle_id)` khỏi primary key
- Tạo index cho `(service_id, vehicle_id)` để tối ưu query
- Giữ nguyên dữ liệu hiện có

### 2. Model Changes

**File**: `VehicleServiceManagementService/src/main/java/com/example/VehicleServiceManagementService/model/Vehicleservice.java`

- Thay đổi từ `@EmbeddedId VehicleServiceId` sang `@Id Integer registrationId`
- Thêm `@GeneratedValue(strategy = GenerationType.IDENTITY)` cho `registrationId`
- Cập nhật `@JoinColumn` cho `service` và `vehicle` (bỏ `insertable = false, updatable = false`)
- Cập nhật helper methods `getServiceId()` và `getVehicleId()`
- Bỏ method `initializeId()`

### 3. Repository Changes

**File**: `VehicleServiceManagementService/src/main/java/com/example/VehicleServiceManagementService/repository/VehicleServiceRepository.java`

- Thay đổi từ `JpaRepository<Vehicleservice, VehicleServiceId>` sang `JpaRepository<Vehicleservice, Integer>`
- Thêm method `findByRegistrationId(Integer registrationId)`
- Thêm method `findByServiceIdAndVehicleId(String serviceId, String vehicleId)` - trả về List
- Thêm method `findLatestByServiceIdAndVehicleId(String serviceId, String vehicleId)` - trả về Optional (bản ghi mới nhất)
- Cập nhật `existsByService_ServiceIdAndVehicle_VehicleId()` thay vì `existsById_ServiceIdAndId_VehicleId()`
- Cập nhật `deleteByServiceIdAndVehicleId()` để xóa tất cả bản ghi
- Thêm method `deleteByRegistrationId(Integer registrationId)`
- Cập nhật native query để bao gồm `registration_id`

### 4. Service Changes

**File**: `VehicleServiceManagementService/src/main/java/com/example/VehicleServiceManagementService/service/VehicleServiceService.java`

- Đơn giản hóa `saveVehicleService()` - không cần kiểm tra composite key
- Bỏ logic xử lý composite key
- `registration_id` sẽ được tự động generate bởi database
- Cập nhật `createVehicleService()` - bỏ logic tạo composite key

### 5. Controller Changes

**File**: `VehicleServiceManagementService/src/main/java/com/example/VehicleServiceManagementService/controller/VehicleServiceAPI.java`

- Thêm endpoint `GET /api/vehicleservices/{registrationId}` - lấy theo registration_id
- Cập nhật endpoint `GET /api/vehicleservices/service/{serviceId}/vehicle/{vehicleId}` - lấy bản ghi mới nhất
- Cập nhật endpoint `PUT /api/vehicleservices/{registrationId}` - cập nhật theo registration_id
- Thêm endpoint `DELETE /api/vehicleservices/{registrationId}` - xóa theo registration_id
- Giữ endpoint `DELETE /api/vehicleservices/service/{serviceId}/vehicle/{vehicleId}` - xóa tất cả bản ghi
- Cập nhật `getAllVehicleServices()` - bao gồm `registration_id` trong response
- Cập nhật `getVehicleServicesByVehicleId()` - bao gồm `registration_id` trong response
- Cập nhật `convertToMap()` - bao gồm `registration_id` trong response
- Đơn giản hóa logic kiểm tra duplicate - chỉ kiểm tra dịch vụ đang chờ

### 6. Vehicle API Changes

**File**: `VehicleServiceManagementService/src/main/java/com/example/VehicleServiceManagementService/controller/VehicleAPI.java`

- Cập nhật `deleteVehicle()` - xóa vehicleservice bằng cách xóa theo vehicle_id (không cần tắt foreign key checks)

## 🎯 Kết Quả

Sau khi thực hiện các thay đổi:

1. ✅ Có thể đăng ký cùng một dịch vụ (service_id) cho cùng một xe (vehicle_id) nhiều lần
2. ✅ Mỗi đăng ký dịch vụ có một `registration_id` duy nhất (AUTO_INCREMENT)
3. ✅ Chỉ chặn duplicate nếu có dịch vụ đang chờ xử lý (pending/in_progress)
4. ✅ Cho phép đăng ký lại sau khi dịch vụ trước đó đã completed
5. ✅ Giữ nguyên dữ liệu hiện có (không mất dữ liệu)

## 🔍 Kiểm Tra

### Test 1: Đăng Ký Dịch Vụ Mới

```bash
curl -X POST http://localhost:8083/api/vehicleservices \
  -H "Content-Type: application/json" \
  -d '{
    "serviceId": "SRV001",
    "vehicleId": "VEH001",
    "serviceDescription": "Bảo dưỡng định kỳ",
    "status": "pending"
  }'
```

### Test 2: Đăng Ký Lại Dịch Vụ (Sau Khi Completed)

```bash
# Đăng ký lần 1
curl -X POST http://localhost:8083/api/vehicleservices \
  -H "Content-Type: application/json" \
  -d '{
    "serviceId": "SRV001",
    "vehicleId": "VEH001",
    "serviceDescription": "Bảo dưỡng lần 1",
    "status": "pending"
  }'

# Cập nhật status thành completed
curl -X PUT http://localhost:8083/api/vehicleservices/{registrationId} \
  -H "Content-Type: application/json" \
  -d '{
    "status": "completed"
  }'

# Đăng ký lần 2 (cho phép)
curl -X POST http://localhost:8083/api/vehicleservices \
  -H "Content-Type: application/json" \
  -d '{
    "serviceId": "SRV001",
    "vehicleId": "VEH001",
    "serviceDescription": "Bảo dưỡng lần 2",
    "status": "pending"
  }'
```

### Test 3: Kiểm Tra Duplicate (Đang Chờ)

```bash
# Đăng ký lần 1
curl -X POST http://localhost:8083/api/vehicleservices \
  -H "Content-Type: application/json" \
  -d '{
    "serviceId": "SRV001",
    "vehicleId": "VEH001",
    "status": "pending"
  }'

# Đăng ký lần 2 (sẽ bị chặn)
curl -X POST http://localhost:8083/api/vehicleservices \
  -H "Content-Type: application/json" \
  -d '{
    "serviceId": "SRV001",
    "vehicleId": "VEH001",
    "status": "pending"
  }'
# Response: 409 Conflict - "Dịch vụ này đã được đăng ký cho xe này và đang trong trạng thái chờ xử lý."
```

## 📊 Cấu Trúc Bảng Sau Khi Thay Đổi

```sql
CREATE TABLE vehicleservice (
    registration_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    service_id VARCHAR(20) NOT NULL,
    vehicle_id VARCHAR(20) NOT NULL,
    service_name VARCHAR(255),
    service_description TEXT,
    service_type VARCHAR(50),
    request_date TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    completion_date TIMESTAMP,
    INDEX idx_service_vehicle (service_id, vehicle_id),
    INDEX idx_vehicle_id (vehicle_id),
    INDEX idx_service_id (service_id),
    INDEX idx_status (status),
    FOREIGN KEY (service_id) REFERENCES service(service_id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id)
);
```

## ⚠️ Lưu Ý

1. **Backup Database**: Trước khi chạy script, nên backup database để đảm bảo an toàn
2. **Downtime**: Script có thể mất một chút thời gian nếu có nhiều dữ liệu
3. **Foreign Keys**: Đảm bảo foreign keys đã được cấu hình đúng
4. **Application Restart**: Cần khởi động lại ứng dụng sau khi chạy script
5. **UI Updates**: Có thể cần cập nhật UI để hiển thị `registration_id` nếu cần

## ✅ Checklist

- [x] Tạo script SQL để thêm `registration_id`
- [x] Cập nhật model `Vehicleservice`
- [x] Cập nhật `VehicleServiceRepository`
- [x] Cập nhật `VehicleServiceService`
- [x] Cập nhật `VehicleServiceAPI`
- [x] Cập nhật `VehicleAPI` (xóa vehicleservice)
- [x] Test đăng ký dịch vụ mới
- [x] Test đăng ký lại dịch vụ (sau khi completed)
- [x] Test kiểm tra duplicate (đang chờ)

## 📚 Tài Liệu Tham Khảo

- [MySQL AUTO_INCREMENT](https://dev.mysql.com/doc/refman/8.0/en/example-auto-increment.html)
- [Spring Data JPA - Primary Keys](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.entity-persistence.id-class)
- [Hibernate - Identity Generation](https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#identifiers-generators-identity)


