# 📋 Tổng quan dự án: Hệ thống Quản lý Đồng sở hữu & Chia sẻ Chi phí Xe Điện

## 🏗️ Kiến trúc hệ thống

Dự án sử dụng **Microservices Architecture** với 3 service chính:

### 1. **cost-payment-service** (Port: 8081)
- **Database**: `Cost_Payment_DB` (MySQL)
- **Chức năng**: Quản lý chi phí, chia sẻ chi phí, thanh toán, quỹ chung, theo dõi sử dụng

### 2. **group-management-service** (Port: 8082)
- **Database**: `Group_Management_DB` (MySQL)
- **Chức năng**: Quản lý nhóm đồng sở hữu, thành viên, bỏ phiếu

### 3. **ui-service** (Port: 8080)
- **Chức năng**: Giao diện người dùng (Thymeleaf), tích hợp với 2 service trên

---

## ✅ Các chức năng ĐÃ TRIỂN KHAI

### 🏠 **1. Quản lý Nhóm (Group Management)**

#### ✅ Đã có:
- ✅ Tạo nhóm đồng sở hữu (`Group`)
- ✅ Quản lý thành viên (`GroupMember`)
  - Thêm/xóa thành viên
  - Phân quyền: Admin, Member
  - Quản lý tỷ lệ sở hữu (`ownershipPercent`)
  - Validation: Tổng tỷ lệ ≤ 100%
  - Rule: Phải có ít nhất 1 Admin
- ✅ Bỏ phiếu (`Voting`, `VotingResult`)
  - Tạo quyết định cần bỏ phiếu
  - Bỏ phiếu: Đồng ý (A) / Không đồng ý (D)
  - Logic: >50% đồng ý + Admin đồng ý → Chấp nhận
- ✅ Quỹ chung (`GroupFund`, `FundTransaction`)
  - Tạo quỹ tự động khi tạo nhóm
  - Nạp tiền vào quỹ
  - Rút tiền từ quỹ
  - Lịch sử giao dịch

#### 📍 Endpoints:
- `GET /api/groups` - Lấy tất cả nhóm
- `POST /api/groups` - Tạo nhóm mới
- `GET /api/groups/{id}` - Lấy nhóm theo ID
- `GET /api/groups/user/{userId}` - Lấy nhóm của user
- `GET /api/groups/{groupId}/members` - Lấy thành viên
- `POST /api/groups/{groupId}/members` - Thêm thành viên
- `PUT /api/groups/{groupId}/members/{memberId}` - Cập nhật thành viên
- `DELETE /api/groups/{groupId}/members/{memberId}` - Xóa thành viên
- `GET /api/groups/{groupId}/votes` - Lấy danh sách bỏ phiếu
- `POST /api/groups/{groupId}/votes` - Tạo bỏ phiếu
- `POST /api/groups/votes/{voteId}/results` - Bỏ phiếu

---

### 💰 **2. Quản lý Chi phí (Cost Management)**

#### ✅ Đã có:
- ✅ Tạo chi phí (`Cost`)
  - Loại chi phí: Sạc điện, Bảo dưỡng, Bảo hiểm, Đăng kiểm, Vệ sinh, Khác
  - Trạng thái: `PENDING` (chưa chia), `SHARED` (đã chia)
- ✅ Chia chi phí (`CostShare`)
  - Chia theo tỷ lệ sở hữu
  - Chia theo phần trăm tùy chỉnh
  - Chia theo mức độ sử dụng (Usage-based)
  - Tự động chia (`AutoSplit`)
- ✅ Theo dõi sử dụng (`UsageTracking`)
  - Quãng đường, thời gian sử dụng
  - Tính toán chi phí dựa trên sử dụng

