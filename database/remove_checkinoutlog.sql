-- =====================================================
-- XÓA BẢNG CHECKINOUTLOG VÀ FOREIGN KEY CONSTRAINT
-- =====================================================

USE legal_contract;

-- Xóa foreign key constraint nếu tồn tại
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
     WHERE CONSTRAINT_SCHEMA = 'legal_contract' 
     AND TABLE_NAME = 'checkinoutlog' 
     AND CONSTRAINT_NAME = 'checkinoutlog_ibfk_2') > 0,
    CONCAT('ALTER TABLE checkinoutlog DROP FOREIGN KEY checkinoutlog_ibfk_2;'),
    'SELECT "Foreign key constraint does not exist" AS message;'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Xóa bảng checkinoutlog nếu tồn tại
DROP TABLE IF EXISTS checkinoutlog;

SELECT 'Bảng checkinoutlog và foreign key constraint đã được xóa thành công!' AS result;

