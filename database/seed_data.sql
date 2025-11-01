-- =====================================================
-- DỮ LIỆU MẪU - CarRental MicroServices System
-- =====================================================

-- ==============================
-- 1️⃣ DỮ LIỆU CHO VEHICLE_MANAGEMENT
-- ==============================
USE vehicle_management;

-- Nhóm xe
INSERT INTO vehiclegroup (group_name, description) VALUES
('Nhóm Sedan', 'Xe 4 chỗ cao cấp'),
('Nhóm SUV', 'Xe gầm cao, phù hợp đi xa'),
('Nhóm City', 'Xe nhỏ gọn, tiết kiệm nhiên liệu'),
('Nhóm Tesla Model 3', 'Xe điện Tesla - 5 thành viên'),
('Nhóm BMW i3', 'Xe điện BMW - 3 thành viên');

-- Xe
INSERT INTO vehicle (group_id, vehicle_number, vehicle_type, status) VALUES
-- Group 1 - Sedan
(1, '30A-12345', 'Toyota Camry', 'available'),
(1, '30A-67890', 'Honda Civic', 'available'),
-- Group 2 - SUV
(2, '30A-11111', 'BMW X5', 'in service'),
(2, '30A-22222', 'Mazda CX5', 'available'),
-- Group 3 - City
(3, '30A-33333', 'Kia Seltos', 'available'),
(3, '30A-44444', 'Toyota Corolla', 'available'),
-- Group 4 - Tesla
(4, '30A-TSLA1', 'Tesla Model 3', 'available'),
(4, '30A-TSLA2', 'Tesla Model 3', 'in service'),
-- Group 5 - BMW
(5, '30A-BMW01', 'BMW i3', 'maintenance'),
(5, '30A-BMW02', 'BMW i3', 'available');

-- Dịch vụ xe
INSERT INTO vehicleservice (vehicle_id, service_name, service_description, service_type, status, completion_date) VALUES
-- Maintenance
(3, 'Bảo dưỡng định kỳ 20,000km', 'Thay dầu, lọc gió, kiểm tra hệ thống', 'maintenance', 'completed', '2024-10-19 17:00:00'),
(9, 'Bảo dưỡng pin điện', 'Kiểm tra và bảo dưỡng pin xe điện', 'maintenance', 'in_progress', NULL),

-- Repair
(7, 'Sửa chữa hệ thống điều hòa', 'Điều hòa không lạnh, cần kiểm tra gas', 'repair', 'pending', NULL),

-- Cleaning
(2, 'Vệ sinh nội thất sâu', 'Vệ sinh toàn bộ nội thất, ghế, thảm', 'cleaning', 'completed', '2024-10-22 09:00:00'),
(5, 'Rửa xe và đánh bóng', 'Vệ sinh ngoại thất, đánh bóng', 'cleaning', 'pending', NULL),

-- Inspection
(1, 'Kiểm tra an toàn', 'Kiểm tra phanh, đèn, hệ thống an toàn', 'inspection', 'completed', '2024-10-23 10:00:00');

-- Lịch sử xe
INSERT INTO vehiclehistory (group_id, user_id, usage_start, usage_end) VALUES
-- Group 1
(1, 101, '2024-10-20 08:00:00', '2024-10-20 12:00:00'),
(1, 102, '2024-10-21 08:00:00', '2024-10-21 17:00:00'),
-- Group 2
(2, 103, '2024-10-18 09:00:00', '2024-10-18 15:00:00'),
(2, 104, '2024-10-22 10:00:00', '2024-10-22 18:00:00'),
-- Group 3
(3, 105, '2024-10-20 10:00:00', '2024-10-20 16:00:00'),
-- Group 4
(4, 106, '2024-10-19 09:00:00', '2024-10-19 18:00:00'),
(4, 107, '2024-10-23 08:00:00', NULL),
-- Group 5
(5, 108, '2024-10-21 08:30:00', '2024-10-21 17:30:00');