#### 📍 Endpoints:
- `GET /api/costs` - Lấy tất cả chi phí
- `POST /api/costs` - Tạo chi phí mới
- `GET /api/costs/{id}` - Lấy chi phí theo ID
- `PUT /api/costs/{id}` - Cập nhật chi phí
- `DELETE /api/costs/{id}` - Xóa chi phí
- `GET /api/costs/{costId}/shares` - Lấy danh sách chia sẻ
- `POST /api/costs/{costId}/calculate-shares` - Tính toán chia sẻ
- `POST /api/shares` - Tạo chia sẻ
- `PUT /api/shares/{id}` - Cập nhật chia sẻ
- `DELETE /api/shares/{id}` - Xóa chia sẻ
- `POST /api/auto-split` - Tự động chia theo tỷ lệ sở hữu
- `GET /api/usage-tracking` - Lấy lịch sử sử dụng
- `POST /api/usage-tracking` - Thêm bản ghi sử dụng

---

### 💳 **3. Quản lý Thanh toán (Payment Management)**

#### ✅ Đã có:
- ✅ Thanh toán (`Payment`)
  - Trạng thái: `PENDING`, `PAID`, `OVERDUE`, `CANCELLED`
  - Liên kết với `CostShare`
  - Thanh toán trực tuyến (có endpoint)
- ✅ Theo dõi thanh toán
  - Lấy thanh toán theo user
  - Lấy thanh toán theo cost
  - Lọc theo trạng thái

#### 📍 Endpoints:
- `GET /api/payments` - Lấy tất cả thanh toán
- `GET /api/payments/{id}` - Lấy thanh toán theo ID
- `GET /api/payments/user/{userId}` - Lấy thanh toán của user
- `GET /api/payments/cost/{costId}` - Lấy thanh toán của cost
- `POST /api/payments` - Tạo thanh toán
- `PUT /api/payments/{id}` - Cập nhật thanh toán
- `POST /api/payments/{id}/pay` - Thực hiện thanh toán

---

### 🎯 **4. Giao diện Người dùng (UI)**

#### ✅ Đã có:
- ✅ **Trang chủ** (`/`)
  - Giới thiệu hệ thống
  - Các tính năng chính
  - Quick start guide

- ✅ **User Dashboard** (`/user`)
  - Dashboard cá nhân
  - Lịch sử sử dụng
  - Chi phí cá nhân

- ✅ **Quản lý Nhóm** (`/groups`)
  - Danh sách nhóm
  - Tạo nhóm (`/groups/create`)
  - Quản lý thành viên
  - Bỏ phiếu (`/groups/voting`)
  - Quỹ chung (`/groups/fund`)

- ✅ **Quản lý Chi phí** (`/costs`)
  - Danh sách chi phí
  - Tạo chi phí (`/costs/create`)
  - Chia sẻ chi phí (`/costs/sharing`)
  - Tự động chia (`/costs/auto-split`)
  - Theo dõi sử dụng (`/costs/usage-tracking`)

- ✅ **Admin Panel** (`/admin`)
  - Tổng quan (`/admin/overview`)
  - Quản lý nhóm (`/admin/groups`)
  - Quản lý chi phí (`/admin/costs`)
  - Quản lý quỹ (`/admin/funds`)
  - Theo dõi thanh toán (`/admin/payments`)
  - Tự động chia (`/admin/auto-split`)

---

## ❌ Các chức năng CHƯA TRIỂN KHAI (so với yêu cầu)

### 🔐 **1. Xác thực & Phân quyền**
- ❌ Đăng ký tài khoản
- ❌ Xác thực (CMND/CCCD, giấy phép lái xe)
- ❌ Đăng nhập/Đăng xuất
- ❌ Session management
- ❌ Phân quyền theo role (Co-owner, Staff, Admin)

### 📅 **2. Đặt lịch & Sử dụng xe**
- ❌ Lịch chung hiển thị thời gian xe trống/đang sử dụng
- ❌ Đặt lịch trước để đảm bảo quyền sử dụng
- ❌ Hệ thống ưu tiên công bằng dựa trên:
  - Tỷ lệ sở hữu
  - Lịch sử sử dụng
- ❌ Check-in/Check-out khi nhận và trả xe
- ❌ Quét QR code
- ❌ Ký số (Digital signature)

### 📄 **3. Hợp đồng Pháp lý**
- ❌ Quản lý hợp đồng đồng sở hữu (e-contract)
- ❌ Upload/Download hợp đồng
- ❌ Ký số trên hợp đồng
- ❌ Lưu trữ hợp đồng điện tử

