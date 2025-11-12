-- Script đơn giản để sửa primary key của bảng vehicleservice
-- Chạy script này trong MySQL Workbench hoặc command line

USE vehicle_management;

-- Bước 1: Tắt foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Bước 2: Xóa primary key cũ (nếu có)
-- Lưu ý: MySQL sẽ báo lỗi nếu không có primary key, nhưng không sao
ALTER TABLE vehicleservice DROP PRIMARY KEY;

-- Bước 3: Xóa cột registration_id nếu có
-- Kiểm tra xem cột có tồn tại không trước khi xóa
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'vehicle_management' 
  AND TABLE_NAME = 'vehicleservice' 
  AND COLUMN_NAME = 'registration_id';

SET @sql = IF(@col_exists > 0, 
    'ALTER TABLE vehicleservice DROP COLUMN registration_id', 
    'SELECT "Cột registration_id không tồn tại" AS Message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Bước 4: Đảm bảo service_id và vehicle_id là NOT NULL và VARCHAR
ALTER TABLE vehicleservice 
    MODIFY COLUMN service_id VARCHAR(20) NOT NULL,
    MODIFY COLUMN vehicle_id VARCHAR(20) NOT NULL;

-- Bước 5: Xóa duplicate records (giữ lại bản ghi mới nhất)
-- Tạo bảng tạm để lưu các bản ghi cần giữ lại
CREATE TEMPORARY TABLE temp_vehicleservice_keep AS
SELECT service_id, vehicle_id, MAX(request_date) as max_date
FROM vehicleservice
GROUP BY service_id, vehicle_id;

-- Xóa các bản ghi duplicate (giữ lại bản mới nhất)
DELETE vs FROM vehicleservice vs
LEFT JOIN temp_vehicleservice_keep tk 
    ON vs.service_id = tk.service_id 
    AND vs.vehicle_id = tk.vehicle_id 
    AND vs.request_date = tk.max_date
WHERE tk.service_id IS NULL;

DROP TEMPORARY TABLE temp_vehicleservice_keep;

-- Bước 6: Tạo composite primary key mới
ALTER TABLE vehicleservice 
    ADD PRIMARY KEY (service_id, vehicle_id);

-- Bước 7: Bật lại foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Bước 8: Kiểm tra kết quả
SELECT '=== PRIMARY KEY SAU KHI SỬA ===' AS Info;
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME,
    ORDINAL_POSITION
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'vehicle_management' 
  AND TABLE_NAME = 'vehicleservice'
  AND CONSTRAINT_NAME = 'PRIMARY'
ORDER BY ORDINAL_POSITION;

SELECT '✅ Hoàn tất! Primary key hiện tại là composite key (service_id, vehicle_id)' AS Result;

