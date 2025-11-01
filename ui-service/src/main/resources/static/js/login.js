// login.js

document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const statusMessage = document.getElementById('error-message'); // Sử dụng ID error-message
    const passwordInput = document.getElementById('password');
    const LOGIN_API_URL = "http://localhost:8081/api/users/login";

    // QUAN TRỌNG: Nếu bạn dùng togglePassword, hãy đảm bảo gọi hàm đó
    function togglePassword(id) {
        // Logic togglePassword (có thể nằm trong auth-utils hoặc được định nghĩa riêng)
        const input = document.getElementById(id);
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

    // Áp dụng togglePassword cho các input password (ví dụ)
    document.querySelectorAll('.toggle-password').forEach(span => {
        span.addEventListener('click', () => togglePassword(span.previousElementSibling.id));
    });


    if (loginForm) {
        loginForm.addEventListener('submit', async function(event) {
            event.preventDefault();
            statusMessage.style.display = 'none';

            const email = document.getElementById('email').value;
            const password = passwordInput.value;

            try {
                const response = await fetch(LOGIN_API_URL, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, password })
                });

                const result = await response.json();

                if (response.ok) {
                    // Đăng nhập thành công
                    localStorage.setItem("jwtToken", result.token);
                    localStorage.setItem("userName", result.fullName);
                    localStorage.setItem("userRole", result.role); // <-- CẬP NHẬT: LƯU ROLE

                    // LOGIC ĐIỀU HƯỚNG DỰA TRÊN ROLE
                    if (result.role === 'ROLE_ADMIN') {
                        // Admin: Chuyển hướng đến Dashboard
                        window.location.href = "/admin/groups";
                    } else if (result.role === 'ROLE_USER') {
                        // User: Chuyển hướng đến Onboarding
                        window.location.href = "/user/onboarding";
                    } else {
                        // Mặc định hoặc role không xác định
                        window.location.href = "/";
                    }
                } else {
                    // Đăng nhập thất bại (Dùng result.message nếu có)
                    statusMessage.textContent = result.message || "Email hoặc mật khẩu không đúng.";
                    statusMessage.style.display = 'block';
                }
            } catch (error) {
                statusMessage.textContent = 'Lỗi kết nối đến dịch vụ: ' + error.message;
                statusMessage.style.display = 'block';
            }
        });
    }
});