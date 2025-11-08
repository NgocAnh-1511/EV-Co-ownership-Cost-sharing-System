-- Script kiểm tra và sửa bảng vehicleservice
-- Chạy script này để kiểm tra cấu trúc và sửa lỗi primary key

USE vehicle_management;

-- ============================================
-- BƯỚC 1: KIỂM TRA CẤU TRÚC BẢNG HIỆN TẠI
-- ============================================
SELECT '=== CẤU TRÚC BẢNG VEHICLESERVICE ===' AS Info;
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

-- ============================================
-- BƯỚC 2: KIỂM TRA PRIMARY KEY HIỆN TẠI
-- ============================================
SELECT '=== PRIMARY KEY HIỆN TẠI ===' AS Info;
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME,
    ORDINAL_POSITION,
    SEQ_IN_INDEX
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'vehicle_management' 
  AND TABLE_NAME = 'vehicleservice'
  AND CONSTRAINT_NAME = 'PRIMARY'
ORDER BY ORDINAL_POSITION;

-- ============================================
-- BƯỚC 3: KIỂM TRA DỮ LIỆU HIỆN TẠI
-- ============================================
SELECT '=== DỮ LIỆU HIỆN TẠI (10 dòng đầu) ===' AS Info;
SELECT 
    service_id,
    vehicle_id,
    service_name,
    service_type,
    status,
    request_date
FROM vehicleservice
ORDER BY request_date DESC
LIMIT 10;

-- ============================================
-- BƯỚC 4: KIỂM TRA DUPLICATE
-- ============================================
SELECT '=== KIỂM TRA DUPLICATE (service_id, vehicle_id) ===' AS Info;
SELECT 
    service_id,
    vehicle_id,
    COUNT(*) as count
FROM vehicleservice
GROUP BY service_id, vehicle_id
HAVING COUNT(*) > 1;

-- ============================================
-- BƯỚC 5: SỬA LẠI PRIMARY KEY
-- ============================================
SELECT '=== BẮT ĐẦU SỬA PRIMARY KEY ===' AS Info;

-- Tắt foreign key checks
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_SAFE_UPDATES = 0;

-- Xóa tất cả indexes và constraints liên quan đến primary key
SET @sql = (SELECT CONCAT('ALTER TABLE vehicleservice DROP PRIMARY KEY') 
            FROM INFORMATION_SCHEMA.TABLES 
            WHERE TABLE_SCHEMA = 'vehicle_management' 
              AND TABLE_NAME = 'vehicleservice');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Xóa cột registration_id nếu tồn tại (MySQL không hỗ trợ DROP COLUMN IF EXISTS, nên dùng procedure)
DROP PROCEDURE IF EXISTS drop_column_if_exists;
DELIMITER //
CREATE PROCEDURE drop_column_if_exists()
BEGIN
    DECLARE col_count INT;
    SELECT COUNT(*) INTO col_count
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'vehicle_management'
      AND TABLE_NAME = 'vehicleservice'
      AND COLUMN_NAME = 'registration_id';
    
    IF col_count > 0 THEN
        ALTER TABLE vehicleservice DROP COLUMN registration_id;
        SELECT 'Đã xóa cột registration_id' AS Result;
    ELSE
        SELECT 'Không có cột registration_id để xóa' AS Result;
    END IF;
END//
DELIMITER ;

CALL drop_column_if_exists();
DROP PROCEDURE IF EXISTS drop_column_if_exists;

-- Đảm bảo service_id và vehicle_id không null
ALTER TABLE vehicleservice 
    MODIFY COLUMN service_id VARCHAR(20) NOT NULL,
    MODIFY COLUMN vehicle_id VARCHAR(20) NOT NULL;

-- Xóa duplicate nếu có (giữ lại bản ghi mới nhất)
DELETE t1 FROM vehicleservice t1
INNER JOIN vehicleservice t2 
WHERE t1.service_id = t2.service_id 
  AND t1.vehicle_id = t2.vehicle_id
  AND t1.request_date < t2.request_date;

-- Tạo composite primary key mới
ALTER TABLE vehicleservice 
    ADD PRIMARY KEY (service_id, vehicle_id);

-- Bật lại foreign key checks
SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;

-- ============================================
-- BƯỚC 6: KIỂM TRA KẾT QUẢ
-- ============================================
SELECT '=== KIỂM TRA PRIMARY KEY SAU KHI SỬA ===' AS Info;
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME,
    ORDINAL_POSITION
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'vehicle_management' 
  AND TABLE_NAME = 'vehicleservice'
  AND CONSTRAINT_NAME = 'PRIMARY'
ORDER BY ORDINAL_POSITION;

SELECT '✅ Đã sửa xong! Primary key hiện tại là composite key (service_id, vehicle_id)' AS Result;

