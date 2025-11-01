// profile-status.js

document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('jwtToken');
    const PROFILE_API_URL = "http://localhost:8081/api/users/profile";

    // 1. Kiểm tra xác thực (Giống onboarding)
    if (!token) {
        if (typeof logout === 'function') {
            logout();
        } else {
            window.location.href = '/login';
        }
        return;
    }

    // Hàm hiển thị ảnh (nếu là URL) hoặc văn bản (nếu chưa có)
    function displayImage(elementId, url) {
        const element = document.getElementById(elementId);
        if (url) {
            element.innerHTML = `<img src="${url}" alt="Giấy tờ đã tải lên">`;
        } else {
            element.textContent = "Chưa tải lên";
        }
    }

    // 2. Tải dữ liệu hồ sơ
    async function loadProfileStatus() {
        try {
            const response = await fetch(PROFILE_API_URL, {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (!response.ok) {
                if (response.status === 401) logout();
                throw new Error('Không thể tải dữ liệu hồ sơ.');
            }

            const user = await response.json();

            // 3. Cập nhật thẻ Trạng thái (Status Tag)
            const statusTag = document.getElementById('status-tag');
            if (user.profileStatus) {
                statusTag.classList.remove('pending', 'approved', 'rejected');

                if (user.profileStatus === 'APPROVED') {
                    statusTag.textContent = 'Đã duyệt';
                    statusTag.classList.add('approved');
                } else if (user.profileStatus === 'REJECTED') {
                    statusTag.textContent = 'Bị từ chối';
                    statusTag.classList.add('rejected');
                } else { // PENDING
                    statusTag.textContent = 'Đang chờ duyệt';
                    statusTag.classList.add('pending');
                }
            }

            // 4. Điền dữ liệu vào các thẻ span (read-only)
            document.getElementById('fullName-view').textContent = user.fullName || 'N/A';
            document.getElementById('phoneNumber-view').textContent = user.phoneNumber || 'N/A';
            document.getElementById('email-view').textContent = user.email || 'N/A';
            document.getElementById('dateOfBirth-view').textContent = user.dateOfBirth || 'N/A';

            document.getElementById('idCardNumber-view').textContent = user.idCardNumber || 'N/A';
            document.getElementById('idCardIssueDate-view').textContent = user.idCardIssueDate || 'N/A';
            document.getElementById('idCardIssuePlace-view').textContent = user.idCardIssuePlace || 'N/A';

            document.getElementById('licenseNumber-view').textContent = user.licenseNumber || 'N/A';
            document.getElementById('licenseClass-view').textContent = user.licenseClass || 'N/A';
            document.getElementById('licenseIssueDate-view').textContent = user.licenseIssueDate || 'N/A';
            document.getElementById('licenseExpiryDate-view').textContent = user.licenseExpiryDate || 'N/A';

            // 5. Hiển thị ảnh (nếu có URL)
            displayImage('idCardFrontUrl-view', user.idCardFrontUrl);
            displayImage('idCardBackUrl-view', user.idCardBackUrl);
            displayImage('licenseImageUrl-view', user.licenseImageUrl);
            displayImage('portraitImageUrl-view', user.portraitImageUrl);

        } catch (error) {
            console.error('Lỗi tải trang Tình trạng Hồ sơ:', error);
        }
    }

    // 6. Tải tên người dùng (từ auth-utils.js)
    if (typeof checkAuthAndLoadUser === 'function') {
        checkAuthAndLoadUser();
    }

    // Tải dữ liệu chính
    loadProfileStatus();
});