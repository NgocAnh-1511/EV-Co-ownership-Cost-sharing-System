# 🔧 Fix Lỗi: Unknown column 'v1_0.registration_id' in 'field list'

## ❌ Lỗi

```
Unknown column 'v1_0.registration_id' in 'field list'
```

## 🔍 Nguyên Nhân

Cột `registration_id` **không tồn tại** trong bảng `vehicleservice` trong database. Hibernate đang cố query nhưng không tìm thấy cột này.

## ✅ Giải Pháp

### Bước 1: Chạy Script SQL Để Tạo Cột

```bash
mysql -u root -p < database/create_registration_id_column.sql
```

Hoặc trong MySQL:

```sql
USE vehicle_management;

-- Kiểm tra cấu trúc hiện tại
DESCRIBE vehicleservice;

-- Tạo cột registration_id nếu chưa có
ALTER TABLE vehicleservice 
ADD COLUMN registration_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;
```

### Bước 2: Kiểm Tra Kết Quả

```sql
-- Kiểm tra cấu trúc
DESCRIBE vehicle_management.vehicleservice;

-- Kiểm tra cột registration_id
SHOW COLUMNS FROM vehicle_management.vehicleservice WHERE Field = 'registration_id';
```

Kết quả mong đợi:
- `Field`: `registration_id`
- `Type`: `int`
- `Null`: `NO`
- `Key`: `PRI`
- `Extra`: `auto_increment`

### Bước 3: Khởi Động Lại Service

**QUAN TRỌNG**: Sau khi chạy script SQL, **KHỞI ĐỘNG LẠI SERVICE** để Hibernate reload schema.

```bash
# Stop service
# Start service lại
```

### Bước 4: Test Lại

Sau khi khởi động lại service, test lại chức năng đăng ký dịch vụ.

## 🔍 Kiểm Tra Chi Tiết

### Kiểm Tra Bảng Có Tồn Tại Không

```sql
USE vehicle_management;
SHOW TABLES LIKE 'vehicleservice';
```

### Kiểm Tra Cấu Trúc Bảng

```sql
DESCRIBE vehicle_management.vehicleservice;
```

### Kiểm Tra Cột Registration_ID

```sql
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_KEY,
    EXTRA
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicleservice'
  AND COLUMN_NAME = 'registration_id';
```

## ⚠️ Lưu Ý

1. **Backup database** trước khi chạy script nếu có dữ liệu quan trọng
2. **Khởi động lại service** sau khi sửa database - Hibernate cần reload schema
3. **Kiểm tra dữ liệu** - nếu bảng đã có dữ liệu, cột mới sẽ được tạo với giá trị NULL hoặc AUTO_INCREMENT sẽ bắt đầu từ 1

## 🐛 Nếu Vẫn Lỗi

### Kiểm Tra Schema

Đảm bảo đang sử dụng đúng database:

```sql
SELECT DATABASE();
-- Phải trả về: vehicle_management
```

### Kiểm Tra Table Name

Đảm bảo tên bảng đúng:

```sql
SHOW TABLES FROM vehicle_management;
-- Phải có: vehicleservice
```

### Kiểm Tra Hibernate Schema

Trong `application.properties`, kiểm tra:
- `spring.jpa.hibernate.ddl-auto=update` - Hibernate sẽ tự động update schema
- Nhưng nếu có lỗi, có thể cần set thành `validate` và fix database thủ công

## ✅ Sau Khi Fix

Sau khi chạy script SQL và khởi động lại service:
1. ✅ Cột `registration_id` đã được tạo trong database
2. ✅ Hibernate có thể query được cột này
3. ✅ Lỗi "Unknown column" sẽ được giải quyết

## 📝 Files Liên Quan

- `database/create_registration_id_column.sql` - Script tạo cột registration_id
- `VehicleServiceManagementService/src/main/java/com/example/VehicleServiceManagementService/model/Vehicleservice.java` - Model với @Id registration_id

