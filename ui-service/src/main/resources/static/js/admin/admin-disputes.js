// admin-disputes.js

document.addEventListener('DOMContentLoaded', function() {
    // (File admin-guard.js đã bảo vệ trang này)
    // (File auth-utils.js đã tự động load tên Admin)

    const token = localStorage.getItem('jwtToken');
    const disputeListBody = document.getElementById('dispute-list-body');

    // NÂNG CẤP: API của Dispute Service (Cổng 8083)
    const API_DISPUTE_URL = "http://localhost:8083/api/disputes";

    let allDisputes = [];

    // 1. Hàm tải tất cả tranh chấp (cho Admin)
    async function loadAllDisputes() {
        try {
            const response = await fetch(`${API_DISPUTE_URL}/all`, {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (!response.ok) {
                if(response.status === 401 || response.status === 403) logout();
                throw new Error('Không thể tải danh sách tranh chấp.');
            }

            allDisputes = await response.json();
            renderTable(allDisputes);

        } catch (error) {
            console.error('Lỗi tải danh sách tranh chấp:', error);
            disputeListBody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: red;">${error.message}</td></tr>`;
        }
    }

    // 2. Hàm hiển thị dữ liệu ra bảng
    function renderTable(disputes) {
        disputeListBody.innerHTML = '';

        if (disputes.length === 0) {
            disputeListBody.innerHTML = `<tr><td colspan="6" style="text-align: center;">Không có tranh chấp nào.</td></tr>`;
            return;
        }

        disputes.forEach(dispute => {
            const row = document.createElement('tr');
            row.setAttribute('data-dispute-id', dispute.disputeId);

            row.innerHTML = `
                <td>DP#${dispute.disputeId}</td>
                <td>${dispute.subject || 'N/A'}</td>
                <td>HĐ#${dispute.contractId}</td>
                <td>User#${dispute.createdByUserId}</td>
                <td>
                    <select class="status-select" data-id="${dispute.disputeId}">
                        <option value="PENDING" ${dispute.status === 'PENDING' ? 'selected' : ''}>Mới (Pending)</option>
                        <option value="PROCESSING" ${dispute.status === 'PROCESSING' ? 'selected' : ''}>Đang xử lý</option>
                        <option value="WAITING_RESPONSE" ${dispute.status === 'WAITING_RESPONSE' ? 'selected' : ''}>Chờ phản hồi</option>
                        <option value="RESOLVED" ${dispute.status === 'RESOLVED' ? 'selected' : ''}>Đã giải quyết</option>
                        <option value="CLOSED" ${dispute.status === 'CLOSED' ? 'selected' : ''}>Đã đóng</option>
                    </select>
                </td>
                <td class="action-buttons">
                    <button class="btn-view" data-action="view" title="Xem chi tiết"><i class="fas fa-eye"></i></button>
                    <button class="btn-approve" data-action="save" title="Lưu thay đổi"><i class="fas fa-save"></i></button>
                </td>
            `;
            disputeListBody.appendChild(row);
        });
    }

    // 3. Hàm xử lý Cập nhật trạng thái
    async function handleStatusUpdate(disputeId, newStatus) {
        try {
            const response = await fetch(`${API_DISPUTE_URL}/${disputeId}/status?status=${newStatus}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                throw new Error('Cập nhật thất bại.');
            }

            alert(`Cập nhật trạng thái cho DP#${disputeId} thành công!`);
            // Tải lại danh sách
            loadAllDisputes();

        } catch (error) {
            console.error('Lỗi cập nhật trạng thái:', error);
            alert(`Đã xảy ra lỗi: ${error.message}`);
        }
    }

    // 4. Thêm Event Listener cho các nút
    disputeListBody.addEventListener('click', function(event) {
        const button = event.target.closest('button');
        if (!button) return;

        const action = button.dataset.action;
        const disputeId = button.closest('tr').dataset.disputeId;

        if (action === 'view') {
            // (Chúng ta có thể thêm Modal xem chi tiết sau, tương tự trang Duyệt Hồ Sơ)
            alert(`Xem chi tiết tranh chấp DP#${disputeId} (chưa làm)`);
        }
        else if (action === 'save') {
            // Lấy trạng thái mới từ <select>
            const selectElement = disputeListBody.querySelector(`select[data-id='${disputeId}']`);
            const newStatus = selectElement.value;

            if (confirm(`Bạn có chắc muốn đổi trạng thái của DP#${disputeId} thành "${newStatus}"?`)) {
                handleStatusUpdate(disputeId, newStatus);
            }
        }
    });

    // Tải dữ liệu khi trang được mở
    loadAllDisputes();
});