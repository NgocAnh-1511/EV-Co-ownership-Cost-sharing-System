# Admin Dashboard HTML Fragments

Thư mục này chứa các file HTML fragment cho Admin Dashboard, được tách ra từ file `admin-dashboard.html` gốc để dễ quản lý và bảo trì.

## Cấu trúc file

### 1. `overview.html`
- **Mô tả**: Trang tổng quan với thống kê, biểu đồ và hoạt động gần đây
- **Controller**: `AdminOverviewController`
- **URL**: `/admin` hoặc `/admin/overview`
- **Nội dung**:
  - Time period filter (Hôm nay, Tuần này, Tháng này, v.v.)
  - Quick actions buttons
  - Stats cards (8 cards: Tổng chi phí, Đã thanh toán, Chưa thanh toán, v.v.)
  - Charts (Chi phí theo tháng, Phân loại chi phí, Bar chart, Tỷ lệ thanh toán)
  - Top lists (Top 5 chi phí cao nhất, Top 5 thành viên chưa thanh toán)
  - Recent activities
  - Custom Date Range Modal

### 2. `costs.html`
- **Mô tả**: Trang quản lý chi phí với bảng danh sách và filter
- **Controller**: `AdminCostsController`
- **URL**: `/admin/costs`
- **Nội dung**:
  - Button tạo chi phí mới
  - Filter group (Loại chi phí, Trạng thái, Tìm kiếm)
  - Data table hiển thị danh sách chi phí

### 3. `auto-split.html`
- **Mô tả**: Trang tạo chi phí và tự động chia
- **Controller**: `AdminAutoSplitController`
- **URL**: `/admin/auto-split`
- **Nội dung**:
  - Form tạo chi phí và chia tự động
  - Các phương thức chia: Theo tỷ lệ sở hữu, Theo km đã chạy, Chia đều
  - Preview result section

### 4. `payments.html`
- **Mô tả**: Trang theo dõi thanh toán
- **Controller**: `AdminPaymentsController`
- **URL**: `/admin/payments`
- **Nội dung**:
  - Stats cards (Tổng thanh toán, Đã thanh toán, Chờ thanh toán, Tổng số tiền)
  - Filter group (Trạng thái, Ngày, Tìm kiếm)
  - Data table hiển thị danh sách thanh toán

### 5. `groups.html`
- **Mô tả**: Trang quản lý nhóm
- **Controller**: `AdminGroupsController`
- **URL**: `/admin/groups`
- **Nội dung**:
  - Button tạo nhóm mới
  - Groups grid hiển thị danh sách nhóm

### 6. `funds.html`
- **Mô tả**: Trang quản lý quỹ chung
- **Controller**: `AdminFundsController`
- **URL**: `/admin/funds`
- **Nội dung**:
  - Stats cards (Tổng số dư quỹ, Tổng nạp tiền, Tổng rút tiền, Yêu cầu chờ duyệt)
  - Filter group (Nhóm, Trạng thái, Tìm kiếm)
  - Data table hiển thị danh sách quỹ
  - Pending requests section (Yêu cầu rút tiền chờ duyệt)

## Cách sử dụng trong Controller

### Ví dụ với Thymeleaf:

```java
@Controller
@RequestMapping("/admin")
public class AdminOverviewController {
    
    @GetMapping({"", "/overview"})
    public String overview(Model model) {
        // Thêm dữ liệu vào model
        model.addAttribute("totalCost", 1000000);
        // ...
        
        // Sử dụng fragment
        return "admin/overview";
    }
}
```

### Trong file template chính (admin-layout.html):

```html
<div th:replace="~{admin/overview}"></div>
```

Hoặc sử dụng với điều kiện:

```html
<div th:if="${page == 'overview'}" th:replace="~{admin/overview}"></div>
<div th:if="${page == 'costs'}" th:replace="~{admin/costs}"></div>
```

## Lưu ý

1. **Modals**: Tất cả các modals đã được tách ra thành file `fragments/admin-modals.html` riêng
2. **Sidebar**: Sidebar đã được tách ra thành file `fragments/admin-sidebar.html`
3. **Layout**: Layout chung đã được tách ra thành file `fragments/admin-layout.html`
4. **JavaScript**: Cần cập nhật các hàm JavaScript để sử dụng URL routing thay vì hash navigation
5. **CSS**: CSS chung được định nghĩa trong `admin-layout.css`

## Cập nhật JavaScript

Các hàm `switchSection()` cần được cập nhật để sử dụng URL routing:

```javascript
// Cũ
function switchSection(section) {
    // Hide all sections
    // Show selected section
}

// Mới
function switchSection(section) {
    window.location.href = '/admin/' + section;
}
```

Hoặc sử dụng AJAX để load fragment:

```javascript
function switchSection(section) {
    fetch('/admin/' + section)
        .then(response => response.text())
        .then(html => {
            document.getElementById('content-area').innerHTML = html;
        });
}
```

## Tích hợp với các Controller đã tạo

Các controller đã được tạo trong package `com.example.ui_service.controller.admin`:
- `AdminOverviewController` → `overview.html`
- `AdminCostsController` → `costs.html`
- `AdminAutoSplitController` → `auto-split.html`
- `AdminPaymentsController` → `payments.html`
- `AdminGroupsController` → `groups.html`
- `AdminFundsController` → `funds.html`