### 💳 **4. Thanh toán Trực tuyến**
- ⚠️ Có endpoint nhưng chưa tích hợp:
  - ❌ E-wallet
  - ❌ Banking integration
  - ❌ Payment gateway (VNPay, MoMo, etc.)

### 📊 **5. Báo cáo & Phân tích**
- ❌ Bảng tổng kết chi phí theo tháng/quý
- ❌ So sánh mức sử dụng với tỷ lệ sở hữu
- ❌ Phân tích chi tiết cá nhân
- ❌ Xuất báo cáo tài chính minh bạch
- ❌ Export PDF/Excel

### 🤖 **6. AI Gợi ý**
- ❌ AI phân tích sử dụng xe
- ❌ Đề xuất lịch sử dụng công bằng
- ❌ Gợi ý phân bổ chi phí tối ưu

### 👥 **7. Quản lý Tranh chấp**
- ❌ Theo dõi & giám sát tranh chấp
- ❌ Xử lý khiếu nại
- ❌ Lịch sử tranh chấp

### 🚗 **8. Quản lý Xe**
- ❌ Thông tin xe (Vehicle entity)
- ❌ Quản lý nhiều xe trong một nhóm
- ❌ Liên kết chi phí với xe cụ thể

### 📱 **9. Mobile App**
- ❌ Ứng dụng di động
- ❌ Push notifications

---

## 📊 Entity Models hiện có

### **Cost Payment Service:**
1. `Cost` - Chi phí
2. `CostShare` - Chia sẻ chi phí
3. `CostSplitDetail` - Chi tiết chia sẻ
4. `Payment` - Thanh toán
5. `PaymentStatus` - Trạng thái thanh toán
6. `GroupFund` - Quỹ chung
7. `FundTransaction` - Giao dịch quỹ
8. `UsageTracking` - Theo dõi sử dụng
9. `TransactionVote` - Bỏ phiếu giao dịch
10. `SplitMethod` - Phương thức chia

### **Group Management Service:**
1. `Group` - Nhóm đồng sở hữu
2. `GroupMember` - Thành viên nhóm
3. `Voting` - Bỏ phiếu
4. `VotingResult` - Kết quả bỏ phiếu

---

## 🔧 Công nghệ sử dụng

- **Backend**: Spring Boot, Spring Data JPA
- **Frontend**: Thymeleaf, HTML, CSS, JavaScript
- **Database**: MySQL
- **Architecture**: Microservices
- **Containerization**: Docker, Docker Compose
- **Build Tool**: Maven

---

## 📝 Ghi chú

1. **Authentication**: Hiện tại hệ thống chưa có authentication, đang dùng `userId` qua query param hoặc hardcode
2. **Vehicle Management**: Có trường `vehicleId` trong `Cost` và `Group` nhưng chưa có entity `Vehicle` riêng
3. **Payment Integration**: Có cấu trúc thanh toán nhưng chưa tích hợp payment gateway thực tế
4. **File Upload**: Chưa có chức năng upload file (hợp đồng, ảnh, etc.)

---

## 🎯 Đề xuất phát triển tiếp theo

### **Ưu tiên cao:**
1. ✅ **Authentication & Authorization** - Cần thiết cho production
2. ✅ **Đặt lịch sử dụng xe** - Core feature
3. ✅ **Check-in/Check-out với QR** - Core feature
4. ✅ **Hợp đồng điện tử** - Legal requirement

### **Ưu tiên trung bình:**
5. ✅ **Tích hợp Payment Gateway** - Thanh toán thực tế
6. ✅ **Báo cáo & Export** - Business intelligence
7. ✅ **Quản lý Vehicle** - Hoàn thiện data model

### **Ưu tiên thấp:**
8. ✅ **AI Gợi ý** - Nice to have
9. ✅ **Mobile App** - Future expansion
10. ✅ **Quản lý tranh chấp** - Advanced feature

---

**Cập nhật lần cuối**: Dựa trên codebase hiện tại

