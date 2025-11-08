-- =====================================================
-- SỬA LẠI AUTO_INCREMENT CHO BẢNG VEHICLESERVICE
-- =====================================================

USE vehicle_management;

-- Kiểm tra cấu trúc bảng hiện tại
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

-- Đảm bảo registration_id là AUTO_INCREMENT PRIMARY KEY
ALTER TABLE vehicleservice 
MODIFY COLUMN registration_id INT NOT NULL AUTO_INCREMENT;

-- Kiểm tra AUTO_INCREMENT value hiện tại
SELECT AUTO_INCREMENT 
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicleservice';

-- Nếu cần, đặt lại AUTO_INCREMENT value
-- SET @max_id = (SELECT COALESCE(MAX(registration_id), 0) FROM vehicleservice);
-- SET @sql = CONCAT('ALTER TABLE vehicleservice AUTO_INCREMENT = ', @max_id + 1);
-- PREPARE stmt FROM @sql;
-- EXECUTE stmt;
-- DEALLOCATE PREPARE stmt;

-- Kiểm tra lại cấu trúc sau khi sửa
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

SELECT 'Đã sửa lại AUTO_INCREMENT cho bảng vehicleservice thành công!' AS result;

