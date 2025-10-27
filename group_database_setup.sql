-- ===============================
-- DATABASE: Group_Management_DB
-- ===============================
CREATE DATABASE IF NOT EXISTS Group_Management_DB;
USE Group_Management_DB;

-- 1️⃣ Bảng GROUP (Nhóm đồng sở hữu)
CREATE TABLE `Group` (
    groupId INT AUTO_INCREMENT PRIMARY KEY,
    groupName VARCHAR(100) NOT NULL,
    adminId INT NOT NULL,
    vehicleId INT,
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('Active', 'Inactive') DEFAULT 'Active'
);

-- 2️⃣ Bảng GROUP_MEMBER (Thành viên nhóm)
CREATE TABLE GroupMember (
    memberId INT AUTO_INCREMENT PRIMARY KEY,
    groupId INT NOT NULL,
    userId INT NOT NULL,
    role ENUM('Admin', 'Member') DEFAULT 'Member',
    joinedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (groupId) REFERENCES `Group`(groupId) ON DELETE CASCADE
);

-- 3️⃣ Bảng VOTING (Phiên bỏ phiếu)
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

-- 4️⃣ Bảng VOTING_RESULT (Kết quả bỏ phiếu theo thành viên)
CREATE TABLE VotingResult (
    resultId INT AUTO_INCREMENT PRIMARY KEY,
    voteId INT NOT NULL,
    memberId INT NOT NULL,
    choice ENUM('A','B') NOT NULL,
    votedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (voteId) REFERENCES Voting(voteId) ON DELETE CASCADE,
    FOREIGN KEY (memberId) REFERENCES GroupMember(memberId) ON DELETE CASCADE
);

-- Insert sample data
INSERT INTO `Group` (groupName, adminId, vehicleId, status) VALUES 
('EV Group Tesla Model 3', 1, 1, 'Active'),
('EV Group BMW i3', 2, 2, 'Active');

INSERT INTO GroupMember (groupId, userId, role) VALUES 
(1, 1, 'Admin'),
(1, 2, 'Member'),
(1, 3, 'Member'),
(2, 2, 'Admin'),
(2, 4, 'Member');

INSERT INTO Voting (groupId, topic, optionA, optionB, totalVotes) VALUES 
(1, 'Có nên mua thêm phụ kiện cho xe?', 'Có', 'Không', 0),
(2, 'Có nên đổi màu xe?', 'Đổi', 'Giữ nguyên', 0);
