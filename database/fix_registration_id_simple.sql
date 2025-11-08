-- =====================================================
-- FIX REGISTRATION_ID - SCRIPT ĐỂN GIẢN
-- =====================================================

USE vehicle_management;

-- Bước 1: Kiểm tra cấu trúc hiện tại
SELECT '=== KIỂM TRA CẤU TRÚC ===' AS step;
SHOW COLUMNS FROM vehicleservice WHERE Field = 'registration_id';

-- Bước 2: Xóa và tạo lại cột registration_id
SELECT '=== SỬA LẠI CỘT ===' AS step;

-- Tắt foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Xóa cột cũ (nếu có)
ALTER TABLE vehicleservice DROP COLUMN registration_id;

-- Tạo lại cột với AUTO_INCREMENT PRIMARY KEY
ALTER TABLE vehicleservice 
ADD COLUMN registration_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;

-- Bật lại foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Bước 3: Kiểm tra lại
SELECT '=== KIỂM TRA LẠI ===' AS step;
SHOW COLUMNS FROM vehicleservice WHERE Field = 'registration_id';

-- Bước 4: Kiểm tra AUTO_INCREMENT
SELECT '=== KIỂM TRA AUTO_INCREMENT ===' AS step;
SHOW TABLE STATUS FROM vehicle_management WHERE Name = 'vehicleservice';

SELECT '=== HOÀN TẤT ===' AS step;
SELECT 'Đã sửa xong! Bạn có thể test insert.' AS result;

