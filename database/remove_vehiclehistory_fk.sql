-- =====================================================
-- XÓA FOREIGN KEY CONSTRAINT TỪ VEHICLEHISTORY ĐẾN VEHICLEGROUP
-- =====================================================

USE vehicle_management;

-- Xóa foreign key constraint từ vehiclehistory đến vehiclegroup
SET @constraint_name = NULL;

SELECT CONSTRAINT_NAME INTO @constraint_name
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE CONSTRAINT_SCHEMA = 'vehicle_management'
AND TABLE_NAME = 'vehiclehistory'
AND CONSTRAINT_NAME = 'vehiclehistory_ibfk_1'
AND CONSTRAINT_TYPE = 'FOREIGN KEY'
LIMIT 1;

SET @sql = IF(@constraint_name IS NOT NULL,
    CONCAT('ALTER TABLE vehicle_management.vehiclehistory DROP FOREIGN KEY ', @constraint_name),
    'SELECT "Foreign key constraint does not exist" AS message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT 'Foreign key constraint từ vehiclehistory đã được xóa thành công!' AS result;

