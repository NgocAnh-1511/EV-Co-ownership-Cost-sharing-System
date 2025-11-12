-- Script để sửa lại primary key của bảng vehicleservice thành composite key
-- Chạy script này để đảm bảo bảng dùng composite key (service_id, vehicle_id)

USE vehicle_management;

-- Bước 1: Kiểm tra cấu trúc bảng hiện tại
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_KEY,
    EXTRA
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'vehicle_management' 
  AND TABLE_NAME = 'vehicleservice'
ORDER BY ORDINAL_POSITION;

-- Bước 2: Kiểm tra primary key hiện tại
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME,
    ORDINAL_POSITION
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'vehicle_management' 
  AND TABLE_NAME = 'vehicleservice'
  AND CONSTRAINT_NAME = 'PRIMARY';

-- Bước 3: Tắt foreign key checks tạm thời
SET FOREIGN_KEY_CHECKS = 0;

-- Bước 4: Xóa primary key cũ (nếu có)
ALTER TABLE vehicleservice DROP PRIMARY KEY;

-- Bước 5: Xóa cột registration_id nếu còn tồn tại
ALTER TABLE vehicleservice DROP COLUMN IF EXISTS registration_id;

-- Bước 6: Đảm bảo service_id và vehicle_id không null
ALTER TABLE vehicleservice 
    MODIFY COLUMN service_id VARCHAR(20) NOT NULL,
    MODIFY COLUMN vehicle_id VARCHAR(20) NOT NULL;

-- Bước 7: Tạo composite primary key mới
ALTER TABLE vehicleservice 
    ADD PRIMARY KEY (service_id, vehicle_id);

-- Bước 8: Bật lại foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Bước 9: Kiểm tra lại cấu trúc sau khi sửa
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME,
    ORDINAL_POSITION
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'vehicle_management' 
  AND TABLE_NAME = 'vehicleservice'
  AND CONSTRAINT_NAME = 'PRIMARY'
ORDER BY ORDINAL_POSITION;

-- Bước 10: Kiểm tra dữ liệu hiện tại
SELECT 
    service_id,
    vehicle_id,
    service_name,
    service_type,
    status,
    request_date
FROM vehicleservice
LIMIT 10;

SELECT '✅ Đã sửa xong primary key thành composite key (service_id, vehicle_id)' AS Result;

