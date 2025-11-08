-- =====================================================
-- XÓA CỘT REGISTRATION_ID VÀ TẠO COMPOSITE PRIMARY KEY
-- Primary key sẽ là (service_id, vehicle_id)
-- =====================================================

USE vehicle_management;

-- Bước 1: Kiểm tra cấu trúc hiện tại
SELECT '=== KIỂM TRA CẤU TRÚC HIỆN TẠI ===' AS step;
DESCRIBE vehicleservice;

-- Bước 2: Xóa PRIMARY KEY hiện tại (nếu có)
SELECT '=== XÓA PRIMARY KEY CŨ ===' AS step;

-- Lấy tên constraint PRIMARY KEY
SET @pk_name = (
    SELECT CONSTRAINT_NAME
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = 'vehicle_management'
      AND TABLE_NAME = 'vehicleservice'
      AND CONSTRAINT_TYPE = 'PRIMARY KEY'
    LIMIT 1
);

-- Xóa PRIMARY KEY nếu có
SET @sql_drop_pk = IF(@pk_name IS NOT NULL,
    CONCAT('ALTER TABLE vehicle_management.vehicleservice DROP PRIMARY KEY'),
    'SELECT "Không có PRIMARY KEY để xóa" AS message'
);

PREPARE stmt_drop_pk FROM @sql_drop_pk;
EXECUTE stmt_drop_pk;
DEALLOCATE PREPARE stmt_drop_pk;

-- Bước 3: Xóa cột registration_id (nếu có)
SELECT '=== XÓA CỘT REGISTRATION_ID ===' AS step;

SET FOREIGN_KEY_CHECKS = 0;

-- Kiểm tra xem cột có tồn tại không
SET @col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'vehicle_management'
      AND TABLE_NAME = 'vehicleservice'
      AND COLUMN_NAME = 'registration_id'
);

-- Xóa cột nếu có
SET @sql_drop_col = IF(@col_exists > 0,
    'ALTER TABLE vehicle_management.vehicleservice DROP COLUMN registration_id',
    'SELECT "Cột registration_id không tồn tại" AS message'
);

PREPARE stmt_drop_col FROM @sql_drop_col;
EXECUTE stmt_drop_col;
DEALLOCATE PREPARE stmt_drop_col;

SET FOREIGN_KEY_CHECKS = 1;

-- Bước 4: Đảm bảo service_id và vehicle_id là NOT NULL
SELECT '=== ĐẢM BẢO SERVICE_ID VÀ VEHICLE_ID LÀ NOT NULL ===' AS step;

ALTER TABLE vehicle_management.vehicleservice
MODIFY COLUMN service_id VARCHAR(20) NOT NULL;

ALTER TABLE vehicle_management.vehicleservice
MODIFY COLUMN vehicle_id VARCHAR(20) NOT NULL;

-- Bước 5: Tạo composite PRIMARY KEY (service_id, vehicle_id)
SELECT '=== TẠO COMPOSITE PRIMARY KEY ===' AS step;

ALTER TABLE vehicle_management.vehicleservice
ADD PRIMARY KEY (service_id, vehicle_id);

-- Bước 6: Kiểm tra lại cấu trúc
SELECT '=== KIỂM TRA LẠI ===' AS step;
DESCRIBE vehicleservice;

-- Kiểm tra PRIMARY KEY
SELECT '=== KIỂM TRA PRIMARY KEY ===' AS step;
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME,
    ORDINAL_POSITION
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicleservice'
  AND CONSTRAINT_NAME = 'PRIMARY'
ORDER BY ORDINAL_POSITION;

SELECT '=== HOÀN TẤT ===' AS step;
SELECT 'Đã xóa cột registration_id và tạo composite PRIMARY KEY (service_id, vehicle_id)!' AS result;
SELECT 'Vui lòng KHỞI ĐỘNG LẠI SERVICE!' AS important_note;

