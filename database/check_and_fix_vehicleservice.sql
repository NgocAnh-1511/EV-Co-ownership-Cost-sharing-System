-- =====================================================
-- KIỂM TRA VÀ SỬA LẠI BẢNG VEHICLESERVICE
-- =====================================================

USE vehicle_management;

-- Bước 1: Kiểm tra cấu trúc bảng hiện tại
SELECT '=== KIỂM TRA CẤU TRÚC BẢNG ===' AS step;
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_KEY,
    EXTRA,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicleservice'
ORDER BY ORDINAL_POSITION;

-- Bước 2: Kiểm tra AUTO_INCREMENT value
SELECT '=== KIỂM TRA AUTO_INCREMENT ===' AS step;
SELECT 
    TABLE_NAME,
    AUTO_INCREMENT
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicleservice';

-- Bước 3: Kiểm tra PRIMARY KEY
SELECT '=== KIỂM TRA PRIMARY KEY ===' AS step;
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME,
    ORDINAL_POSITION
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicleservice'
  AND CONSTRAINT_NAME = 'PRIMARY';

-- Bước 4: Kiểm tra dữ liệu hiện tại
SELECT '=== KIỂM TRA DỮ LIỆU ===' AS step;
SELECT 
    COUNT(*) AS total_records,
    MAX(registration_id) AS max_id,
    MIN(registration_id) AS min_id
FROM vehicleservice;

-- Bước 5: SỬA LẠI CẤU TRÚC BẢNG (CHỈ CHẠY NẾU CẦN)
-- Lưu ý: Backup dữ liệu trước khi chạy phần này!

-- 5.1: Đảm bảo registration_id là AUTO_INCREMENT PRIMARY KEY
ALTER TABLE vehicleservice 
MODIFY COLUMN registration_id INT NOT NULL AUTO_INCREMENT;

-- 5.2: Đặt lại AUTO_INCREMENT value dựa trên MAX ID hiện tại
SET @max_id = (SELECT COALESCE(MAX(registration_id), 0) FROM vehicleservice);
SET @new_auto_increment = @max_id + 1;
SET @sql = CONCAT('ALTER TABLE vehicleservice AUTO_INCREMENT = ', @new_auto_increment);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT CONCAT('Đã đặt AUTO_INCREMENT = ', @new_auto_increment) AS result;

-- Bước 6: Kiểm tra lại sau khi sửa
SELECT '=== KIỂM TRA SAU KHI SỬA ===' AS step;
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

SELECT 
    AUTO_INCREMENT
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicleservice';

SELECT '=== HOÀN TẤT ===' AS step;
SELECT 'Bảng vehicleservice đã được kiểm tra và sửa lại!' AS result;

