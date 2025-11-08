-- =====================================================
-- FIX REGISTRATION_ID - SCRIPT ĐƠN GIẢN NHẤT
-- Chạy script này để tạo cột registration_id
-- =====================================================

USE vehicle_management;

-- Hiển thị cấu trúc bảng hiện tại
SELECT '=== CẤU TRÚC BẢNG HIỆN TẠI ===' AS info;
DESCRIBE vehicleservice;

-- Kiểm tra xem cột registration_id có tồn tại không
SELECT '=== KIỂM TRA CỘT REGISTRATION_ID ===' AS info;
SELECT COUNT(*) AS column_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicleservice'
  AND COLUMN_NAME = 'registration_id';

-- Xóa cột cũ nếu có (để tạo lại)
SELECT '=== XÓA CỘT CŨ (NẾU CÓ) ===' AS info;
SET FOREIGN_KEY_CHECKS = 0;
ALTER TABLE vehicleservice DROP COLUMN IF EXISTS registration_id;
SET FOREIGN_KEY_CHECKS = 1;

-- Tạo lại cột registration_id với AUTO_INCREMENT PRIMARY KEY
SELECT '=== TẠO CỘT REGISTRATION_ID ===' AS info;
ALTER TABLE vehicleservice 
ADD COLUMN registration_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;

-- Kiểm tra lại
SELECT '=== KIỂM TRA LẠI ===' AS info;
DESCRIBE vehicleservice;

-- Hiển thị cột registration_id
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

-- Kiểm tra AUTO_INCREMENT
SELECT '=== KIỂM TRA AUTO_INCREMENT ===' AS info;
SELECT AUTO_INCREMENT 
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicleservice';

SELECT '=== HOÀN TẤT ===' AS info;
SELECT 'Cột registration_id đã được tạo thành công!' AS result;
SELECT 'Vui lòng KHỞI ĐỘNG LẠI SERVICE để Hibernate reload schema!' AS important_note;

