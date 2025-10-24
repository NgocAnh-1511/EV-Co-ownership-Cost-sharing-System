-- ===============================
-- DATABASE: Cost_Payment_DB
-- ===============================
CREATE DATABASE IF NOT EXISTS Cost_Payment_DB;
USE Cost_Payment_DB;

-- 1️⃣ Bảng COST (Chi phí)
CREATE TABLE Cost (
    costId INT AUTO_INCREMENT PRIMARY KEY,
    vehicleId INT NOT NULL,
    costType ENUM('ElectricCharge','Maintenance','Insurance','Inspection','Cleaning','Other') DEFAULT 'Other',
    amount DOUBLE NOT NULL,
    description TEXT,
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2️⃣ Bảng COST_SHARE (Chia chi phí giữa đồng sở hữu)
CREATE TABLE CostShare (
    shareId INT AUTO_INCREMENT PRIMARY KEY,
    costId INT NOT NULL,
    userId INT NOT NULL,
    percent DOUBLE DEFAULT 0,
    amountShare DOUBLE DEFAULT 0,
    calculatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (costId) REFERENCES Cost(costId) ON DELETE CASCADE
);

-- 3️⃣ Bảng PAYMENT (Thanh toán)
CREATE TABLE Payment (
    paymentId INT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    costId INT,
    method ENUM('EWallet','Banking','Cash') DEFAULT 'EWallet',
    amount DOUBLE NOT NULL,
    transactionCode VARCHAR(100),
    paymentDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('Pending','Completed','Failed') DEFAULT 'Pending',
    FOREIGN KEY (costId) REFERENCES Cost(costId) ON DELETE SET NULL
);

-- 4️⃣ Bảng GROUP_FUND (Quỹ chung)
CREATE TABLE GroupFund (
    fundId INT AUTO_INCREMENT PRIMARY KEY,
    groupId INT NOT NULL,
    totalContributed DOUBLE DEFAULT 0,
    currentBalance DOUBLE DEFAULT 0,
    updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    note TEXT
);

-- 5️⃣ Bảng FUND_TRANSACTION (Giao dịch quỹ)
CREATE TABLE FundTransaction (
    transactionId INT AUTO_INCREMENT PRIMARY KEY,
    fundId INT NOT NULL,
    userId INT,
    transactionType ENUM('Deposit','Withdraw') DEFAULT 'Deposit',
    amount DOUBLE NOT NULL,
    purpose VARCHAR(255),
    date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (fundId) REFERENCES GroupFund(fundId) ON DELETE CASCADE
);

-- Insert sample data
INSERT INTO Cost (vehicleId, costType, amount, description) VALUES 
(1, 'ElectricCharge', 500000, 'Chi phí sạc điện tháng 1'),
(1, 'Maintenance', 2000000, 'Bảo dưỡng định kỳ'),
(2, 'Insurance', 3000000, 'Bảo hiểm xe năm 2024');

INSERT INTO GroupFund (groupId, totalContributed, currentBalance, note) VALUES 
(1, 1000000, 800000, 'Quỹ chung nhóm Tesla Model 3'),
(2, 500000, 300000, 'Quỹ chung nhóm BMW i3');

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
