# 🚀 HƯỚNG DẪN FIX LỖI - CHẠY NGAY

## ❌ Lỗi Hiện Tại

```
Unknown column 'v1_0.registration_id' in 'field list'
```

## ✅ Giải Pháp - 3 Bước Đơn Giản

### Bước 1: Chạy SQL Để Tạo Cột

**Mở MySQL và chạy:**

```sql
USE vehicle_management;

-- Xóa cột cũ nếu có
SET FOREIGN_KEY_CHECKS = 0;
ALTER TABLE vehicleservice DROP COLUMN IF EXISTS registration_id;
SET FOREIGN_KEY_CHECKS = 1;

-- Tạo cột registration_id
ALTER TABLE vehicleservice 
ADD COLUMN registration_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;

-- Kiểm tra
DESCRIBE vehicleservice;
```

**Hoặc chạy file:**
```bash
mysql -u root -p < database/FIX_NOW.sql
```

### Bước 2: Kiểm Tra Kết Quả

```sql
SHOW COLUMNS FROM vehicle_management.vehicleservice WHERE Field = 'registration_id';
```

**Kết quả phải có:**
- `Extra`: `auto_increment`
- `Key`: `PRI`

### Bước 3: KHỞI ĐỘNG LẠI SERVICE

**QUAN TRỌNG**: Phải khởi động lại service để Hibernate reload schema!

```bash
# Stop service
# Start service lại
```

## 🔍 Kiểm Tra Nhanh

Nếu vẫn lỗi, kiểm tra:

```sql
-- Kiểm tra database
SELECT DATABASE();

-- Kiểm tra bảng
SHOW TABLES FROM vehicle_management LIKE 'vehicleservice';

-- Kiểm tra cột
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_KEY, EXTRA
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicleservice'
  AND COLUMN_NAME = 'registration_id';
```

## ⚠️ Lưu Ý

1. **Phải khởi động lại service** sau khi chạy SQL
2. **Backup database** nếu có dữ liệu quan trọng
3. **Kiểm tra logs** nếu vẫn lỗi

## ✅ Sau Khi Fix

Sau khi chạy SQL và khởi động lại service, lỗi sẽ được giải quyết!

