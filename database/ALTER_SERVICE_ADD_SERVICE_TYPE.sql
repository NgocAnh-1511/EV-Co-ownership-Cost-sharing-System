-- =====================================================
-- THÊM CỘT SERVICE_TYPE VÀO BẢNG SERVICE
-- Chạy file này nếu bảng service đã tồn tại
-- =====================================================

USE vehicle_management;

-- Kiểm tra và thêm cột service_type nếu chưa có
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'vehicle_management' 
    AND TABLE_NAME = 'service' 
    AND COLUMN_NAME = 'service_type'
);

-- Thêm cột service_type nếu chưa có
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE service ADD COLUMN service_type VARCHAR(50) NOT NULL DEFAULT "Khác" AFTER service_name',
    'SELECT "Column service_type already exists" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Thêm index cho service_type nếu chưa có
CREATE INDEX IF NOT EXISTS idx_service_type ON service(service_type);

-- Cập nhật dữ liệu service_type cho các bản ghi hiện có (nếu chưa có)
UPDATE service SET service_type = 'Bảo dưỡng' 
WHERE service_name LIKE '%Bảo dưỡng%' OR service_name LIKE '%Thay dầu%' OR service_name LIKE '%Thay lọc%' OR service_name LIKE '%Cân bằng%' OR service_name LIKE '%pin EV%' OR service_name LIKE '%hộp số%' OR service_name LIKE '%bugi%'
AND (service_type IS NULL OR service_type = 'Khác');

UPDATE service SET service_type = 'Sửa chữa' 
WHERE service_name LIKE '%Sửa chữa%' OR service_name LIKE '%Thay%' OR service_name LIKE '%Kiểm tra phanh%' OR service_name LIKE '%Kiểm tra hệ thống điện%' OR service_name LIKE '%Kiểm tra động cơ%' OR service_name LIKE '%Kiểm tra hệ thống phanh%'
AND (service_type IS NULL OR service_type = 'Khác');

UPDATE service SET service_type = 'Vệ sinh' 
WHERE service_name LIKE '%Vệ sinh%' OR service_name LIKE '%Đánh bóng%' OR service_name LIKE '%Rửa xe%'
AND (service_type IS NULL OR service_type = 'Khác');

UPDATE service SET service_type = 'Kiểm tra' 
WHERE service_name LIKE '%Kiểm tra%' AND service_name NOT LIKE '%Sửa chữa%' AND service_name NOT LIKE '%Kiểm tra phanh%' AND service_name NOT LIKE '%Kiểm tra hệ thống điện%' AND service_name NOT LIKE '%Kiểm tra động cơ%' AND service_name NOT LIKE '%Kiểm tra hệ thống phanh%'
AND (service_type IS NULL OR service_type = 'Khác');

SELECT '✅ Bảng service đã được cập nhật với cột service_type!' AS result;
SELECT 'Cấu trúc bảng service:' AS info;
DESCRIBE service;


