-- =====================================================
-- XÓA FOREIGN KEY CONSTRAINT TỪ VEHICLEHISTORY ĐẾN VEHICLEGROUP
-- Phiên bản đơn giản
-- =====================================================

USE vehicle_management;

-- Xóa foreign key constraint từ vehiclehistory đến vehiclegroup
ALTER TABLE vehiclehistory DROP FOREIGN KEY vehiclehistory_ibfk_1;

