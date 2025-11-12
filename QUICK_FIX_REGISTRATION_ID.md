# 🚀 QUICK FIX - Registration ID

## ⚡ Cách Sửa Nhanh

### Bước 1: Chạy Script SQL

```bash
mysql -u root -p < database/fix_registration_id_simple.sql
```

Hoặc trong MySQL:

```sql
USE vehicle_management;

SET FOREIGN_KEY_CHECKS = 0;
ALTER TABLE vehicleservice DROP COLUMN registration_id;
ALTER TABLE vehicleservice 
ADD COLUMN registration_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;
SET FOREIGN_KEY_CHECKS = 1;
```

### Bước 2: Kiểm Tra

```sql
SHOW COLUMNS FROM vehicle_management.vehicleservice WHERE Field = 'registration_id';
```

Kết quả mong đợi:
- `Extra`: `auto_increment`
- `Key`: `PRI`

### Bước 3: Khởi Động Lại Service

Sau khi chạy script, khởi động lại service và test lại.

## 🔍 Kiểm Tra Database

Nếu vẫn lỗi, kiểm tra:

```sql
-- Kiểm tra AUTO_INCREMENT
SELECT AUTO_INCREMENT 
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicleservice';

-- Kiểm tra dữ liệu
SELECT * FROM vehicle_management.vehicleservice ORDER BY registration_id DESC LIMIT 5;
```

## ✅ Sau Khi Fix

Code đã được cập nhật với nhiều fallback:
1. Thử `LAST_INSERT_ID()`
2. Query lại bằng `service_id`, `vehicle_id`, `request_date`
3. Lấy `MAX(registration_id)`
4. Query từ repository

Vì vậy ngay cả khi `LAST_INSERT_ID()` không hoạt động, code vẫn sẽ lấy được ID bằng cách khác.

