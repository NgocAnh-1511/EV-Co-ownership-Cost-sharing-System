-- =====================================================
-- SỬA LẠI BẢNG SERVICE CHO ĐÚNG
-- =====================================================

USE vehicle_management;

-- Kiểm tra và thêm các cột nếu chưa có
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'vehicle_management' 
    AND TABLE_NAME = 'service' 
    AND COLUMN_NAME = 'created_date'
);

-- Thêm cột created_date nếu chưa có
SET @sql1 = IF(@col_exists = 0,
    'ALTER TABLE service ADD COLUMN created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP',
    'SELECT "Column created_date already exists" AS message'
);

PREPARE stmt1 FROM @sql1;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

-- Thêm cột updated_date nếu chưa có
SET @col_exists2 = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'vehicle_management' 
    AND TABLE_NAME = 'service' 
    AND COLUMN_NAME = 'updated_date'
);

SET @sql2 = IF(@col_exists2 = 0,
    'ALTER TABLE service ADD COLUMN updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP',
    'SELECT "Column updated_date already exists" AS message'
);

PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- Thêm index cho service_name nếu chưa có
CREATE INDEX IF NOT EXISTS idx_service_name ON service(service_name);

-- Đảm bảo service_id là NOT NULL và UNIQUE
ALTER TABLE service 
MODIFY COLUMN service_id VARCHAR(20) NOT NULL;

-- Đảm bảo service_name là NOT NULL
ALTER TABLE service 
MODIFY COLUMN service_name VARCHAR(255) NOT NULL;

-- Thêm cột service_type nếu chưa có
SET @col_exists3 = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'vehicle_management' 
    AND TABLE_NAME = 'service' 
    AND COLUMN_NAME = 'service_type'
);

SET @sql3 = IF(@col_exists3 = 0,
    'ALTER TABLE service ADD COLUMN service_type VARCHAR(50) NOT NULL DEFAULT "Khác" AFTER service_name',
    'SELECT "Column service_type already exists" AS message'
);

PREPARE stmt3 FROM @sql3;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

-- Thêm index cho service_type
CREATE INDEX IF NOT EXISTS idx_service_type ON service(service_type);

SELECT 'Bảng service đã được sửa lại thành công!' AS result;
SELECT 'Cấu trúc bảng service:' AS info;
DESCRIBE service;


