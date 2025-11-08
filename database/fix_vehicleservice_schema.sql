-- =====================================================
-- SỬA LẠI BẢNG SERVICE VÀ VEHICLESERVICE CHO ĐÚNG
-- =====================================================

USE vehicle_management;

-- Bước 1: Kiểm tra và tạo bảng service nếu chưa có
CREATE TABLE IF NOT EXISTS service (
    service_id VARCHAR(20) PRIMARY KEY,
    service_name VARCHAR(255) NOT NULL,
    INDEX idx_service_name (service_name)
);

-- Bước 2: Xóa bảng vehicleservice cũ nếu có (CHỈ CHẠY NẾU KHÔNG CÓ DỮ LIỆU QUAN TRỌNG)
-- DROP TABLE IF EXISTS vehicleservice;

-- Bước 3: Tạo lại bảng vehicleservice với cấu trúc đúng
CREATE TABLE IF NOT EXISTS vehicleservice (
    registration_id INT AUTO_INCREMENT PRIMARY KEY,
    service_id VARCHAR(20) NOT NULL,
    vehicle_id VARCHAR(20) NOT NULL,
    service_name VARCHAR(255),
    service_description TEXT,
    service_type VARCHAR(50),
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'pending',
    completion_date TIMESTAMP NULL,
    FOREIGN KEY (service_id) REFERENCES service(service_id) ON DELETE RESTRICT,
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id) ON DELETE RESTRICT,
    INDEX idx_vehicle_id (vehicle_id),
    INDEX idx_service_id (service_id),
    INDEX idx_status (status),
    INDEX idx_request_date (request_date)
);

-- Bước 4: Nếu bảng vehicleservice đã tồn tại, sửa lại cấu trúc
-- Kiểm tra và thêm cột registration_id nếu chưa có
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'vehicle_management' 
    AND TABLE_NAME = 'vehicleservice' 
    AND COLUMN_NAME = 'registration_id'
);

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE vehicleservice ADD COLUMN registration_id INT AUTO_INCREMENT PRIMARY KEY FIRST',
    'SELECT "Column registration_id already exists" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Đảm bảo service_id là NOT NULL
ALTER TABLE vehicleservice 
MODIFY COLUMN service_id VARCHAR(20) NOT NULL;

-- Đảm bảo vehicle_id là NOT NULL
ALTER TABLE vehicleservice 
MODIFY COLUMN vehicle_id VARCHAR(20) NOT NULL;

-- Đảm bảo status có default value
ALTER TABLE vehicleservice 
MODIFY COLUMN status VARCHAR(50) DEFAULT 'pending';

-- Thêm foreign key constraint nếu chưa có
SET @fk_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
    WHERE TABLE_SCHEMA = 'vehicle_management' 
    AND TABLE_NAME = 'vehicleservice' 
    AND CONSTRAINT_NAME = 'fk_vehicleservice_service'
);

SET @sql2 = IF(@fk_exists = 0,
    'ALTER TABLE vehicleservice ADD CONSTRAINT fk_vehicleservice_service FOREIGN KEY (service_id) REFERENCES service(service_id) ON DELETE RESTRICT',
    'SELECT "Foreign key fk_vehicleservice_service already exists" AS message'
);

PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- Thêm foreign key constraint cho vehicle_id nếu chưa có
SET @fk_exists2 = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
    WHERE TABLE_SCHEMA = 'vehicle_management' 
    AND TABLE_NAME = 'vehicleservice' 
    AND CONSTRAINT_NAME = 'fk_vehicleservice_vehicle'
);

SET @sql3 = IF(@fk_exists2 = 0,
    'ALTER TABLE vehicleservice ADD CONSTRAINT fk_vehicleservice_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id) ON DELETE RESTRICT',
    'SELECT "Foreign key fk_vehicleservice_vehicle already exists" AS message'
);

PREPARE stmt3 FROM @sql3;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

-- Thêm indexes
CREATE INDEX IF NOT EXISTS idx_vehicle_id ON vehicleservice(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_service_id ON vehicleservice(service_id);
CREATE INDEX IF NOT EXISTS idx_status ON vehicleservice(status);
CREATE INDEX IF NOT EXISTS idx_request_date ON vehicleservice(request_date);

SELECT 'Bảng service và vehicleservice đã được sửa lại thành công!' AS result;



