-- =====================================================
-- ĐẢM BẢO CỘT REGISTRATION_ID TỒN TẠI
-- Script này sẽ kiểm tra và tạo cột nếu chưa có
-- =====================================================

USE vehicle_management;

-- Bước 1: Kiểm tra cột registration_id có tồn tại không
SELECT '=== KIỂM TRA CỘT REGISTRATION_ID ===' AS step;

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

-- Bước 2: Kiểm tra xem cột có tồn tại không
SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'vehicle_management'
      AND TABLE_NAME = 'vehicleservice'
      AND COLUMN_NAME = 'registration_id'
);

-- Bước 3: Tạo cột nếu chưa tồn tại
SELECT '=== TẠO CỘT NẾU CHƯA CÓ ===' AS step;

SET @sql = IF(@column_exists = 0,
    'ALTER TABLE vehicle_management.vehicleservice ADD COLUMN registration_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST',
    'SELECT "Cột registration_id đã tồn tại" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Bước 4: Nếu cột đã tồn tại nhưng không có AUTO_INCREMENT, sửa lại
SELECT '=== SỬA LẠI CỘT NẾU CẦN ===' AS step;

-- Kiểm tra xem cột có AUTO_INCREMENT không
SET @has_auto_increment = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'vehicle_management'
      AND TABLE_NAME = 'vehicleservice'
      AND COLUMN_NAME = 'registration_id'
      AND EXTRA LIKE '%auto_increment%'
);

-- Nếu không có AUTO_INCREMENT, sửa lại
SET @sql2 = IF(@has_auto_increment = 0 AND @column_exists > 0,
    'ALTER TABLE vehicle_management.vehicleservice MODIFY COLUMN registration_id INT NOT NULL AUTO_INCREMENT',
    'SELECT "Cột registration_id đã có AUTO_INCREMENT hoặc chưa tồn tại" AS message'
);

PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- Bước 5: Kiểm tra lại cấu trúc
SELECT '=== KIỂM TRA LẠI ===' AS step;

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

-- Bước 6: Kiểm tra PRIMARY KEY
SELECT '=== KIỂM TRA PRIMARY KEY ===' AS step;

SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicleservice'
  AND CONSTRAINT_NAME = 'PRIMARY';

-- Nếu không có PRIMARY KEY trên registration_id, thêm vào
SET @has_primary_key = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = 'vehicle_management'
      AND TABLE_NAME = 'vehicleservice'
      AND COLUMN_NAME = 'registration_id'
      AND CONSTRAINT_NAME = 'PRIMARY'
);

SET @sql3 = IF(@has_primary_key = 0 AND @column_exists > 0,
    'ALTER TABLE vehicle_management.vehicleservice ADD PRIMARY KEY (registration_id)',
    'SELECT "PRIMARY KEY đã tồn tại hoặc cột chưa tồn tại" AS message'
);

PREPARE stmt3 FROM @sql3;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

-- Bước 7: Kiểm tra AUTO_INCREMENT value
SELECT '=== KIỂM TRA AUTO_INCREMENT ===' AS step;

SELECT 
    TABLE_NAME,
    AUTO_INCREMENT
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicleservice';

SELECT '=== HOÀN TẤT ===' AS step;
SELECT 'Đã đảm bảo cột registration_id tồn tại với AUTO_INCREMENT PRIMARY KEY!' AS result;

