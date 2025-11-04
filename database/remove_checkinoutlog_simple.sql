-- =====================================================
-- XÓA BẢNG CHECKINOUTLOG VÀ FOREIGN KEY CONSTRAINT
-- Phiên bản đơn giản
-- =====================================================

USE legal_contract;

-- Xóa foreign key constraint
ALTER TABLE checkinoutlog DROP FOREIGN KEY IF EXISTS checkinoutlog_ibfk_2;

-- Xóa bảng checkinoutlog
DROP TABLE IF EXISTS checkinoutlog;

