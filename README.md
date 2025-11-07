<head>: Chèn user-guard.js (hoặc admin-guard.js).
Cuối <body>: Chèn auth-utils.js (sẽ tự động cập nhật Header) 
và file JS riêng của trang đó (page-specific.js).


# Dự án: EV Co-ownership & Cost-sharing System (Hệ thống Đồng sở hữu Xe điện)

Đây là dự án microservice (sử dụng Spring Boot) để quản lý việc đồng sở hữu và chia sẻ chi phí xe điện.

## 1. 📋 Yêu cầu Môi trường (Prerequisites)

Trước khi bắt đầu, hãy đảm bảo bạn đã cài đặt các công cụ sau:

* **Java (JDK):** Bắt buộc sử dụng **JDK 17** hoặc **JDK 21**. (Dự án sẽ thất bại nếu dùng JDK 25+).
* **Maven:** 3.8+ (Để build dự án).
* **IDE:** IntelliJ IDEA (Khuyến nghị) hoặc Eclipse/VS Code.
* **Database:** MySQL Server 8.0+.
* **Git:** Để clone dự án.

## 2. ⚙️ Cài đặt Môi trường (Setup)

Đây là các bước cài đặt một lần trước khi chạy dự án.

### A. Cài đặt Cơ sở dữ liệu (MySQL)

Dự án này yêu cầu một database MySQL tên là `CoOwnershipDB`.

1.  Mở MySQL Workbench (hoặc terminal) và chạy lệnh sau để tạo database:
    ```sql
    CREATE DATABASE IF NOT EXISTS CoOwnershipDB
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    ```
2.  (Không bắt buộc) Tạo bảng: `UserService` sử dụng `spring.jpa.hibernate.ddl-auto=update`, nên nó sẽ tự động tạo các bảng (như `Users`) khi khởi động.

### B. Cấu hình Database Connection

Module `user-account-service` cần biết mật khẩu database của bạn.

1.  Đi đến file: `user-account-service/src/main/resources/application.properties`
2.  Tìm và sửa các dòng sau, thay thế `your_username` và `your_password` bằng thông tin đăng nhập MySQL của bạn (thường là `root` và mật khẩu của bạn):

    ```properties
    spring.datasource.username=your_username
    spring.datasource.password=your_password
    ```

### C. Tạo Thư mục Upload File

Chức năng upload ảnh (Giấy tờ tùy thân, GPLX) yêu cầu một thư mục tên là `uploads` ở thư mục gốc của dự án.

* Tại thư mục gốc của dự án (ngang hàng với file `pom.xml` chính), hãy **tạo một thư mục mới** tên là `uploads`.

## 3. 🚀 Chạy Dự án (Running the Application)

Đây là một dự án đa module (multi-module) không sử dụng Service Discovery (Eureka), vì vậy **THỨ TỰ KHỞI ĐỘNG RẤT QUAN TRỌNG.**

Bạn phải chạy các module Backend (API) trước, sau đó mới chạy Frontend (UI).

### Thứ tự Khởi động:

Bạn có thể chạy các file Application chính (`...Application.java`) trực tiếp từ IDE (IntelliJ):

1.  **Chạy Backend (User):**
    * **File:** `user-account-service/src/main/java/.../UserAccountServiceApplication.java`
    * **Cổng (Port):** 8081

2.  **Chạy Backend (Financial):**
    * **File:** `financial-reporting-service/src/main/java/.../FinancialReportingServiceApplication.java`
    * **Cổng (Port):** 8082

3.  **Chạy Backend (Dispute):**
    * **File:** `dispute-management-service/src/main/java/.../DisputeManagementServiceApplication.java`
    * **Cổng (Port):** 8083

4.  **Chạy Frontend (UI):**
    * **File:** `ui-service/src/main/java/.../UiServiceApplication.java`
    * **Cổng (Port):** 8080

> **Lưu ý quan trọng:** Bạn phải chạy các service ở cổng 8081, 8082, 8083 **TRƯỚC** khi chạy `ui-service` (cổng 8080), vì `ui-service` đã được cấu hình cứng (hard-code) để gọi đến các địa chỉ đó.

## 4. 🧪 Sử dụng và Kiểm tra

Sau khi tất cả các service đã chạy:

* **Truy cập Giao diện:** Mở trình duyệt và đi đến: `http://localhost:8080/`
* **Trang Đăng nhập:** `http://localhost:8080/login`
* **Trang Đăng ký:** `http://localhost:8080/register`

### Logic Phân quyền:

* **Tài khoản User (ROLE_USER):**
    * Đăng ký tài khoản (ví dụ: `user@example.com`).
    * Sau khi đăng nhập, bạn sẽ được chuyển đến trang: `http://localhost:8080/user/onboarding`
    * Bạn cũng có thể xem trạng thái hồ sơ tại: `http://localhost:8080/user/profile-status`

* **Tài khoản Admin (ROLE_ADMIN):**
    * Đăng ký tài khoản có email chứa `@admin.com` (ví dụ: `admin@admin.com`).
    * Sau khi đăng nhập, bạn sẽ được chuyển đến trang: `http://localhost:8080/admin/groups` (Trang Admin Dashboard)
    * Bạn có thể duyệt hồ sơ User tại: `http://localhost:8080/admin/profile-approval`