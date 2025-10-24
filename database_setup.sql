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
