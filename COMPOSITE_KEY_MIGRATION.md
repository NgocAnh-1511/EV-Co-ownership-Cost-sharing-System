# 🔄 Migration: Xóa registration_id, Dùng Composite Key (service_id, vehicle_id)

## ✅ Đã Thực Hiện

### 1. Model Changes

#### Tạo Composite Key Class
- **File**: `VehicleServiceManagementService/src/main/java/com/example/VehicleServiceManagementService/model/VehicleServiceId.java`
- **Nội dung**: Class `VehicleServiceId` với `serviceId` và `vehicleId`

#### Sửa Vehicleservice Entity
- **File**: `VehicleServiceManagementService/src/main/java/com/example/VehicleServiceManagementService/model/Vehicleservice.java`
- **Thay đổi**:
  - Xóa `@Id` và `registrationId`
  - Thêm `@EmbeddedId private VehicleServiceId id`
  - Thêm helper methods: `getServiceId()`, `getVehicleId()`, `initializeId()`

### 2. Repository Changes

#### Sửa VehicleServiceRepository
- **File**: `VehicleServiceManagementService/src/main/java/com/example/VehicleServiceManagementService/repository/VehicleServiceRepository.java`
- **Thay đổi**:
  - `JpaRepository<Vehicleservice, Integer>` → `JpaRepository<Vehicleservice, VehicleServiceId>`
  - Thêm methods:
    - `findById_ServiceIdAndId_VehicleId()`
    - `existsById_ServiceIdAndId_VehicleId()`
    - `deleteById_ServiceIdAndId_VehicleId()`

### 3. Service Changes

#### Sửa VehicleServiceService
- **File**: `VehicleServiceManagementService/src/main/java/com/example/VehicleServiceManagementService/service/VehicleServiceService.java`
- **Thay đổi**:
  - `createVehicleService()`: Tạo `VehicleServiceId` từ `service` và `vehicle`
  - `saveVehicleService()`: Đơn giản hóa, không cần lấy generated ID nữa

### 4. Controller Changes

#### Sửa VehicleServiceAPI
- **File**: `VehicleServiceManagementService/src/main/java/com/example/VehicleServiceManagementService/controller/VehicleServiceAPI.java`
- **Thay đổi**:
  - `GET /{registrationId}` → `GET /service/{serviceId}/vehicle/{vehicleId}`
  - `PUT /{registrationId}` → `PUT /service/{serviceId}/vehicle/{vehicleId}`
  - `DELETE /{registrationId}` → `DELETE /service/{serviceId}/vehicle/{vehicleId}`
  - Xóa các tham chiếu đến `registrationId`

## 📝 Database Migration

### Script SQL

**File**: `database/remove_registration_id_add_composite_key.sql`

Script này sẽ:
1. Xóa PRIMARY KEY cũ (nếu có)
2. Xóa cột `registration_id` (nếu có)
3. Đảm bảo `service_id` và `vehicle_id` là NOT NULL
4. Tạo composite PRIMARY KEY `(service_id, vehicle_id)`

### Cách Chạy

```bash
mysql -u root -p < database/remove_registration_id_add_composite_key.sql
```

Hoặc trong MySQL:

```sql
USE vehicle_management;
SOURCE database/remove_registration_id_add_composite_key.sql;
```

## 🔄 API Endpoints Mới

### 1. GET - Lấy đăng ký dịch vụ
```
GET /api/vehicleservices/service/{serviceId}/vehicle/{vehicleId}
```

### 2. PUT - Cập nhật đăng ký dịch vụ
```
PUT /api/vehicleservices/service/{serviceId}/vehicle/{vehicleId}
Body: {
  "serviceDescription": "...",
  "status": "pending|completed|...",
  "completionDate": "..."
}
```

**Lưu ý**: Không thể thay đổi `serviceId` và `vehicleId` vì chúng là primary key.

### 3. DELETE - Xóa đăng ký dịch vụ
```
DELETE /api/vehicleservices/service/{serviceId}/vehicle/{vehicleId}
```

### 4. POST - Đăng ký dịch vụ mới (không đổi)
```
POST /api/vehicleservices
Body: {
  "serviceId": "...",
  "vehicleId": "...",
  "serviceDescription": "...",
  "status": "pending"
}
```

## ⚠️ Lưu Ý

### 1. Business Logic
- **Một xe chỉ có thể đăng ký một dịch vụ một lần**
- Nếu đăng ký lại cùng dịch vụ, sẽ **UPDATE** thay vì tạo mới
- Nếu muốn cho phép đăng ký nhiều lần, cần thêm `request_date` vào composite key

### 2. Database
- **Backup database** trước khi chạy script migration
- **Khởi động lại service** sau khi chạy script SQL
- Kiểm tra dữ liệu hiện tại - nếu có duplicate (service_id, vehicle_id), sẽ lỗi khi tạo primary key

### 3. Frontend/UI
- Cần cập nhật UI để sử dụng `serviceId` và `vehicleId` thay vì `registrationId`
- Cập nhật API calls để dùng endpoints mới

## 🔍 Kiểm Tra Sau Migration

### 1. Kiểm Tra Database

```sql
-- Kiểm tra cấu trúc bảng
DESCRIBE vehicle_management.vehicleservice;

-- Kiểm tra PRIMARY KEY
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME,
    ORDINAL_POSITION
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicleservice'
  AND CONSTRAINT_NAME = 'PRIMARY'
ORDER BY ORDINAL_POSITION;

-- Kết quả mong đợi:
-- service_id (ORDINAL_POSITION = 1)
-- vehicle_id (ORDINAL_POSITION = 2)
```

### 2. Kiểm Tra Service

```bash
# Test endpoint
curl -X GET http://localhost:8083/api/vehicleservices/test

# Test get all
curl -X GET http://localhost:8083/api/vehicleservices

# Test register service
curl -X POST http://localhost:8083/api/vehicleservices \
  -H "Content-Type: application/json" \
  -d '{
    "serviceId": "SVC001",
    "vehicleId": "VEH001",
    "serviceDescription": "Test",
    "status": "pending"
  }'
```

## ✅ Checklist

- [x] Tạo `VehicleServiceId` composite key class
- [x] Sửa `Vehicleservice` entity
- [x] Sửa `VehicleServiceRepository`
- [x] Sửa `VehicleServiceService`
- [x] Sửa `VehicleServiceAPI` controller
- [x] Tạo script SQL migration
- [ ] Chạy script SQL migration
- [ ] Khởi động lại service
- [ ] Test API endpoints
- [ ] Cập nhật UI/Frontend (nếu có)

## 🐛 Troubleshooting

### Lỗi: "Duplicate entry for key 'PRIMARY'"
- **Nguyên nhân**: Đã có dữ liệu duplicate (service_id, vehicle_id)
- **Giải pháp**: Xóa hoặc merge các bản ghi duplicate trước khi chạy migration

### Lỗi: "Unknown column 'registration_id'"
- **Nguyên nhân**: Script SQL chưa được chạy
- **Giải pháp**: Chạy script SQL migration

### Lỗi: "Cannot find entity with composite key"
- **Nguyên nhân**: ID không được khởi tạo đúng
- **Giải pháp**: Đảm bảo `initializeId()` được gọi trước khi save

