# 🔧 Hướng Dẫn Xóa Và Tạo Lại Cột Registration_ID

## 📋 Mục Đích

Xóa cột `registration_id` cũ và tạo lại với cấu hình đúng để fix lỗi "The database returned no natively generated values".

## ⚠️ LƯU Ý QUAN TRỌNG

**BACKUP DATABASE TRƯỚC KHI CHẠY SCRIPT!**

Script này sẽ:
- ✅ Xóa cột `registration_id` cũ
- ✅ Tạo lại cột `registration_id` với AUTO_INCREMENT PRIMARY KEY
- ⚠️ **Dữ liệu hiện tại sẽ bị mất ID** (nhưng dữ liệu khác vẫn giữ nguyên)

## 🚀 Các Bước Thực Hiện

### Bước 1: Backup Dữ Liệu (QUAN TRỌNG!)

```sql
-- Backup toàn bộ bảng vehicleservice
CREATE TABLE vehicleservice_backup AS SELECT * FROM vehicle_management.vehicleservice;
```

### Bước 2: Chạy Script SQL

```bash
mysql -u root -p < database/recreate_registration_id.sql
```

Hoặc chạy trong MySQL Workbench/Command Line:

```sql
USE vehicle_management;
SOURCE database/recreate_registration_id.sql;
```

### Bước 3: Kiểm Tra Kết Quả

```sql
-- Kiểm tra cấu trúc cột
SHOW COLUMNS FROM vehicle_management.vehicleservice WHERE Field = 'registration_id';

-- Kết quả mong đợi:
-- Field: registration_id
-- Type: int
-- Null: NO
-- Key: PRI
-- Default: NULL
-- Extra: auto_increment

-- Kiểm tra AUTO_INCREMENT
SELECT AUTO_INCREMENT 
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicleservice';
```

### Bước 4: Test Insert

```sql
-- Test insert một record
INSERT INTO vehicle_management.vehicleservice 
(service_id, vehicle_id, service_name, service_type, status, request_date)
VALUES 
('SVC001', 'VEH001', 'Test Service', 'maintenance', 'pending', NOW());

-- Kiểm tra ID được generate
SELECT LAST_INSERT_ID();

-- Xóa record test
DELETE FROM vehicle_management.vehicleservice WHERE registration_id = LAST_INSERT_ID();
```

### Bước 5: Khởi Động Lại Service

Sau khi chạy script, khởi động lại service và test lại chức năng đăng ký dịch vụ.

## 📝 Nội Dung Script

Script `recreate_registration_id.sql` sẽ:

1. **Kiểm tra dữ liệu hiện tại** - hiển thị 10 records gần nhất
2. **Xóa cột registration_id cũ** - DROP COLUMN
3. **Tạo lại cột registration_id** - với AUTO_INCREMENT PRIMARY KEY
4. **Kiểm tra cấu trúc** - đảm bảo đã tạo đúng
5. **Kiểm tra AUTO_INCREMENT** - đảm bảo hoạt động

## 🔍 Kiểm Tra Chi Tiết

### 1. Kiểm Tra Cấu Trúc Bảng

```sql
DESCRIBE vehicle_management.vehicleservice;
```

### 2. Kiểm Tra PRIMARY KEY

```sql
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicleservice'
  AND CONSTRAINT_NAME = 'PRIMARY';
```

### 3. Kiểm Tra AUTO_INCREMENT

```sql
SHOW TABLE STATUS FROM vehicle_management WHERE Name = 'vehicleservice';
```

## ⚠️ Nếu Có Lỗi

### Lỗi: "Cannot drop column 'registration_id' because it is referenced by foreign key"

```sql
-- Xóa foreign key constraints trước
-- (Script sẽ tự động xử lý)
```

### Lỗi: "Table doesn't exist"

```sql
-- Kiểm tra database và table
SHOW DATABASES;
USE vehicle_management;
SHOW TABLES;
```

### Lỗi: "Duplicate column name"

```sql
-- Cột đã tồn tại, chỉ cần sửa lại
ALTER TABLE vehicleservice 
MODIFY COLUMN registration_id INT NOT NULL AUTO_INCREMENT;
```

## ✅ Sau Khi Hoàn Tất

1. ✅ Cột `registration_id` đã được tạo lại với AUTO_INCREMENT PRIMARY KEY
2. ✅ Hibernate sẽ tự động lấy generated ID sau khi insert
3. ✅ Lỗi "The database returned no natively generated values" sẽ được giải quyết

## 📞 Hỗ Trợ

Nếu gặp vấn đề, kiểm tra:
- Logs của service
- Database connection
- MySQL version (nên dùng MySQL 5.7+ hoặc 8.0+)
- JDBC driver version

