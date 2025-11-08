// admin-guard.js
// QUAN TRỌNG: File này phải được chèn vào <head>

(function() {
    const token = localStorage.getItem('jwtToken');
    const userRole = localStorage.getItem('userRole');

    // Kiểm tra ngay lập tức
    if (!token || userRole !== 'ROLE_ADMIN') {

        // Hiển thị thông báo bạn yêu cầu
        alert("Bạn không có quyền truy cập trang này. Vui lòng đăng nhập với tài khoản Admin.");

        // Xóa thông tin đăng nhập (nếu có) và chuyển hướng
        localStorage.clear();
        window.location.href = '/login';

        // Dừng thực thi bất kỳ script nào khác trên trang này
        throw new Error("Access Denied: Not ROLE_ADMIN");
    }
})();