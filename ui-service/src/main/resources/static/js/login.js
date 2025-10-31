// login.js

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

// Bắt sự kiện submit của form Đăng Nhập
document.addEventListener("DOMContentLoaded", function() {
    const loginForm = document.getElementById("loginForm");
    if (!loginForm) return; // Bảo vệ nếu form không tồn tại

    loginForm.addEventListener("submit", async function(event) {
        event.preventDefault(); // Ngăn form submit

        const email = document.getElementById("email").value;
        const password = document.getElementById("password").value;
        const errorMessageDiv = document.getElementById("error-message");

        errorMessageDiv.style.display = "none"; // Ẩn thông báo lỗi cũ

        const data = {
            email: email,
            password: password
        };

        // URL API đăng nhập backend
        const API_URL = "http://localhost:8081/api/users/login";

        try {
            const response = await fetch(API_URL, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(data)
            });

            if (response.ok) {
                // Đăng nhập thành công
                const result = await response.json(); // Lấy response (chứa token)

                // Lưu JWT token vào localStorage để dùng cho các request sau
                localStorage.setItem("jwtToken", result.token);
                localStorage.setItem("userName", result.fullName);

                // Chuyển hướng đến trang chủ (ví dụ: "/")
                window.location.href = "/"; // (Bạn cần tạo trang chủ này)
            } else {
                // Sai email hoặc mật khẩu (401 Unauthorized từ backend)
                const errorText = await response.text();
                errorMessageDiv.textContent = errorText;
                errorMessageDiv.style.display = "block";
            }
        } catch (error) {
            // Lỗi mạng
            errorMessageDiv.textContent = "Không thể kết nối đến máy chủ. Vui lòng thử lại sau.";
            errorMessageDiv.style.display = "block";
        }
    });
});