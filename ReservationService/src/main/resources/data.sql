-- Khởi tạo dữ liệu mẫu cho hệ thống quản lý xe đồng sở hữu

-- Xóa dữ liệu cũ (nếu có)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE reservations;
TRUNCATE TABLE group_members;
TRUNCATE TABLE vehicles;
TRUNCATE TABLE vehicle_groups;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- Tạo người dùng mẫu
INSERT INTO users (user_id, full_name, email, phone, username, password) VALUES
(1, 'Nguyễn Văn A', 'nguyenvana@email.com', '0901234567', 'nguyenvana', 'password123'),
(2, 'Trần Thị B', 'tranthib@email.com', '0912345678', 'tranthib', 'password123'),
(3, 'Lê Văn C', 'levanc@email.com', '0923456789', 'levanc', 'password123'),
(4, 'Phạm Thị D', 'phamthid@email.com', '0934567890', 'phamthid', 'password123'),
(5, 'Hoàng Văn E', 'hoangvane@email.com', '0945678901', 'hoangvane', 'password123');

-- Tạo nhóm xe đồng sở hữu (mỗi xe có một group riêng)
INSERT INTO vehicle_groups (group_id, group_name, description, creation_date, active) VALUES
('GRP001', 'Nhóm Honda City 2022', 'Nhóm cho Honda City 2022', NOW(), 'active'),
('GRP002', 'Nhóm Toyota Vios 2021', 'Nhóm cho Toyota Vios 2021', NOW(), 'active'),
('GRP003', 'Nhóm Mazda CX-5 2023', 'Nhóm cho Mazda CX-5 2023', NOW(), 'active'),
('GRP004', 'Nhóm Honda CR-V 2022', 'Nhóm cho Honda CR-V 2022', NOW(), 'active'),
('GRP005', 'Nhóm Toyota Camry 2023', 'Nhóm cho Toyota Camry 2023', NOW(), 'active');

-- Tạo xe (mỗi xe có một group_id riêng)
INSERT INTO vehicles (vehicle_id, vehicle_name, license_plate, vehicle_type, group_id, status) VALUES
(1, 'Honda City 2022', '51A-123.45', 'Sedan', 'GRP001', 'AVAILABLE'),
(2, 'Toyota Vios 2021', '51B-678.90', 'Sedan', 'GRP002', 'AVAILABLE'),
(3, 'Mazda CX-5 2023', '51C-111.22', 'SUV', 'GRP003', 'AVAILABLE'),
(4, 'Honda CR-V 2022', '51D-222.33', 'SUV', 'GRP004', 'AVAILABLE'),
(5, 'Toyota Camry 2023', '51E-333.44', 'Sedan', 'GRP005', 'AVAILABLE');

-- Thêm thành viên vào nhóm (với phần trăm sở hữu)
INSERT INTO group_members (group_id, user_id, ownership_percentage) VALUES
-- Nhóm GRP001 (Honda City): Nguyễn Văn A (70%), Trần Thị B (30%)
('GRP001', 1, 70.0),
('GRP001', 2, 30.0),
-- Nhóm GRP002 (Toyota Vios): Lê Văn C (50%), Nguyễn Văn A (25%), Phạm Thị D (25%)
('GRP002', 3, 50.0),
('GRP002', 1, 25.0),
('GRP002', 4, 25.0),
-- Nhóm GRP003 (Mazda CX-5): Hoàng Văn E (60%), Trần Thị B (40%)
('GRP003', 5, 60.0),
('GRP003', 2, 40.0),
-- Nhóm GRP004 (Honda CR-V): Lê Văn C (100%)
('GRP004', 3, 100.0),
-- Nhóm GRP005 (Toyota Camry): Hoàng Văn E (100%)
('GRP005', 5, 100.0);

-- Tạo lịch đặt xe mẫu
INSERT INTO reservations (vehicle_id, user_id, start_datetime, end_datetime, purpose, status) VALUES
-- Xe 1: Honda City
(1, 1, '2024-10-22 08:00:00', '2024-10-22 10:30:00', 'Đi họp khách hàng', 'COMPLETED'),
(1, 2, '2024-10-25 14:00:00', '2024-10-25 18:00:00', 'Đi mua sắm', 'BOOKED'),
(1, 1, '2024-10-28 09:00:00', '2024-10-28 17:00:00', 'Đi công tác', 'BOOKED'),

-- Xe 2: Toyota Vios
(2, 2, '2024-10-21 09:00:00', '2024-10-21 10:45:00', 'Đưa con đi học', 'COMPLETED'),
(2, 1, '2024-10-24 06:00:00', '2024-10-24 07:30:00', 'Đi sân bay', 'BOOKED'),

-- Xe 3: Mazda CX-5
(3, 3, '2024-10-20 08:00:00', '2024-10-20 11:15:00', 'Họp đối tác', 'COMPLETED'),
(3, 4, '2024-10-23 13:00:00', '2024-10-23 16:00:00', 'Khảo sát địa điểm', 'BOOKED'),
(3, 1, '2024-10-26 10:00:00', '2024-10-26 15:00:00', 'Gặp khách hàng', 'BOOKED'),

-- Xe 4: Honda CR-V
(4, 3, '2024-10-19 14:00:00', '2024-10-19 18:20:00', 'Đi bảo hiểm', 'COMPLETED'),
(4, 4, '2024-10-22 09:00:00', '2024-10-22 12:00:00', 'Đi ngân hàng', 'BOOKED'),

-- Xe 5: Toyota Camry
(5, 5, '2024-10-18 07:00:00', '2024-10-18 08:30:00', 'Đưa đón sân bay', 'COMPLETED'),
(5, 2, '2024-10-23 16:00:00', '2024-10-23 19:00:00', 'Đi ăn tối', 'BOOKED'),
(5, 5, '2024-10-27 11:00:00', '2024-10-27 14:00:00', 'Đi du lịch gần', 'BOOKED');

-- Thêm một số lịch đặt trong tương lai
INSERT INTO reservations (vehicle_id, user_id, start_datetime, end_datetime, purpose, status) VALUES
(1, 1, '2024-11-05 09:00:00', '2024-11-05 17:00:00', 'Họp hội nghị', 'BOOKED'),
(2, 2, '2024-11-08 14:00:00', '2024-11-08 18:00:00', 'Đi chơi cuối tuần', 'BOOKED'),
(3, 3, '2024-11-10 08:00:00', '2024-11-10 12:00:00', 'Gặp đối tác', 'BOOKED'),
(4, 4, '2024-11-12 10:00:00', '2024-11-12 16:00:00', 'Khảo sát dự án', 'BOOKED'),
(5, 5, '2024-11-15 13:00:00', '2024-11-15 19:00:00', 'Đi ăn liên hoan', 'BOOKED');

