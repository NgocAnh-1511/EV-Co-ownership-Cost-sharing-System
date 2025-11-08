-- =====================================================
-- TẠO CỘT REGISTRATION_ID
-- Chạy script này để tạo cột registration_id
-- =====================================================

USE vehicle_management;

-- Bước 1: Kiểm tra cấu trúc hiện tại
DESCRIBE vehicleservice;

-- Bước 2: Tạo cột registration_id
-- Xóa cột cũ nếu có
SET FOREIGN_KEY_CHECKS = 0;
ALTER TABLE vehicleservice DROP COLUMN IF EXISTS registration_id;
SET FOREIGN_KEY_CHECKS = 1;

-- Tạo cột mới
ALTER TABLE vehicleservice 
ADD COLUMN registration_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;

-- Bước 3: Kiểm tra lại
DESCRIBE vehicleservice;

SELECT 'Hoàn tất! Cột registration_id đã được tạo.' AS ket_qua;
SELECT 'Vui lòng KHỞI ĐỘNG LẠI SERVICE!' AS chu_y;