-- ==============================
-- 2️⃣ DỮ LIỆU CHO LEGAL_CONTRACT
-- ==============================
USE legal_contract;

-- Hợp đồng pháp lý
INSERT INTO legalcontract (contract_code, contract_status, creation_date, signed_date) VALUES
('CONTRACT-EV-001', 'signed', '2024-10-15 09:00:00', '2024-10-15 14:00:00'),
('CONTRACT-EV-002', 'pending', '2024-10-18 10:00:00', NULL),
('CONTRACT-EV-003', 'signed', '2024-10-19 08:00:00', '2024-10-20 09:00:00'),
('CONTRACT-EV-004', 'draft', '2024-10-21 09:00:00', NULL),
('CONTRACT-EV-005', 'signed', '2024-10-22 08:30:00', '2024-10-22 09:30:00'),
('CONTRACT-EV-006', 'pending', '2024-10-23 10:00:00', NULL),
('CONTRACT-EV-007', 'archived', '2024-09-01 09:00:00', '2024-09-02 10:00:00'),
('CONTRACT-EV-008', 'signed', '2024-10-10 09:00:00', '2024-10-11 15:00:00');

-- Lịch sử hợp đồng
INSERT INTO contracthistory (contract_id, action, action_date) VALUES
(1, 'Contract created', '2024-10-15 09:00:00'),
(1, 'Contract signed', '2024-10-15 14:00:00'),
(3, 'Contract created', '2024-10-19 08:00:00'),
(3, 'Contract signed', '2024-10-20 09:00:00'),
(5, 'Contract signed', '2024-10-22 09:30:00'),
(7, 'Contract archived', '2024-09-05 10:00:00');

-- Chữ ký hợp đồng
INSERT INTO contractsignatures (contract_id, signer_id, signature_date) VALUES
(1, 201, '2024-10-15 14:00:00'),
(3, 202, '2024-10-20 09:00:00'),
(5, 203, '2024-10-22 09:30:00'),
(8, 204, '2024-10-11 15:00:00');

-- Check-in/Check-out log
INSERT INTO checkinoutlog (vehicle_id, checkin_time, checkout_time, status, vehicle_condition_before, 
                          vehicle_condition_after, notes, performed_by, qr_scan_time, signature_time, qr_code_data) VALUES
-- Check-in records
('VEH001', '2024-10-20 08:00:00', NULL, 'checkin', 'Xe tốt, đầy nhiên liệu 100%, sạch sẽ', NULL, 
 'Giao xe cho khách hàng Nguyễn Văn A', 'Admin User', '2024-10-20 08:00:00', NULL, 'VEH001'),

('VEH002', '2024-10-20 09:30:00', NULL, 'checkin', 'Xe mới, không hư hỏng, giấy tờ đầy đủ', NULL,
 'Khách hàng Trần Thị B thuê xe', 'Staff Manager', '2024-10-20 09:30:00', NULL, 'VEH002'),

-- Check-out records
('VEH005', '2024-10-21 10:15:00', '2024-10-21 16:30:00', 'checkout', 'Xe tốt, sẵn sàng', 
 'Xe trả lại trong tình trạng tốt, không hư hỏng', 'Đã trả xe đúng giờ', 'Admin User', 
 '2024-10-21 10:15:00', '2024-10-21 16:30:00', 'VEH005'),

('VEH004', '2024-10-21 14:00:00', NULL, 'checkin', 'Đầy xăng, giấy tờ OK', NULL,
 'Khách hàng Lê Văn C', 'Admin User', '2024-10-21 14:00:00', NULL, 'VEH004'),

('VEH007', '2024-10-22 08:45:00', '2024-10-22 17:00:00', 'checkout', 'Xe hoàn toàn mới', 
 'Xe sạch, không có hư hỏng, nhiên liệu đủ', 'Trả xe đúng giờ, khách hàng hài lòng', 'Staff Manager',
 '2024-10-22 08:45:00', '2024-10-22 17:00:00', 'VEH007');

COMMIT;




