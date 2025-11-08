-- =====================================================
-- TEST VÀ SỬA LẠI BẢNG VEHICLESERVICE
-- Script này sẽ test và fix lỗi AUTO_INCREMENT
-- =====================================================

USE vehicle_management;

-- Bước 1: Kiểm tra cấu trúc hiện tại
SELECT '=== BƯỚC 1: KIỂM TRA CẤU TRÚC ===' AS step;

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

-- Bước 2: Xóa cột registration_id nếu tồn tại
SELECT '=== BƯỚC 2: XÓA CỘT CŨ ===' AS step;

-- Tắt foreign key checks tạm thời
SET FOREIGN_KEY_CHECKS = 0;

-- Xóa cột registration_id (nếu có)
ALTER TABLE vehicleservice DROP COLUMN IF EXISTS registration_id;

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'Đã xóa cột registration_id cũ' AS result;

-- Bước 3: Tạo lại cột registration_id với AUTO_INCREMENT PRIMARY KEY
SELECT '=== BƯỚC 3: TẠO LẠI CỘT ===' AS step;

ALTER TABLE vehicleservice 
ADD COLUMN registration_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;

SELECT 'Đã tạo lại cột registration_id' AS result;

-- Bước 4: Kiểm tra lại cấu trúc
SELECT '=== BƯỚC 4: KIỂM TRA LẠI ===' AS step;

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

-- Bước 5: Kiểm tra AUTO_INCREMENT
SELECT '=== BƯỚC 5: KIỂM TRA AUTO_INCREMENT ===' AS step;

SELECT 
    TABLE_NAME,
    AUTO_INCREMENT
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicleservice';

-- Bước 6: TEST INSERT
SELECT '=== BƯỚC 6: TEST INSERT ===' AS step;

-- Kiểm tra xem có service và vehicle nào không
SELECT COUNT(*) AS service_count FROM service;
SELECT COUNT(*) AS vehicle_count FROM vehicle;

-- Test insert (chỉ nếu có dữ liệu)
-- INSERT INTO vehicleservice 
-- (service_id, vehicle_id, service_name, service_type, status, request_date)
-- SELECT 
--     (SELECT service_id FROM service LIMIT 1),
--     (SELECT vehicle_id FROM vehicle LIMIT 1),
--     'Test Service',
--     'maintenance',
--     'pending',
--     NOW()
-- WHERE EXISTS (SELECT 1 FROM service LIMIT 1) 
--   AND EXISTS (SELECT 1 FROM vehicle LIMIT 1);

-- SELECT LAST_INSERT_ID() AS test_generated_id;

-- DELETE FROM vehicleservice WHERE registration_id = LAST_INSERT_ID();

SELECT '=== HOÀN TẤT ===' AS step;
SELECT 'Bảng vehicleservice đã được sửa lại!' AS result;
SELECT 'Bạn có thể test insert để kiểm tra AUTO_INCREMENT hoạt động' AS note;

