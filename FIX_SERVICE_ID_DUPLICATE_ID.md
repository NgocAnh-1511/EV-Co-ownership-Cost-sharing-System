# 🔧 Giải Quyết Vấn Đề Trùng Service ID - Thêm ID Auto-Increment

## 📋 Vấn Đề

Bảng `vehicleservice` sử dụng composite key `(service_id, vehicle_id)` làm primary key, điều này ngăn không cho đăng ký cùng một dịch vụ (service_id) cho cùng một xe (vehicle_id) nhiều lần. Khi đặt dịch vụ, nếu service_id và vehicle_id trùng với bản ghi đã có, sẽ bị lỗi duplicate key.

## ✅ Giải Pháp

Thay đổi cấu trúc bảng `vehicleservice` để:
- Thêm cột `id` INT AUTO_INCREMENT làm primary key
- Bỏ composite key `(service_id, vehicle_id)` khỏi primary key
- Cho phép đăng ký cùng một dịch vụ (service_id) cho cùng một xe (vehicle_id) nhiều lần
- Chỉ kiểm tra duplicate ở application layer (không cho phép đăng ký nếu có dịch vụ đang chờ xử lý)

## 🚀 Các Bước Thực Hiện

### Bước 1: Chạy Script SQL

Chạy script SQL để thêm cột `id` và thay đổi primary key:

```bash
mysql -u root -p vehicle_management < database/add_id_primary_key.sql
```

Hoặc chạy trực tiếp trong MySQL Workbench:

```sql
SOURCE database/add_id_primary_key.sql;
```

### Bước 2: Kiểm Tra Kết Quả

Sau khi chạy script, kiểm tra cấu trúc bảng:

```sql
-- Kiểm tra cấu trúc bảng
DESCRIBE vehicle_management.vehicleservice;

-- Kiểm tra primary key
SHOW INDEX FROM vehicle_management.vehicleservice WHERE Key_name = 'PRIMARY';

-- Kiểm tra dữ liệu
SELECT id, service_id, vehicle_id, status, request_date 
FROM vehicle_management.vehicleservice 
ORDER BY id DESC 
LIMIT 10;
```

### Bước 3: Khởi Động Lại Ứng Dụng

Sau khi chạy script SQL, khởi động lại ứng dụng:

```bash
# Dừng ứng dụng
# Khởi động lại VehicleServiceManagementService
cd VehicleServiceManagementService
mvn spring-boot:run

# Khởi động lại UI Service
cd ui-service
mvn spring-boot:run
```

## 📝 Các Thay Đổi Đã Thực Hiện

### 1. Database Schema

**File**: `database/add_id_primary_key.sql`

- Thêm cột `id INT AUTO_INCREMENT PRIMARY KEY`
- Xóa composite key `(service_id, vehicle_id)` khỏi primary key
- Xóa cột `registration_id` nếu tồn tại (để tránh nhầm lẫn)
- Tạo index cho `(service_id, vehicle_id)` để tối ưu query
- Giữ nguyên dữ liệu hiện có

### 2. Model Changes

**File**: `VehicleServiceManagementService/src/main/java/com/example/VehicleServiceManagementService/model/Vehicleservice.java`

- Thay đổi từ `@EmbeddedId VehicleServiceId` sang `@Id Integer id`
- Thêm `@GeneratedValue(strategy = GenerationType.IDENTITY)` cho `id`
- Cập nhật `@JoinColumn` cho `service` và `vehicle` (bỏ `insertable = false, updatable = false`)
- Cập nhật helper methods `getServiceId()` và `getVehicleId()`
- Bỏ method `initializeId()`

### 3. Repository Changes

**File**: `VehicleServiceManagementService/src/main/java/com/example/VehicleServiceManagementService/repository/VehicleServiceRepository.java`

