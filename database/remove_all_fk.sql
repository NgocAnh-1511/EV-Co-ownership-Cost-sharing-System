-- =====================================================
-- XÓA TẤT CẢ FOREIGN KEY CONSTRAINTS LIÊN QUAN ĐẾN VEHICLEGROUP
-- =====================================================

USE legal_contract;

-- Xóa foreign key constraint từ legalcontract đến vehiclegroup
-- MySQL không hỗ trợ IF EXISTS, cần kiểm tra trước khi xóa
SET @constraint_name = NULL;

SELECT CONSTRAINT_NAME INTO @constraint_name
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE CONSTRAINT_SCHEMA = 'legal_contract'
AND TABLE_NAME = 'legalcontract'
AND CONSTRAINT_NAME = 'legalcontract_ibfk_1'
AND CONSTRAINT_TYPE = 'FOREIGN KEY'
LIMIT 1;

SET @sql = IF(@constraint_name IS NOT NULL,
    CONCAT('ALTER TABLE legalcontract DROP FOREIGN KEY ', @constraint_name),
    'SELECT "Foreign key constraint does not exist" AS message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT 'Tất cả foreign key constraints đã được xóa thành công!' AS result;

