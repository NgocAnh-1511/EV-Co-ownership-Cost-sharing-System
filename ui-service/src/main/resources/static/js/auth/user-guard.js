// user-guard.js
// QUAN TRỌNG: File này phải được chèn vào <head>

(function() {
    const token = localStorage.getItem('jwtToken');
    const userRole = localStorage.getItem('userRole');

    // Kiểm tra ngay lập tức
    if (!token || userRole !== 'ROLE_USER') {

        alert("Bạn không có quyền truy cập trang này. Vui lòng đăng nhập với tài khoản User.");

        // Xóa thông tin đăng nhập (nếu có) và chuyển hướng
        localStorage.clear();
        window.location.href = '/login';

        throw new Error("Access Denied: Not ROLE_USER");
    }
})();