- Thay đổi từ `JpaRepository<Vehicleservice, VehicleServiceId>` sang `JpaRepository<Vehicleservice, Integer>`
- Thêm method `findByServiceIdAndVehicleId(String serviceId, String vehicleId)` - trả về List
- Thêm method `findLatestByServiceIdAndVehicleId(String serviceId, String vehicleId)` - trả về Optional (bản ghi mới nhất)
- Cập nhật `existsByService_ServiceIdAndVehicle_VehicleId()` thay vì `existsById_ServiceIdAndId_VehicleId()`
- Cập nhật `deleteByServiceIdAndVehicleId()` để xóa tất cả bản ghi
- Cập nhật native query để bao gồm `id`

### 4. Service Changes

**File**: `VehicleServiceManagementService/src/main/java/com/example/VehicleServiceManagementService/service/VehicleServiceService.java`

- Đơn giản hóa `saveVehicleService()` - không cần kiểm tra composite key
- Bỏ logic xử lý composite key
- `id` sẽ được tự động generate bởi database
- Cập nhật `createVehicleService()` - bỏ logic tạo composite key

### 5. Controller Changes (Backend)

**File**: `VehicleServiceManagementService/src/main/java/com/example/VehicleServiceManagementService/controller/VehicleServiceAPI.java`

- Thêm endpoint `GET /api/vehicleservices/{id}` - lấy theo id
- Cập nhật endpoint `GET /api/vehicleservices/service/{serviceId}/vehicle/{vehicleId}` - lấy bản ghi mới nhất
- Cập nhật endpoint `PUT /api/vehicleservices/{id}` - cập nhật theo id
- Thêm endpoint `DELETE /api/vehicleservices/{id}` - xóa theo id
- Giữ endpoint `DELETE /api/vehicleservices/service/{serviceId}/vehicle/{vehicleId}` - xóa tất cả bản ghi
- Cập nhật `getAllVehicleServices()` - bao gồm `id` trong response
- Cập nhật `getVehicleServicesByVehicleId()` - bao gồm `id` trong response
- Cập nhật `convertToMap()` - bao gồm `id` trong response
- Đơn giản hóa logic kiểm tra duplicate - chỉ kiểm tra dịch vụ đang chờ

### 6. UI Service Changes

**File**: `ui-service/src/main/java/com/example/ui_service/service/VehicleServiceRestClient.java`

- Thêm method `updateServiceStatusById(Integer id, String status)` - cập nhật theo id
- Giữ method `updateServiceStatus(String serviceId, String vehicleId, String status)` - deprecated nhưng vẫn hoạt động

**File**: `ui-service/src/main/java/com/example/ui_service/controller/VehicleServiceController.java`

- Thêm endpoint `PUT /admin/vehicle-manager/api/service/{id}/status` - cập nhật theo id
- Giữ endpoint `PUT /admin/vehicle-manager/api/service/{serviceId}/vehicle/{vehicleId}/status` - deprecated
- Cập nhật `getVehicleId()` helper - ưu tiên lấy từ root, fallback về nested object

**File**: `ui-service/src/main/resources/templates/admin/vehicle-manager.html`

- Cập nhật `buildServiceItem()` - lấy `id` từ service (Integer)
- Cập nhật `trackStatusChange()` - sử dụng `id` làm key
- Cập nhật `saveChangesAndClose()` - sử dụng endpoint mới với `id` nếu có
- Tương thích ngược: vẫn hỗ trợ serviceId/vehicleId nếu không có id

## 🎯 Kết Quả

Sau khi thực hiện các thay đổi:

1. ✅ Có thể đăng ký cùng một dịch vụ (service_id) cho cùng một xe (vehicle_id) nhiều lần
2. ✅ Mỗi đăng ký dịch vụ có một `id` duy nhất (AUTO_INCREMENT)
3. ✅ Chỉ chặn duplicate nếu có dịch vụ đang chờ xử lý (pending/in_progress)
4. ✅ Cho phép đăng ký lại sau khi dịch vụ trước đó đã completed
5. ✅ Giữ nguyên dữ liệu hiện có (không mất dữ liệu)
6. ✅ Tương thích ngược với code cũ (hỗ trợ cả id và serviceId/vehicleId)

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

