# ⚡ Quick Fix Service ID Format

## 🎯 Vấn Đề

Trong bảng `vehicleservice`, `service_id` đang hiển thị là số (1, 7, 22) thay vì format SRV001, SRV002, SRV003, ...

## ✅ Giải Pháp Nhanh

### Bước 1: Kiểm Tra Dữ Liệu

```sql
USE vehicle_management;

-- Kiểm tra service table
SELECT service_id, service_name FROM service ORDER BY service_id;

-- Kiểm tra vehicleservice table
SELECT service_id, vehicle_id, service_name FROM vehicleservice ORDER BY service_id;
```

### Bước 2: Chạy Script Fix

```bash
mysql -u root -p < database/fix_service_id_simple.sql
```

Hoặc trong MySQL:

```sql
USE vehicle_management;
SOURCE database/fix_service_id_simple.sql;
```

### Bước 3: Kiểm Tra Kết Quả

```sql
-- Kiểm tra service table
SELECT service_id, service_name FROM service ORDER BY service_id;

-- Kiểm tra vehicleservice table  
SELECT service_id, vehicle_id, service_name FROM vehicleservice ORDER BY service_id;

-- Kiểm tra foreign key
SELECT 
    vs.service_id,
    s.service_id AS service_table_id,
    s.service_name
FROM vehicleservice vs
LEFT JOIN service s ON vs.service_id = s.service_id
WHERE s.service_id IS NULL;
```

## 📝 Script Sẽ Làm Gì

1. **Backup dữ liệu**: Tạo bảng backup cho `service` và `vehicleservice`
2. **Tìm service_id lớn nhất**: Tìm service_id lớn nhất có format SRV (ví dụ: SRV025)
3. **Tạo mapping**: Tạo mapping service_id cũ -> mới cho các service chưa có format SRV
4. **Cập nhật service table**: Cập nhật service_id trong bảng `service`
5. **Cập nhật vehicleservice table**: Cập nhật service_id trong bảng `vehicleservice`
6. **Kiểm tra**: Kiểm tra foreign key constraints

## ⚠️ Lưu Ý

1. **Backup database** trước khi chạy script
2. Script sẽ tự động backup dữ liệu vào `service_backup` và `vehicleservice_backup`
3. Nếu có lỗi, có thể restore từ backup:
   ```sql
   DROP TABLE service;
   CREATE TABLE service AS SELECT * FROM service_backup;
   
   DROP TABLE vehicleservice;
   CREATE TABLE vehicleservice AS SELECT * FROM vehicleservice_backup;
   ```

## 🔄 Sau Khi Fix

Sau khi chạy script:
- Tất cả `service_id` sẽ có format SRV001, SRV002, SRV003, ...
- `vehicleservice.service_id` sẽ khớp với `service.service_id`
- Khi tạo service mới, sẽ tự động generate service_id tiếp theo (SRV004, SRV005, ...)

## 🧪 Test

```bash
# Test tạo service mới
curl -X POST http://localhost:8083/api/services \
  -H "Content-Type: application/json" \
  -d '{
    "serviceName": "Test Service",
    "serviceType": "test"
  }'

# Kiểm tra service_id được generate
curl -X GET http://localhost:8083/api/services
```

