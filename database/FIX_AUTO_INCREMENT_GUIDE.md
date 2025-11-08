# 🔧 Hướng Dẫn Sửa Lỗi AUTO_INCREMENT cho Vehicleservice

## ❌ Lỗi Gặp Phải

```
The database returned no natively generated values : com.example.VehicleServiceManagementService.model.Vehicleservice
```

## 🔍 Nguyên Nhân

Lỗi này xảy ra khi:
1. Bảng `vehicleservice` chưa có `AUTO_INCREMENT` được cấu hình đúng
2. Cột `registration_id` không phải là `PRIMARY KEY` với `AUTO_INCREMENT`
3. Database không trả về generated keys sau khi insert

## ✅ Giải Pháp

### Bước 1: Chạy Script SQL để Sửa

```bash
# Windows
mysql -u root -p < database/fix_vehicleservice_autoincrement.sql

# Hoặc trong MySQL Workbench/Command Line
USE vehicle_management;
SOURCE database/fix_vehicleservice_autoincrement.sql;
```

### Bước 2: Kiểm Tra Cấu Trúc Bảng

Sau khi chạy script, kiểm tra xem bảng đã đúng chưa:

```sql
USE vehicle_management;

-- Kiểm tra cấu trúc cột registration_id
SHOW COLUMNS FROM vehicleservice WHERE Field = 'registration_id';

-- Kết quả mong đợi:
-- Field: registration_id
-- Type: int
-- Null: NO
-- Key: PRI
-- Default: NULL
-- Extra: auto_increment
```

### Bước 3: Kiểm Tra AUTO_INCREMENT Value

```sql
-- Kiểm tra giá trị AUTO_INCREMENT hiện tại
SELECT AUTO_INCREMENT 
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicleservice';

-- Nếu cần, đặt lại AUTO_INCREMENT
ALTER TABLE vehicleservice AUTO_INCREMENT = 1;
```

## 📝 Nội Dung Script

Script `fix_vehicleservice_autoincrement.sql` sẽ:
1. Kiểm tra cấu trúc bảng hiện tại
2. Sửa lại cột `registration_id` thành `AUTO_INCREMENT PRIMARY KEY`
3. Đảm bảo cột là `NOT NULL`
4. Kiểm tra lại cấu trúc sau khi sửa

## 🚀 Sau Khi Sửa

Sau khi chạy script, khởi động lại service và thử đăng ký dịch vụ lại. Lỗi sẽ được giải quyết.

## ⚠️ Lưu Ý

- **Backup database** trước khi chạy script nếu có dữ liệu quan trọng
- Nếu bảng đã có dữ liệu, đảm bảo không có conflict với AUTO_INCREMENT value
- Nếu gặp lỗi foreign key constraint, có thể cần xóa và tạo lại bảng (CHỈ nếu không có dữ liệu quan trọng)

