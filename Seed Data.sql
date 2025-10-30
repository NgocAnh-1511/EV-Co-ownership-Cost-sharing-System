-- DỮ LIỆU MẪU (SEED DATA)

-- Mật khẩu: '12345678' (đã được mã hóa BCrypt)
SET @PASSWORD_HASH = '$2a$10$w09d8UuF4e7hA1f7U9F65O.T7Q0T7B2P4H5J6K7L8M9N0O1P2R3S4';

-- 1. TẠO NGƯỜI DÙNG CẦN THIẾT
INSERT INTO Users (email, password_hash, full_name, role, is_verified, phone_number) VALUES
('admin@carshare.com', @PASSWORD_HASH, 'Quản Trị Viên', 'ROLE_ADMIN', TRUE, '0901234567'), -- ID 1 (Admin)
('user1@example.com', @PASSWORD_HASH, 'Nguyễn Văn A', 'ROLE_USER', TRUE, '0918765432'),   -- ID 2 (User đã duyệt)
('user2@example.com', @PASSWORD_HASH, 'Trần Thị B', 'ROLE_USER', FALSE, '0987654321');  -- ID 3 (User chưa duyệt/chờ cập nhật hồ sơ)

-- 2. TẠO TÀI SẢN (ASSETS)
INSERT INTO Assets (asset_name, identifier, description, total_value, image_url) VALUES
('VinFast VF8', '29A-12345', 'Xe điện SUV, màu trắng, phiên bản Plus', 1000000000.00, '/uploads/vf8.jpg'),
('Wuling Hongguang Mini EV', '30F-98765', 'Xe điện cỡ nhỏ, màu vàng', 250000000.00, '/uploads/wuling.jpg'),
('Tesla Model Y', '51B-00112', 'Xe điện, nhập khẩu', 2200000000.00, '/uploads/tesla.jpg');

-- Lấy IDs của Asset vừa tạo
SET @VF8_ID = LAST_INSERT_ID() - 2;
SET @WULING_ID = LAST_INSERT_ID() - 1;
SET @TESLA_ID = LAST_INSERT_ID();

-- 3. TẠO HỢP ĐỒNG (CONTRACTS)
INSERT INTO Contracts (asset_id, title, status, start_date, end_date) VALUES
(@VF8_ID, 'HĐ Đồng Sở Hữu VF8 - A & B', 'active', '2025-01-01', '2026-01-01'), -- ID 1
(@WULING_ID, 'HĐ Đồng Sở Hữu Wuling - A', 'finished', '2024-05-15', '2025-05-15'), -- ID 2
(@TESLA_ID, 'HĐ Mua Sắm Tesla - Admin', 'pending', '2025-11-01', NULL); -- ID 3

-- Lấy IDs của Contract vừa tạo
SET @CONTRACT_VF8 = LAST_INSERT_ID() - 2;
SET @CONTRACT_WULING = LAST_INSERT_ID() - 1;
SET @CONTRACT_TESLA = LAST_INSERT_ID();

-- 4. TẠO QUYỀN SỞ HỮU (OWNERSHIP)
INSERT INTO Ownership (user_id, asset_id, contract_id, ownership_percentage) VALUES
-- VF8 (Chia sẻ giữa User A và User B)
(2, @VF8_ID, @CONTRACT_VF8, 60.00), -- User A (ID 2) sở hữu 60%
(3, @VF8_ID, @CONTRACT_VF8, 40.00), -- User B (ID 3) sở hữu 40%

-- Wuling (Chỉ User A)
(2, @WULING_ID, @CONTRACT_WULING, 100.00),

-- Tesla (Admin đang chuẩn bị mua)
(1, @TESLA_ID, @CONTRACT_TESLA, 100.00);

-- Các bảng còn lại (Expenses, Transactions,...) được tạo rỗng