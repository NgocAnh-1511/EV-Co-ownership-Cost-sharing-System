-- ===============================
-- DATABASE: Group_Management_DB
-- Version ĐƠN GIẢN - Đủ làm chức năng
-- ===============================

DROP DATABASE IF EXISTS Group_Management_DB;
CREATE DATABASE Group_Management_DB;
USE Group_Management_DB;

-- ==========================================
-- BẢNG CHÍNH
-- ==========================================

-- 1. Nhóm
CREATE TABLE `Group` (
    groupId INT AUTO_INCREMENT PRIMARY KEY,
    groupName VARCHAR(100) NOT NULL,
    adminId INT NOT NULL,
    vehicleId INT,
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('Active', 'Inactive') DEFAULT 'Active'
);

-- 2. Thành viên (THÊM ownershipPercent - Cho chức năng chia theo sở hữu)
CREATE TABLE GroupMember (
    memberId INT AUTO_INCREMENT PRIMARY KEY,
    groupId INT NOT NULL,
    userId INT NOT NULL,
    role ENUM('Admin', 'Member') DEFAULT 'Member',
    ownershipPercent DOUBLE DEFAULT 0,
    joinedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (groupId) REFERENCES `Group`(groupId) ON DELETE CASCADE
);

-- 3. Bỏ phiếu
CREATE TABLE Voting (
    voteId INT AUTO_INCREMENT PRIMARY KEY,
    groupId INT NOT NULL,
    topic VARCHAR(255) NOT NULL,
    optionA VARCHAR(100),
    optionB VARCHAR(100),
    finalResult VARCHAR(100),
    totalVotes INT DEFAULT 0,
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (groupId) REFERENCES `Group`(groupId) ON DELETE CASCADE
);

-- 4. Kết quả bỏ phiếu
CREATE TABLE VotingResult (
    resultId INT AUTO_INCREMENT PRIMARY KEY,
    voteId INT NOT NULL,
    memberId INT NOT NULL,
    choice ENUM('A','B') NOT NULL,
    votedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (voteId) REFERENCES Voting(voteId) ON DELETE CASCADE,
    FOREIGN KEY (memberId) REFERENCES GroupMember(memberId) ON DELETE CASCADE
);

-- ==========================================
-- DỮ LIỆU MẪU ĐƠN GIẢN
-- ==========================================

-- Nhóm
INSERT INTO `Group` (groupName, adminId, vehicleId, status) VALUES 
('EV Group Tesla Model 3', 1, 1, 'Active'),
('EV Group BMW i3', 2, 2, 'Active');

-- Thành viên với % sở hữu
INSERT INTO GroupMember (groupId, userId, role, ownershipPercent) VALUES 
-- Group 1: Tổng 100%
(1, 1, 'Admin', 50.0),   -- User 1: 50%
(1, 2, 'Member', 30.0),  -- User 2: 30%
(1, 3, 'Member', 20.0),  -- User 3: 20%

-- Group 2: Tổng 100%
(2, 2, 'Admin', 60.0),   -- User 2: 60%
(2, 4, 'Member', 40.0);  -- User 4: 40%

-- Bỏ phiếu
INSERT INTO Voting (groupId, topic, optionA, optionB, totalVotes) VALUES 
(1, 'Có nên mua phụ kiện?', 'Có', 'Không', 0),
(2, 'Có nên đổi màu xe?', 'Đổi', 'Giữ', 0);

-- ==========================================
-- XEM DỮ LIỆU
-- ==========================================

SELECT '=== NHÓM VÀ THÀNH VIÊN ===' as '';
SELECT 
    g.groupId,
    g.groupName,
    gm.userId,
    gm.role,
    CONCAT(gm.ownershipPercent,'%') as 'Sở hữu'
FROM GroupMember gm JOIN `Group` g ON gm.groupId=g.groupId
ORDER BY gm.groupId, gm.ownershipPercent DESC;

SELECT '=== KIỂM TRA TỔNG % ===' as '';
SELECT 
    groupId,
    SUM(ownershipPercent) as 'Tổng',
    IF(SUM(ownershipPercent)=100, '✅ OK', '❌ Sai') as 'Status'
FROM GroupMember GROUP BY groupId;

SELECT '✅ HOÀN TẤT!' as '';
