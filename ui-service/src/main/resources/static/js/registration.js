// registration.js

// Hàm chuyển đổi ẩn/hiện mật khẩu (Đã cập nhật để phù hợp với HTML mới)
function togglePassword(id) {
    const input = document.getElementById(id);
    // Lấy icon (i tag) nằm trong span.toggle-password
    const icon = input.nextElementSibling.querySelector('i');

    if (input.type === "password") {
        input.type = "text";
        icon.classList.remove("fa-eye");
        icon.classList.add("fa-eye-slash");
    } else {
        input.type = "password";
        icon.classList.remove("fa-eye-slash");
        icon.classList.add("fa-eye");
    }
}

// Bắt sự kiện submit của form Đăng Ký
document.addEventListener("DOMContentLoaded", function() {
    const registerForm = document.getElementById("registerForm");
    if (!registerForm) return; // Bảo vệ nếu form không tồn tại

    registerForm.addEventListener("submit", async function(event) {
        event.preventDefault(); // Ngăn form submit theo cách truyền thống

        const fullName = document.getElementById("fullName").value;
        const email = document.getElementById("email").value;
        const password = document.getElementById("password").value;
        const confirmPassword = document.getElementById("confirmPassword").value;
        const errorMessageDiv = document.getElementById("error-message");

        errorMessageDiv.style.display = "none"; // Ẩn thông báo lỗi cũ

        // 1. Kiểm tra mật khẩu trùng khớp (phía client)
        if (password !== confirmPassword) {
            errorMessageDiv.textContent = "Lỗi: Mật khẩu xác nhận không trùng khớp!";
            errorMessageDiv.style.display = "block";
            return;
        }

        // 2. Chuẩn bị dữ liệu gửi đi
        const data = {
            fullName: fullName,
            email: email,
            password: password
        };

        // 3. Gọi API backend (user-account-service)
        const API_URL = "http://localhost:8081/api/users/register";

        try {
            const response = await fetch(API_URL, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(data)
            });

            if (response.ok) {
                // Đăng ký thành công
                alert("Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
                window.location.href = "/login";
            } else {
                // Xử lý lỗi từ server (ví dụ: email đã tồn tại)
                // Lấy nội dung lỗi từ server và hiển thị
                const errorText = await response.text();
                errorMessageDiv.textContent = errorText;
                errorMessageDiv.style.display = "block";
            }
        } catch (error) {
            // Lỗi mạng hoặc server không chạy
            errorMessageDiv.textContent = "Không thể kết nối đến máy chủ. Vui lòng thử lại sau.";
            errorMessageDiv.style.display = "block";
        }
    });
});