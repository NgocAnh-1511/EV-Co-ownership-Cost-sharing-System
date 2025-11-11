// auth-utils.js

/**
 * Hàm logout: Xóa token và chuyển hướng.
 */
function logout() {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('userName');
    localStorage.removeItem('userRole');
    window.location.href = '/login';
}

/**
 * Hàm kiểm tra Auth và TẢI TÊN USER VÀO HEADER
 * (Hàm này vẫn được giữ lại để các file khác có thể gọi nếu cần)
 */
function checkAuthAndLoadUser() {
    const token = localStorage.getItem("jwtToken");

    if (token) {
        const userName = localStorage.getItem("userName");
        const userNameDisplay = document.getElementById('userNameDisplay');

        if (userNameDisplay && userName && userName !== "null") {
            // Cập nhật tên nếu tìm thấy
            userNameDisplay.textContent = userName;
        }
    }
}

// --- NÂNG CẤP: TỰ ĐỘNG CHẠY KHI TẢI TRANG ---
// Bất kỳ trang nào (Admin/User) chèn file này vào
// sẽ tự động gọi hàm checkAuthAndLoadUser().
document.addEventListener('DOMContentLoaded', function() {
    checkAuthAndLoadUser();
});