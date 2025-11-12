image.png# PHÂN TÍCH YÊU CẦU DỰ ÁN
## EV Co-ownership & Cost-sharing System

---

## 📋 TỔNG QUAN DỰ ÁN

**Tên dự án:** Phần mềm quản lý đồng sở hữu & chia sẻ chi phí xe điện

**Actors:** 
- Co-owner (Chủ xe đồng sở hữu)
- Staff (Nhân viên vận hành)
- Admin (Quản trị viên)

---

## ✅ CÁC CHỨC NĂNG ĐÃ ĐƯỢC TRIỂN KHAI

### 1. Chức năng cho Chủ xe (Co-owner)

#### a. Quản lý tài khoản & quyền sở hữu
- ❌ **Đăng ký & xác thực (CMND/CCCD, giấy phép lái xe)** - CHƯA CÓ
- ✅ **Quản lý tỷ lệ sở hữu** - ĐÃ CÓ (`GroupMember.ownershipPercent`)
- ❌ **Quản lý hợp đồng đồng sở hữu (e-contract)** - CHƯA CÓ

#### b. Đặt lịch & sử dụng xe
- ❌ **Lịch chung hiển thị thời gian xe đang trống/đang sử dụng** - CHƯA CÓ
- ❌ **Đặt lịch trước để đảm bảo quyền sử dụng** - CHƯA CÓ
- ❌ **Hệ thống ưu tiên công bằng dựa trên tỉ lệ sở hữu & lịch sử sử dụng** - CHƯA CÓ

#### c. Chi phí & thanh toán
- ✅ **Tự động chia chi phí theo tỉ lệ sở hữu** - ĐÃ CÓ (`AutoCostSplitService` - `BY_OWNERSHIP`)
- ✅ **Tự động chia chi phí theo mức độ sử dụng** - ĐÃ CÓ (`AutoCostSplitService` - `BY_USAGE`)
- ✅ **Các khoản chi phí: phí sạc điện, bảo dưỡng, bảo hiểm, đăng kiểm, vệ sinh xe** - ĐÃ CÓ (`Cost.costType`: `ElectricCharge`, `Maintenance`, `Insurance`, `Inspection`, `Cleaning`)
- ✅ **Thanh toán trực tuyến (e-wallet, banking)** - ĐÃ CÓ (`Payment.method`: `EWALLET`, `BANKING`, `CASH`)
- ✅ **Bảng tổng kết chi phí theo tháng/quý** - ĐÃ CÓ (trong `UserDashboardController` và `AdminOverviewController`)

#### d. Lịch sử & phân tích cá nhân
- ✅ **Lịch sử sử dụng xe: thời gian, quãng đường** - ĐÃ CÓ (`UsageTracking` entity)
- ✅ **Chi phí phát sinh** - ĐÃ CÓ (`CostShare` và `Payment`)
- ✅ **So sánh mức sử dụng với tỉ lệ sở hữu** - CÓ THỂ TÍNH TOÁN (cần UI để hiển thị)

#### e. Nhóm đồng sở hữu
- ✅ **Quản lý nhóm: thêm/xoá thành viên** - ĐÃ CÓ (`GroupManagementService`)
- ✅ **Phân quyền (admin nhóm, thành viên)** - ĐÃ CÓ (`GroupMember.role`: `Admin`, `Member`)
- ✅ **Bỏ phiếu / quyết định chung** - ĐÃ CÓ (`Voting` và `VotingResult`)
- ✅ **Quỹ chung: quỹ bảo dưỡng, phí dự phòng** - ĐÃ CÓ (`GroupFund` và `FundTransaction`)
- ✅ **Hiển thị minh bạch số dư và lịch sử chi** - ĐÃ CÓ (`AdminFundsController` và UI)
- ❌ **AI gợi ý phân tích sử dụng xe để đề xuất lịch sử dụng công bằng** - CHƯA CÓ

### 2. Chức năng cho Nhà vận hành (Staff, Admin)

- ✅ **Quản lý nhóm xe đồng sở hữu** - ĐÃ CÓ (`AdminGroupsController`)
- ❌ **Quản lý hợp đồng pháp lý điện tử** - CHƯA CÓ
- ❌ **Quản lý Check-in/Check-out khi nhận và trả xe (quét QR, ký số)** - CHƯA CÓ
- ❌ **Quản lý thực hiện các dịch vụ xe** - CHƯA CÓ (chỉ có quản lý chi phí dịch vụ)
- ❌ **Theo dõi & giám sát tranh chấp (nếu có)** - CHƯA CÓ
- ✅ **Xuất báo cáo tài chính minh bạch cho từng nhóm** - ĐÃ CÓ (`AdminOverviewController` với thống kê)

---

## ❌ CÁC CHỨC NĂNG CÒN THIẾU

