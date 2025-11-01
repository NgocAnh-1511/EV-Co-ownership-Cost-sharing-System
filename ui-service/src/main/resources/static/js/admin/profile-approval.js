// profile-approval.js

document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('jwtToken');
    const userListBody = document.getElementById('user-list-body');

    const API_BASE_URL = "http://localhost:8081/api/admin";

    // 1. Kiểm tra xác thực (Admin)
    if (!token || localStorage.getItem('userRole') !== 'ROLE_ADMIN') {
        if (typeof logout === 'function') {
            logout();
        } else {
            window.location.href = '/login';
        }
        return;
    }

    // 2. Hàm tải danh sách User đang chờ
    async function loadPendingUsers() {
        try {
            const response = await fetch(`${API_BASE_URL}/pending-users`, {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (!response.ok) {
                throw new Error('Không thể tải danh sách. Bạn có phải là Admin?');
            }

            const users = await response.json();
            renderTable(users);

        } catch (error) {
            console.error('Lỗi tải danh sách chờ:', error);
            userListBody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: red;">${error.message}</td></tr>`;
        }
    }

    // 3. Hàm hiển thị dữ liệu ra bảng
    function renderTable(users) {
        userListBody.innerHTML = ''; // Xóa dữ liệu cũ

        if (users.length === 0) {
            userListBody.innerHTML = `<tr><td colspan="6" style="text-align: center;">Không có hồ sơ nào đang chờ duyệt.</td></tr>`;
            return;
        }

        users.forEach(user => {
            const row = document.createElement('tr');
            row.setAttribute('data-user-id', user.userId); // Gán ID vào hàng

            row.innerHTML = `
                <td>${user.fullName || 'N/A'}</td>
                <td>${user.email}</td>
                <td>${user.phoneNumber || 'N/A'}</td>
                <td>${user.idCardNumber || 'N/A'}</td>
                <td>${user.profileStatus}</td>
                <td class="action-buttons">
                    <button class="btn-approve" data-action="approve">Duyệt</button>
                    <button class="btn-reject" data-action="reject">Từ chối</button>
                </td>
            `;
            userListBody.appendChild(row);
        });
    }

    // 4. Hàm xử lý hành động (Duyệt/Từ chối)
    async function handleUserAction(userId, action) {
        const url = `${API_BASE_URL}/${action}/${userId}`; // (action là 'approve' hoặc 'reject')

        try {
            const response = await fetch(url, {
                method: 'PUT',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (!response.ok) {
                throw new Error('Hành động thất bại.');
            }

            // Xóa hàng khỏi bảng sau khi hành động thành công
            const rowToRemove = userListBody.querySelector(`tr[data-user-id='${userId}']`);
            if (rowToRemove) {
                rowToRemove.remove();
            }

            // Kiểm tra xem bảng còn trống không
            if (userListBody.children.length === 0) {
                 renderTable([]);
            }

        } catch (error) {
            console.error(`Lỗi khi ${action} user ${userId}:`, error);
            alert(`Đã xảy ra lỗi: ${error.message}`);
        }
    }

    // 5. Thêm Event Listener cho các nút
    userListBody.addEventListener('click', function(event) {
        const target = event.target;

        if (target.tagName === 'BUTTON' && (target.classList.contains('btn-approve') || target.classList.contains('btn-reject'))) {
            const action = target.dataset.action;
            const userId = target.closest('tr').dataset.userId;

            if (confirm(`Bạn có chắc muốn "${action === 'approve' ? 'Duyệt' : 'Từ chối'}" hồ sơ này?`)) {
                handleUserAction(userId, action);
            }
        }
    });

    // Tải dữ liệu khi trang được mở
    loadPendingUsers();
});