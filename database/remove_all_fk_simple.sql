-- =====================================================
-- XÓA TẤT CẢ FOREIGN KEY CONSTRAINTS LIÊN QUAN ĐẾN VEHICLEGROUP
-- Phiên bản đơn giản - chỉ xóa nếu tồn tại
-- =====================================================

USE legal_contract;

-- Xóa foreign key constraint từ legalcontract đến vehiclegroup
-- Lưu ý: Nếu constraint không tồn tại sẽ báo lỗi, nhưng không ảnh hưởng
ALTER TABLE legalcontract DROP FOREIGN KEY legalcontract_ibfk_1;

