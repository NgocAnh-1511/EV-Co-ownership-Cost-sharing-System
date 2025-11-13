// Áp dụng Admin Guard để bảo vệ trang
checkAdminToken();

// Định nghĩa API URLs
const GROUP_API_URL = 'http://localhost:8082/api/vehicle-groups'; // API lấy danh sách nhóm
const REPORT_API_URL = 'http://localhost:8088/api/reports/group'; // API báo cáo

document.addEventListener('DOMContentLoaded', () => {
    const token = getToken(); // Lấy token từ auth-utils
    if (!token) {
        console.error("Không tìm thấy token admin.");
        return;
    }

    // Tải danh sách nhóm vào dropdown
    loadGroups(token);

    // Thêm sự kiện cho nút "Xem Báo cáo"
    document.getElementById('load-report-btn').addEventListener('click', () => {
        const selectedGroupId = document.getElementById('group-select').value;
        if (selectedGroupId) {
            fetchReportData(selectedGroupId, token);
        } else {
            alert('Vui lòng chọn một nhóm.');
        }
    });
});

/**
 * Tải danh sách nhóm từ VehicleService (Cổng 8082)
 */
async function loadGroups(token) {
    try {
        const response = await fetch(GROUP_API_URL, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`Lỗi khi tải danh sách nhóm: ${response.status}`);
        }

        const groups = await response.json();
        const groupSelect = document.getElementById('group-select');

        groups.forEach(group => {
            const option = document.createElement('option');
            option.value = group.groupId;
            // Hiển thị tên nhóm và ID nhóm
            option.textContent = `${group.groupName} (ID: ${group.groupId})`;
            groupSelect.appendChild(option);
        });

    } catch (error) {
        console.error('Lỗi tải nhóm:', error);
        alert('Không thể tải danh sách nhóm. Dịch vụ xe (8082) có đang chạy không?');
    }
}

/**
 * Gọi API (Cổng 8088) để lấy dữ liệu báo cáo cho nhóm đã chọn
 */
async function fetchReportData(groupId, token) {
    const reportContentEl = document.getElementById('report-content');
    reportContentEl.classList.add('hidden'); // Ẩn báo cáo cũ
    showLoading(true);

    try {
        const response = await fetch(`${REPORT_API_URL}/${groupId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`, // Gửi JWT token
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorData = await response.text();
            throw new Error(`Lỗi khi tải báo cáo: ${response.status} - ${errorData}`);
        }

        const reportData = await response.json();

        // Điền dữ liệu vào trang
        populateReport(reportData);
        reportContentEl.classList.remove('hidden'); // Hiển thị báo cáo mới

    } catch (error) {
        console.error('Lỗi nghiêm trọng khi fetch báo cáo:', error);
        alert(`Không thể tải báo cáo. Dịch vụ báo cáo (8088) có đang chạy không?\nLỗi: ${error.message}`);
    } finally {
        showLoading(false);
    }
}

/**
 * Hiển thị/ẩn trạng thái tải
 */
function showLoading(isLoading) {
    const button = document.getElementById('load-report-btn');
    if (isLoading) {
        button.textContent = 'Đang tải...';
        button.disabled = true;
    } else {
        button.textContent = 'Xem Báo cáo';
        button.disabled = false;
    }
}

/**
 * Điền dữ liệu từ API vào các phần của trang
 */
function populateReport(data) {
    // Header
    document.getElementById('group-name').textContent = data.groupInfo?.groupName || 'N/A';
    document.getElementById('vehicle-info').textContent = `${data.vehicleInfo?.vehicleName || 'Xe'} - ${data.vehicleInfo?.vehicleNumber || 'N/A'}`;
    document.getElementById('report-date').textContent = new Date(data.reportGeneratedAt).toLocaleString('vi-VN');

    // Quỹ
    const balance = data.groupFund?.balance || 0;
    document.getElementById('fund-balance').textContent = `${balance.toLocaleString('vi-VN')} VND`;

    // Tỷ lệ
    populateTable('ownership-body', data.ownershipShares, (share) => `
        <tr>
            <td>${share.userId}</td>
            <td>${share.sharePercentage}%</td>
        </tr>
    `);

    // Chi phí
    populateTable('costs-body', data.costs, (cost) => `
        <tr>
            <td>${cost.costId}</td>
            <td>${cost.costType}</td>
            <td>${cost.description}</td>
            <td>${(cost.amount || 0).toLocaleString('vi-VN')} VND</td>
            <td>${cost.createdAt ? new Date(cost.createdAt).toLocaleDateString('vi-VN') : 'N/A'}</td>
        </tr>
    `);

    // Chi phí đã chia
    populateTable('cost-shares-body', data.costShares, (share) => `
        <tr>
            <td>${share.costId}</td>
            <td>${share.userId}</td>
            <td>${(share.amount || 0).toLocaleString('vi-VN')} VND</td>
            <td>${share.status || 'N/A'}</td>
        </tr>
    `);

    // Thanh toán
    populateTable('payments-body', data.payments, (payment) => `
        <tr>
            <td>${payment.paymentId}</td>
            <td>${payment.userId}</td>
            <td>${(payment.amount || 0).toLocaleString('vi-VN')} VND</td>
            <td>${payment.status || 'N/A'}</td>
            <td>${payment.paymentDate ? new Date(payment.paymentDate).toLocaleString('vi-VN') : 'N/A'}</td>
        </tr>
    `);

    // Giao dịch quỹ
    populateTable('transactions-body', data.fundTransactions, (tx) => `
        <tr>
            <td>${tx.transactionId}</td>
            <td>${tx.userId || 'N/A'}</td>
            <td>${tx.type || 'N/A'}</td>
            <td>${(tx.amount || 0).toLocaleString('vi-VN')} VND</td>
            <td>${tx.status || 'N/A'}</td>
            <td>${tx.createdAt ? new Date(tx.createdAt).toLocaleString('vi-VN') : 'N/A'}</td>
        </tr>
    `);

    // Sử dụng
    populateTable('usage-body', data.usageTrackings, (usage) => `
        <tr>
            <td>${usage.userId}</td>
            <td>${usage.startTime ? new Date(usage.startTime).toLocaleString('vi-VN') : 'N/A'}</td>
            <td>${usage.endTime ? new Date(usage.endTime).toLocaleString('vi-VN') : 'N/A'}</td>
            <td>${usage.distanceKm || 0} km</td>
        </tr>
    `);
}

/**
 * Hàm tiện ích để điền dữ liệu vào bảng
 */
function populateTable(bodyId, dataArray, rowTemplate) {
    const tableBody = document.getElementById(bodyId);
    if (!tableBody) return;

    if (!dataArray || dataArray.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="100%">Không có dữ liệu.</td></tr>';
        return;
    }

    tableBody.innerHTML = dataArray.map(rowTemplate).join('');
}