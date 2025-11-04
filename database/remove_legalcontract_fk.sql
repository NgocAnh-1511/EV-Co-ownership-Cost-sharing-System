-- =====================================================
-- XÓA FOREIGN KEY CONSTRAINT TỪ LEGALCONTRACT ĐẾN VEHICLEGROUP
-- =====================================================

USE legal_contract;

-- Xóa foreign key constraint nếu tồn tại
ALTER TABLE legalcontract DROP FOREIGN KEY IF EXISTS legalcontract_ibfk_1;

SELECT 'Foreign key constraint đã được xóa thành công!' AS result;

