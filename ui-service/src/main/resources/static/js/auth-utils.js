// ui-service/src/main/resources/static/js/auth-utils.js

/**
 * Hàm thực hiện đăng xuất: xóa token và chuyển hướng về trang đăng nhập.
 */
function logout() {
    // 1. Xóa JWT Token khỏi localStorage
    localStorage.removeItem('jwtToken');
    // 2. Xóa các thông tin người dùng khác nếu có
    localStorage.removeItem('userName');

    // 3. Chuyển hướng người dùng về trang đăng nhập
    // Đảm bảo đường dẫn này khớp với URL của trang Login của bạn
    window.location.href = '/login';
}

// Hàm này có thể được dùng để kiểm tra và lấy token
function getAuthToken() {
    return localStorage.getItem('jwtToken');
}