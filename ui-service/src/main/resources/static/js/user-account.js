// user-account.js
// (user-guard.js đã được chèn trong <head>)

document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('jwtToken');
    const PROFILE_API_URL = "http://localhost:8081/api/users/profile";

    // NÂNG CẤP: Thêm URL API mới
    const OWNERSHIP_API_URL = "http://localhost:8081/api/ownerships/my-shares";

    // Hàm định dạng ngày tháng (VD: 2023-10-27T10:00:00Z -> 27/10/2023)
    function formatDate(dateString) {
        if (!dateString) return 'N/A';
        try {
            // Thử xử lý Timestamp (2023-10-27T10:00:00Z)
            const date = new Date(dateString);
            if (isNaN(date.getTime())) {
                // Thử xử lý LocalDate (2023-10-27)
                const parts = dateString.split('-');
                if (parts.length === 3) {
                    return `${parts[2]}/${parts[1]}/${parts[0]}`;
                }
                return dateString; // Trả về nguyên bản nếu không parse được
            }
            return date.toLocaleDateString('vi-VN');
        } catch (e) {
            return dateString; // Trả về nguyên bản nếu có lỗi
        }
    }

    // Tải dữ liệu hồ sơ cá nhân
    async function loadProfile() {
        try {
            const response = await fetch(PROFILE_API_URL, {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (!response.ok) {
                if (response.status === 401 && typeof logout === 'function') logout();
                throw new Error('Không thể tải dữ liệu hồ sơ.');
            }

            const user = await response.json();

            // Điền thông tin cá nhân
            document.getElementById('name-view').textContent = user.fullName || 'N/A';
            document.getElementById('joindate-view').textContent = formatDate(user.createdAt);
            document.getElementById('email-view').textContent = user.email || 'N/A';
            document.getElementById('phone-view').textContent = user.phoneNumber || 'N/A';
            // (Điểm tín nhiệm đang là giả lập)

            // Cập nhật thẻ Trạng thái (Status Tag)
            const statusTag = document.getElementById('status-view');
            if (user.profileStatus) {
                statusTag.classList.remove('pending', 'approved', 'rejected');
                if (user.profileStatus === 'APPROVED') {
                    statusTag.textContent = 'Đã Xác Thực';
                    statusTag.classList.add('approved');
                } else if (user.profileStatus === 'REJECTED') {
                    statusTag.textContent = 'Bị Từ Chối';
                    statusTag.classList.add('rejected');
                } else { // PENDING
                    statusTag.textContent = 'Đang Chờ Duyệt';
                    statusTag.classList.add('pending');
                }
            }

        } catch (error) {
            console.error('Lỗi tải trang Quản lý tài khoản:', error);
        }
    }

    // NÂNG CẤP: Hàm mới để tải thống kê sở hữu
    async function loadOwnershipStats() {
        try {
            const response = await fetch(OWNERSHIP_API_URL, {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (!response.ok) {
                if (response.status === 401) logout();
                throw new Error('Không thể tải thống kê sở hữu.');
            }

            const sharesData = await response.json(); // Đây là List<OwnershipShareDetailDTO>

            // Tính toán
            let totalPercentage = 0;
            const vehicleIds = new Set(); // Dùng Set để đếm số xe duy nhất

            sharesData.forEach(dto => {
                if (dto.share && dto.vehicle) {
                    totalPercentage += dto.share.percentage;
                    vehicleIds.add(dto.vehicle.vehicleId);
                }
            });

            // Cập nhật UI
            document.getElementById('widget-total-vehicles').textContent = vehicleIds.size;
            document.getElementById('widget-total-percentage').textContent = `${totalPercentage.toFixed(2)}%`;

        } catch (error) {
            console.error('Lỗi tải thống kê sở hữu:', error);
            document.getElementById('widget-total-vehicles').textContent = 'Lỗi';
            document.getElementById('widget-total-percentage').textContent = 'Lỗi';
        }
    }

    // Tải tất cả dữ liệu khi trang mở
    loadProfile();
    loadOwnershipStats(); // <-- Gọi hàm mới
});