Response sẽ bao gồm `id`:

```json
{
  "id": 1,
  "serviceId": "SRV001",
  "vehicleId": "VEH001",
  "serviceName": "Bảo dưỡng định kỳ",
  "status": "pending",
  ...
}
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

# Cập nhật status thành completed (sử dụng id từ response)
curl -X PUT http://localhost:8083/api/vehicleservices/1 \
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

### Test 4: Cập Nhật Status Theo ID (UI)

```javascript
// Sử dụng endpoint mới với id
fetch('/admin/vehicle-manager/api/service/1/status', {
    method: 'PUT',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({ status: 'completed' })
})
```

## 📊 Cấu Trúc Bảng Sau Khi Thay Đổi

```sql
CREATE TABLE vehicleservice (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
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

## 🔄 API Endpoints

### Backend API (VehicleServiceManagementService)

1. **GET /api/vehicleservices** - Lấy tất cả dịch vụ
2. **GET /api/vehicleservices/{id}** - Lấy dịch vụ theo id ⭐ MỚI
3. **GET /api/vehicleservices/service/{serviceId}/vehicle/{vehicleId}** - Lấy bản ghi mới nhất
4. **GET /api/vehicleservices/vehicle/{vehicleId}** - Lấy tất cả dịch vụ của một xe
5. **POST /api/vehicleservices** - Đăng ký dịch vụ mới
6. **PUT /api/vehicleservices/{id}** - Cập nhật dịch vụ theo id ⭐ MỚI
7. **DELETE /api/vehicleservices/{id}** - Xóa dịch vụ theo id ⭐ MỚI
8. **DELETE /api/vehicleservices/service/{serviceId}/vehicle/{vehicleId}** - Xóa tất cả bản ghi

### UI API (ui-service)

1. **PUT /admin/vehicle-manager/api/service/{id}/status** - Cập nhật status theo id ⭐ MỚI
2. **PUT /admin/vehicle-manager/api/service/{serviceId}/vehicle/{vehicleId}/status** - Cập nhật status (deprecated)

## ⚠️ Lưu Ý

1. **Backup Database**: Trước khi chạy script, nên backup database để đảm bảo an toàn
2. **Downtime**: Script có thể mất một chút thời gian nếu có nhiều dữ liệu
3. **Foreign Keys**: Đảm bảo foreign keys đã được cấu hình đúng
4. **Application Restart**: Cần khởi động lại ứng dụng sau khi chạy script
5. **Tương Thích Ngược**: Code vẫn hỗ trợ endpoint cũ (serviceId/vehicleId) để tương thích ngược
6. **JavaScript**: Frontend đã được cập nhật để sử dụng `id` khi có, fallback về serviceId/vehicleId nếu không có

## ✅ Checklist

- [x] Tạo script SQL để thêm `id`
- [x] Cập nhật model `Vehicleservice`
- [x] Cập nhật `VehicleServiceRepository`
- [x] Cập nhật `VehicleServiceService`
- [x] Cập nhật `VehicleServiceAPI` (Backend)
- [x] Cập nhật `VehicleServiceRestClient` (UI)
- [x] Cập nhật `VehicleServiceController` (UI)
- [x] Cập nhật template `vehicle-manager.html` (JavaScript)
- [x] Test đăng ký dịch vụ mới
- [x] Test đăng ký lại dịch vụ (sau khi completed)
- [x] Test kiểm tra duplicate (đang chờ)
- [x] Test cập nhật status theo id

## 📚 Tài Liệu Tham Khảo

- [MySQL AUTO_INCREMENT](https://dev.mysql.com/doc/refman/8.0/en/example-auto-increment.html)
- [Spring Data JPA - Primary Keys](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.entity-persistence.id-class)
- [Hibernate - Identity Generation](https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#identifiers-generators-identity)

