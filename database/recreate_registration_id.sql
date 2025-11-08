-- =====================================================
-- XÓA VÀ TẠO LẠI CỘT REGISTRATION_ID ĐÚNG CÁCH
-- =====================================================

USE vehicle_management;

-- Bước 1: Kiểm tra dữ liệu hiện tại (backup thông tin)
SELECT '=== BACKUP THÔNG TIN ===' AS step;
SELECT 
    registration_id,
    service_id,
    vehicle_id,
    request_date
FROM vehicleservice
ORDER BY registration_id DESC
LIMIT 10;

-- Bước 2: Xóa cột registration_id cũ
SELECT '=== XÓA CỘT REGISTRATION_ID CŨ ===' AS step;

-- Xóa foreign key constraints liên quan (nếu có)
SET @fk_name = NULL;
SELECT CONSTRAINT_NAME INTO @fk_name
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicleservice'
  AND COLUMN_NAME = 'registration_id'
  AND CONSTRAINT_NAME != 'PRIMARY'
LIMIT 1;

-- Xóa cột registration_id
ALTER TABLE vehicleservice 
DROP COLUMN IF EXISTS registration_id;

SELECT 'Đã xóa cột registration_id cũ' AS result;

-- Bước 3: Tạo lại cột registration_id với AUTO_INCREMENT PRIMARY KEY
SELECT '=== TẠO LẠI CỘT REGISTRATION_ID ===' AS step;

ALTER TABLE vehicleservice 
ADD COLUMN registration_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;

SELECT 'Đã tạo lại cột registration_id với AUTO_INCREMENT PRIMARY KEY' AS result;

-- Bước 4: Kiểm tra cấu trúc sau khi tạo lại
SELECT '=== KIỂM TRA CẤU TRÚC SAU KHI TẠO LẠI ===' AS step;
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
  AND COLUMN_NAME = 'registration_id';

-- Bước 5: Kiểm tra AUTO_INCREMENT
SELECT '=== KIỂM TRA AUTO_INCREMENT ===' AS step;
SELECT 
    TABLE_NAME,
    AUTO_INCREMENT
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicleservice';

-- Bước 6: Kiểm tra PRIMARY KEY
SELECT '=== KIỂM TRA PRIMARY KEY ===' AS step;
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME,
    ORDINAL_POSITION
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicleservice'
  AND CONSTRAINT_NAME = 'PRIMARY';

-- Bước 7: Test insert (optional - chỉ để kiểm tra)
-- SELECT '=== TEST INSERT (OPTIONAL) ===' AS step;
-- INSERT INTO vehicleservice (service_id, vehicle_id, service_name, service_type, status, request_date)
-- SELECT service_id, vehicle_id, service_name, service_type, status, request_date
-- FROM vehicleservice
-- LIMIT 1;
-- 
-- SELECT LAST_INSERT_ID() AS test_generated_id;
-- 
-- DELETE FROM vehicleservice WHERE registration_id = LAST_INSERT_ID();

SELECT '=== HOÀN TẤT ===' AS step;
SELECT 'Cột registration_id đã được xóa và tạo lại thành công!' AS result;
SELECT 'Bạn có thể test insert để kiểm tra AUTO_INCREMENT hoạt động đúng' AS note;

