-- =====================================================
-- FIX REGISTRATION_ID - CHẠY NGAY
-- =====================================================

USE vehicle_management;

-- Hiển thị cấu trúc hiện tại
DESCRIBE vehicleservice;

-- Xóa cột cũ nếu có
SET FOREIGN_KEY_CHECKS = 0;
ALTER TABLE vehicleservice DROP COLUMN IF EXISTS registration_id;
SET FOREIGN_KEY_CHECKS = 1;

-- Tạo cột registration_id
ALTER TABLE vehicleservice 
ADD COLUMN registration_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;

-- Kiểm tra lại
DESCRIBE vehicleservice;

SELECT 'Done! Cột registration_id đã được tạo.' AS result;

