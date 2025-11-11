// user-ownerships.js
document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('jwtToken');
    const CONTRACT_API_URL = "http://localhost:8081/api/contracts";
    const OWNERSHIP_API_URL = "http://localhost:8081/api/ownerships";

    const contractSelect = document.getElementById('contractSelect');
    const tableWrapper = document.getElementById('table-wrapper');
    const tableBody = document.getElementById('ownership-table-body');
    const createFormWrapper = document.getElementById('create-form-wrapper');
    const createForm = document.getElementById('createShareForm');
    const createMsg = document.getElementById('createMsg');
    const sumInfo = document.getElementById('sumInfo');

    let currentVehicleId = null;

    /**
     * 1. Tải danh sách Hợp đồng (chứa thông tin xe) vào dropdown
     */
    async function loadContracts() {
        try {
            const response = await fetch(`${CONTRACT_API_URL}/my-contracts`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (!response.ok) {
                if (response.status === 401 || response.status === 403) logout();
                throw new Error('Không thể tải danh sách hợp đồng.');
            }
            const contractsData = await response.json();

            // Lọc chỉ lấy hợp đồng ACTIVE và có thông tin xe
            const activeContracts = contractsData.filter(item =>
                item.contract.status === 'ACTIVE' && item.vehicle
            );

            if (activeContracts.length === 0) {
                contractSelect.innerHTML = '<option value="">Bạn không có hợp đồng nào đang hoạt động.</option>';
                return;
            }

            contractSelect.innerHTML = '<option value="">-- Vui lòng chọn xe từ hợp đồng --</option>';
            activeContracts.forEach(item => {
                const option = document.createElement('option');
                // Quan trọng: vehicleId là String ("VEH001")
                option.value = item.vehicle.vehicleId;
                option.textContent = `${item.contract.title} (Xe: ${item.vehicle.vehicleName} - BKS: ${item.vehicle.vehicleNumber})`;
                contractSelect.appendChild(option);
            });

        } catch (error) {
            contractSelect.innerHTML = `<option value="">${error.message}</option>`;
        }
    }

    /**
     * 2. Tải chi tiết tỷ lệ sở hữu khi chọn xe
     */
    async function loadShares(vehicleId) {
        if (!vehicleId) {
            tableWrapper.style.display = 'none';
            createFormWrapper.style.display = 'none';
            sumInfo.style.display = 'none';
            return;
        }
        currentVehicleId = vehicleId;
        tableWrapper.style.display = 'block';
        createFormWrapper.style.display = 'block';
        sumInfo.style.display = 'block';
        tableBody.innerHTML = '<tr><td colspan="7" style="text-align: center;">Đang tải...</td></tr>';

        try {
            const res = await fetch(`${OWNERSHIP_API_URL}?vehicleId=${vehicleId}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (!res.ok) throw new Error('Không tải được danh sách tỷ lệ.');

            const data = await res.json();
            tableBody.innerHTML = '';
            let sum = 0;

            if (data.length === 0) {
                 tableBody.innerHTML = '<tr><td colspan="7" style="text-align: center;">Chưa có tỷ lệ sở hữu nào được gán cho xe này.</td></tr>';
            }

            data.forEach(dto => {
                const row = dto.share;
                sum += Number(row.percentage || 0);
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${row.id}</td>
                    <td>${dto.userFullName || 'N/A'}</td>
                    <td>${row.userId}</td>
                    <td>${row.percentage.toFixed(2)}%</td>
                    <td>${row.effectiveFrom || 'N/A'}</td>
                    <td>${row.effectiveTo || 'N/A'}</td>
                    <td>
                        <button class="btn-reject" onclick="deleteShare(${row.id})" title="Xóa" style="padding: 5px 8px; font-size: 0.8rem;">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>`;
                tableBody.appendChild(tr);
            });

            // Cập nhật thông tin tổng
            const sumFixed = sum.toFixed(2);
            if (Math.abs(sum - 100) < 0.001) {
                sumInfo.textContent = `Tổng tỷ lệ: ${sumFixed}% (Đã đủ 100%)`;
                sumInfo.className = 'total-info ok';
            } else {
                sumInfo.textContent = `Tổng tỷ lệ: ${sumFixed}% (Cảnh báo: Tổng chưa đủ 100%)`;
                sumInfo.className = 'total-info warning';
            }

        } catch (error) {
            tableBody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: red;">${error.message}</td></tr>`;
        }
    }

    /**
     * 3. Xử lý thêm mới
     */
    async function handleCreateShare(e) {
        e.preventDefault();
        if (!currentVehicleId) {
            alert("Vui lòng chọn xe trước.");
            return;
        }
        createMsg.textContent = '';

        const uid = Number(document.getElementById('newUserId').value);
        const pct = Number(document.getElementById('newPercentage').value);
        const effFrom = document.getElementById('newFrom').value || null;
        const effTo = document.getElementById('newTo').value || null;

        try {
            const res = await fetch(OWNERSHIP_API_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                body: JSON.stringify({
                    vehicleId: currentVehicleId, // Gửi String vehicleId
                    userId: uid,
                    percentage: pct,
                    effectiveFrom: effFrom,
                    effectiveTo: effTo
                })
            });

            if (!res.ok) {
                const err = await res.json().catch(() => ({ error: 'Lỗi không xác định' }));
                throw new Error(err.error || 'Lỗi tạo mới');
            }

            await loadShares(currentVehicleId); // Tải lại
            createForm.reset(); // Xóa form

        } catch (e) {
            createMsg.textContent = e.message;
        }
    }

    /**
     * 4. Xử lý Xóa (đặt ở global scope để 'onclick' có thể gọi)
     */
    window.deleteShare = async function(id) {
        if (!confirm('Bạn có chắc muốn xoá tỷ lệ sở hữu này?')) return;

        try {
            const res = await fetch(`${OWNERSHIP_API_URL}/${id}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (!res.ok) {
                const err = await res.json().catch(() => ({ error: 'Lỗi xoá' }));
                throw new Error(err.error || 'Lỗi xoá');
            }

            await loadShares(currentVehicleId); // Tải lại

        } catch (e) {
            alert(e.message);
        }
    }

    // --- Gắn kết sự kiện ---
    contractSelect.addEventListener('change', (e) => loadShares(e.target.value));
    createForm.addEventListener('submit', handleCreateShare);

    // --- Tải dữ liệu ban đầu ---
    loadContracts();
});