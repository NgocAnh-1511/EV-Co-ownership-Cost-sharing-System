# 🔧 Fix Service ID Format - SRV001, SRV002, SRV003, ...

## ❌ Vấn Đề

Trong bảng `vehicleservice`, `service_id` đang hiển thị là số (1, 7, 22) thay vì format SRV001, SRV002, SRV003, ...

## 🔍 Nguyên Nhân

1. **Dữ liệu cũ**: Service_id trong database có thể là số hoặc format cũ
2. **Foreign key mismatch**: `vehicleservice.service_id` không khớp với `service.service_id`
3. **Chưa migrate**: Dữ liệu chưa được migrate sang format mới

## ✅ Giải Pháp

### Bước 1: Kiểm Tra Dữ Liệu Hiện Tại

```bash
mysql -u root -p < database/check_service_data.sql
```

Hoặc trong MySQL:

```sql
USE vehicle_management;

-- Kiểm tra bảng service
SELECT * FROM service ORDER BY service_id;

-- Kiểm tra bảng vehicleservice
SELECT service_id, vehicle_id, service_name, status 
FROM vehicleservice 
ORDER BY service_id;

-- Kiểm tra foreign key
SELECT 
    vs.service_id AS vehicleservice_service_id,
    s.service_id AS service_service_id,
    s.service_name
FROM vehicleservice vs
LEFT JOIN service s ON vs.service_id = s.service_id;
```

### Bước 2: Migrate Service ID Sang Format SRV

**QUAN TRỌNG**: Backup database trước khi chạy script này!

```bash
mysql -u root -p < database/migrate_service_id_to_srv_format.sql
```

Hoặc trong MySQL:

```sql
USE vehicle_management;
SOURCE database/migrate_service_id_to_srv_format.sql;
```

### Bước 3: Kiểm Tra Kết Quả

```sql
-- Kiểm tra bảng service
SELECT * FROM service ORDER BY service_id;

-- Kiểm tra bảng vehicleservice
SELECT service_id, vehicle_id, service_name, status 
FROM vehicleservice 
ORDER BY service_id;

-- Kiểm tra foreign key
SELECT 
    COUNT(*) AS total,
    COUNT(CASE WHEN s.service_id IS NOT NULL THEN 1 END) AS valid_fk,
    COUNT(CASE WHEN s.service_id IS NULL THEN 1 END) AS invalid_fk
FROM vehicleservice vs
LEFT JOIN service s ON vs.service_id = s.service_id;
```

## 📝 Script Migration Chi Tiết

Script `migrate_service_id_to_srv_format.sql` sẽ:

1. **Backup dữ liệu**: Tạo bảng backup cho `service` và `vehicleservice`
2. **Tạo mapping**: Tạo bảng tạm mapping service_id cũ -> mới
3. **Generate service_id mới**: Tạo service_id mới theo format SRV001, SRV002, ...
4. **Cập nhật service table**: Cập nhật service_id trong bảng `service`
5. **Cập nhật vehicleservice table**: Cập nhật service_id trong bảng `vehicleservice`
6. **Kiểm tra**: Kiểm tra foreign key constraints

## ⚠️ Lưu Ý

### 1. Backup Database

**QUAN TRỌNG**: Luôn backup database trước khi chạy migration!

```sql
-- Backup toàn bộ database
mysqldump -u root -p vehicle_management > vehicle_management_backup.sql

-- Hoặc chỉ backup bảng service và vehicleservice
mysqldump -u root -p vehicle_management service vehicleservice > service_backup.sql
```

### 2. Kiểm Tra Dữ Liệu

Trước khi migrate, kiểm tra:
- Có bao nhiêu service hiện tại?
- Service_id hiện tại là gì?
- Có service_id nào đã theo format SRV chưa?

### 3. Foreign Key Constraints

Script sẽ tạm thời tắt foreign key checks để cập nhật dữ liệu. Sau khi cập nhật xong, sẽ bật lại và kiểm tra.

### 4. Orphaned Records

Nếu có records trong `vehicleservice` không khớp với `service`, script sẽ cảnh báo. Bạn cần xử lý các records này thủ công.

## 🧪 Test Sau Khi Migrate

### 1. Test Tạo Service Mới

```bash
curl -X POST http://localhost:8083/api/services \
  -H "Content-Type: application/json" \
  -d '{
    "serviceName": "Test Service",
    "serviceType": "test"
  }'
```

**Kết quả mong đợi**: Service_id tự động generate là SRV001, SRV002, ... (tùy vào service_id lớn nhất hiện có)

### 2. Test Lấy Service

```bash
curl -X GET http://localhost:8083/api/services
```

**Kết quả mong đợi**: Tất cả service_id đều có format SRV001, SRV002, ...

### 3. Test Đăng Ký Dịch Vụ

```bash
curl -X POST http://localhost:8083/api/vehicleservices \
  -H "Content-Type: application/json" \
  -d '{
    "serviceId": "SRV001",
    "vehicleId": "VEH001",
    "serviceDescription": "Test",
    "status": "pending"
  }'
```

**Kết quả mong đợi**: Đăng ký thành công với service_id = SRV001

## 🔄 Nếu Cần Rollback

Nếu migration không thành công, có thể rollback:

```sql
USE vehicle_management;

-- Restore từ backup
DROP TABLE IF EXISTS service;
CREATE TABLE service AS SELECT * FROM service_backup;

DROP TABLE IF EXISTS vehicleservice;
CREATE TABLE vehicleservice AS SELECT * FROM vehicleservice_backup;
```

## ✅ Checklist

- [ ] Backup database
- [ ] Chạy script kiểm tra dữ liệu
- [ ] Chạy script migration
- [ ] Kiểm tra kết quả
- [ ] Test tạo service mới
- [ ] Test đăng ký dịch vụ
- [ ] Kiểm tra foreign key constraints