### 1. Quản lý tài khoản & xác thực
- [ ] Hệ thống đăng ký người dùng với CMND/CCCD
- [ ] Xác thực giấy phép lái xe
- [ ] Quản lý hợp đồng đồng sở hữu điện tử (e-contract)
- [ ] Upload và lưu trữ tài liệu pháp lý

### 2. Đặt lịch & sử dụng xe
- [ ] Bảng lịch chung (Calendar view) hiển thị thời gian xe trống/bận
- [ ] Chức năng đặt lịch sử dụng xe
- [ ] Hệ thống ưu tiên dựa trên tỷ lệ sở hữu
- [ ] Hệ thống ưu tiên dựa trên lịch sử sử dụng
- [ ] Thông báo khi có xung đột lịch
- [ ] Xác nhận và hủy đặt lịch

### 3. Check-in/Check-out
- [ ] Tạo QR code cho từng xe
- [ ] Quét QR code khi nhận xe (Check-in)
- [ ] Quét QR code khi trả xe (Check-out)
- [ ] Ghi nhận thời gian sử dụng thực tế
- [ ] Ký số điện tử khi nhận/trả xe
- [ ] Lưu trữ hình ảnh/xác thực khi nhận/trả xe

### 4. Quản lý dịch vụ xe
- [ ] Đặt lịch bảo dưỡng
- [ ] Đặt lịch đăng kiểm
- [ ] Đặt lịch sạc điện
- [ ] Theo dõi lịch sử dịch vụ
- [ ] Thông báo dịch vụ sắp đến hạn

### 5. Tranh chấp & giải quyết
- [ ] Tạo ticket tranh chấp
- [ ] Theo dõi trạng thái tranh chấp
- [ ] Admin/Staff can thiệp và giải quyết
- [ ] Lịch sử tranh chấp

### 6. AI & Phân tích
- [ ] Phân tích mức độ sử dụng xe
- [ ] Đề xuất lịch sử dụng công bằng
- [ ] Dự đoán chi phí
- [ ] Gợi ý tối ưu hóa chi phí

---

## 📊 TỔNG KẾT

### Đã hoàn thành: ~60%
- ✅ Quản lý nhóm và thành viên
- ✅ Hệ thống bỏ phiếu
- ✅ Quản lý chi phí và chia sẻ chi phí
- ✅ Thanh toán trực tuyến
- ✅ Quỹ chung
- ✅ Theo dõi sử dụng (km)
- ✅ Dashboard và báo cáo cơ bản

### Còn thiếu: ~40%
- ❌ Xác thực người dùng (CMND/CCCD, giấy phép lái xe)
- ❌ Hợp đồng điện tử
- ❌ Đặt lịch sử dụng xe
- ❌ Check-in/Check-out với QR code
- ❌ Quản lý dịch vụ xe
- ❌ Giải quyết tranh chấp
- ❌ AI gợi ý

---

## 🎯 KHUYẾN NGHỊ ƯU TIÊN PHÁT TRIỂN

### Ưu tiên cao (Core features)
1. **Đặt lịch sử dụng xe** - Tính năng cốt lõi cho hệ thống đồng sở hữu
2. **Check-in/Check-out với QR code** - Quan trọng cho quản lý thực tế
3. **Xác thực người dùng** - Cần thiết cho tính pháp lý

### Ưu tiên trung bình
4. **Hợp đồng điện tử** - Quan trọng về mặt pháp lý
5. **Quản lý dịch vụ xe** - Nâng cao trải nghiệm
6. **Giải quyết tranh chấp** - Cần thiết khi có vấn đề

### Ưu tiên thấp (Nice to have)
7. **AI gợi ý** - Tính năng nâng cao, có thể làm sau

---

## 📝 GHI CHÚ KỸ THUẬT

### Database cần bổ sung:
- `User` table với thông tin CMND/CCCD, giấy phép lái xe
- `LegalContract` table cho hợp đồng điện tử
- `VehicleSchedule` table cho đặt lịch
- `CheckInOut` table cho check-in/check-out
- `VehicleService` table cho dịch vụ xe
- `Dispute` table cho tranh chấp

### API cần phát triển:
- `/api/auth/register` - Đăng ký với xác thực
- `/api/schedules` - Quản lý lịch sử dụng
- `/api/checkin` - Check-in xe
- `/api/checkout` - Check-out xe
- `/api/contracts` - Quản lý hợp đồng
- `/api/services` - Quản lý dịch vụ
- `/api/disputes` - Quản lý tranh chấp

### UI cần phát triển:
- Trang đăng ký/xác thực
- Trang lịch sử dụng xe (Calendar view)
- Trang check-in/check-out với QR scanner
- Trang quản lý hợp đồng
- Trang quản lý dịch vụ
- Trang giải quyết tranh chấp

---

**Ngày tạo:** 2025-01-27
**Phiên bản:** 1.0

