// user-contracts.js
document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('jwtToken');
    const CONTRACT_API_URL = "http://localhost:8081/api/contracts";

    const tableBody = document.getElementById('contract-table-body');
    const statsGrid = document.getElementById('stats-grid');
    const statusFilterSelect = document.getElementById('statusFilter');
    const vehicleFilterSelect = document.getElementById('vehicleFilter');

    // --- Biến cho Modal ---
    const createContractModal = document.getElementById('createContractModal');
    const createContractButton = document.querySelector('.btn-create-contract');
    const modalCloseButton = document.getElementById('modalCloseButton');
    const modalCancelButton = document.getElementById('modalCancelButton');
    const createContractForm = document.getElementById('createContractForm');
    const vehicleSelect = document.getElementById('vehicleSelect');
    const durationSelect = document.getElementById('durationSelect');
    const modalError = document.getElementById('modal-error-message');
    // -------------------------

    let allContracts = [];

    async function loadContracts() {
        try {
            const response = await fetch(`${CONTRACT_API_URL}/my-contracts`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (!response.ok) {
                if (response.status === 401 || response.status === 403) logout();
                throw new Error('Không thể tải hợp đồng.');
            }
            allContracts = await response.json();
            populateVehicleFilter(allContracts);
            updateDashboard(allContracts);
        } catch (error) {
            console.error('Lỗi tải hợp đồng:', error);
            tableBody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: red;">${error.message}</td></tr>`;
        }
    }

    function populateVehicleFilter(data) {
        const vehicles = new Map();
        data.forEach(item => {
            if (item.vehicle) {
                vehicles.set(item.vehicle.vehicleId, item.vehicle.vehicleName || 'Xe không tên');
            }
        });
        vehicleFilterSelect.innerHTML = '<option value="">Tất cả loại xe</option>';
        vehicles.forEach((name, id) => {
            const option = document.createElement('option');
            option.value = id;
            option.textContent = name;
            vehicleFilterSelect.appendChild(option);
        });
    }

    function updateDashboard(data) {
        const statusFilter = statusFilterSelect.value;
        const vehicleFilter = vehicleFilterSelect.value;
        const filteredData = data.filter(item => {
            const statusMatch = statusFilter ? item.contract.status === statusFilter : true;
            const vehicleMatch = vehicleFilter ? (item.vehicle && item.vehicle.vehicleId === vehicleFilter) : true;
            return statusMatch && vehicleMatch;
        });
        renderStats(data);
        renderTable(filteredData);
    }

    function renderStats(data) {
        // (Không thay đổi)
        const total = data.length;
        const active = data.filter(c => c.contract.status === 'ACTIVE').length;
        const pending = data.filter(c => c.contract.status === 'PENDING').length;
        const expired = data.filter(c => c.contract.status === 'EXPIRED').length;
        statsGrid.innerHTML = `
            <div class="stat-card">
                <div class="stat-icon blue"><i class="fas fa-file-alt"></i></div>
                <div class="stat-info"><h3>Tổng hợp đồng</h3><span>${total}</span></div>
            </div>
            <div class="stat-card">
                <div class="stat-icon green"><i class="fas fa-check-circle"></i></div>
                <div class="stat-info"><h3>Đang hoạt động</h3><span>${active}</span></div>
            </div>
            <div class="stat-card">
                <div class="stat-icon orange"><i class="fas fa-clock"></i></div>
                <div class="stat-info"><h3>Chờ ký</h3><span>${pending}</span></div>
            </div>
            <div class="stat-card">
                <div class="stat-icon gray"><i class="fas fa-times-circle"></i></div>
                <div class="stat-info"><h3>Đã kết thúc</h3><span>${expired}</span></div>
            </div>`;
    }

    function renderTable(data) {
        // (Không thay đổi)
        tableBody.innerHTML = '';
        if (data.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="6" style="text-align: center;">Không tìm thấy hợp đồng nào.</td></tr>';
            return;
        }
        data.forEach(item => {
            const contract = item.contract;
            const vehicle = item.vehicle;
            const row = document.createElement('tr');
            let statusClass = (contract.status || 'pending').toLowerCase();
            let statusText = 'N/A';
            if (contract.status === 'ACTIVE') statusText = 'Đang hoạt động';
            else if (contract.status === 'PENDING') statusText = 'Chờ ký';
            else if (contract.status === 'EXPIRED') statusText = 'Đã kết thúc';

            const vehicleName = vehicle ? (vehicle.vehicleName || 'Xe không rõ') : 'Không có dữ liệu xe';
            const vehiclePlate = vehicle ? (vehicle.vehicleNumber || 'N/A') : 'N/A';
            const vehicleImage = 'https://via.placeholder.com/40/808080/FFFFFF?text=CAR';

            row.innerHTML = `
                <td>
                    <div class="contract-id"><h4>HĐ#${contract.contractId}</h4><p>${contract.title}</p></div>
                </td>
                <td>
                    <div class="vehicle-info">
                        <img src="${vehicleImage}" alt="Xe">
                        <div><span>${vehicleName}</span><p>${vehiclePlate}</p></div>
                    </div>
                </td>
                <td>${contract.signDate || 'N/A'}</td>
                <td>${contract.expiryDate ? 'Đến ' + contract.expiryDate : 'N/A'}</td>
                <td><span class="status-pill ${statusClass}">${statusText}</span></td>
                <td class="action-icons">
                     <a href="#" title="Tải xuống"><i class="fas fa-download"></i></a>
                     <a href="#" title="Chi tiết xe"><i class="fas fa-car"></i></a>
                     ${contract.status === 'PENDING' ?
                        `<a href="#" class="action-sign" data-id="${contract.contractId}" title="Ký hợp đồng"><i class="fas fa-edit"></i></a>` :
                        `<a href="#" title="Xem chi tiết"><i class="fas fa-eye"></i></a>`}
                </td>
            `;
            tableBody.appendChild(row);
        });
    }

    // --- LOGIC CHO MODAL TẠO MỚI ---
    // (Toàn bộ logic modal không thay đổi)

    async function openCreateContractModal() {
        modalError.style.display = 'none';
        vehicleSelect.innerHTML = '<option value="">Đang tải danh sách xe...</option>';
        createContractModal.style.display = 'flex';

        try {
            const response = await fetch(`${CONTRACT_API_URL}/available-vehicles`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (!response.ok) throw new Error('Không thể tải danh sách xe. (Cổng 8082 có đang chạy không?)');

            const vehicles = await response.json();

            const contractedVehicleIds = new Set(allContracts.map(item => item.contract.vehicleId));
            const availableVehicles = vehicles.filter(v =>
                !contractedVehicleIds.has(v.vehicleId) &&
                (v.status ? v.status.toUpperCase() === 'ACTIVE' : true)
            );

            if (availableVehicles.length === 0) {
                vehicleSelect.innerHTML = '<option value="">Không tìm thấy xe nào có sẵn</option>';
                return;
            }

            vehicleSelect.innerHTML = '<option value="">-- Vui lòng chọn xe --</option>';
            availableVehicles.forEach(v => {
                const option = document.createElement('option');
                option.value = v.vehicleId;
                option.textContent = `${v.vehicleName || 'Xe không tên'} (BKS: ${v.vehicleNumber || 'N/A'})`;
                vehicleSelect.appendChild(option);
            });

        } catch (error) {
            vehicleSelect.innerHTML = `<option value="">${error.message}</option>`;
        }
    }

    function closeCreateContractModal() {
        createContractModal.style.display = 'none';
    }

    async function handleCreateContractSubmit(e) {
        e.preventDefault();
        modalError.style.display = 'none';
        const vehicleId = vehicleSelect.value;
        const durationMonths = durationSelect.value;
        if (!vehicleId) {
            modalError.textContent = 'Vui lòng chọn một xe.';
            modalError.style.display = 'block';
            return;
        }
        try {
            const formData = new URLSearchParams();
            formData.append('vehicleId', vehicleId);
            formData.append('durationMonths', durationMonths);
            const response = await fetch(CONTRACT_API_URL, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: formData
            });
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || 'Tạo hợp đồng thất bại.');
            }
            alert('Tạo hợp đồng mới thành công!');
            closeCreateContractModal();
            loadContracts();
        } catch (error) {
            modalError.textContent = `Lỗi: ${error.message}`;
            modalError.style.display = 'block';
        }
    }

    // --- HÀM MỚI ĐỂ XỬ LÝ KÝ HỢP ĐỒNG ---
    async function handleSignContract(contractId) {
        if (!confirm(`Bạn có chắc muốn ký hợp đồng HĐ#${contractId} không?`)) {
            return;
        }

        try {
            const response = await fetch(`${CONTRACT_API_URL}/${contractId}/sign`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.ok) {
                alert('Ký hợp đồng thành công!');
                loadContracts(); // Tải lại danh sách để cập nhật trạng thái
            } else {
                const errorText = await response.text();
                throw new Error(errorText || 'Ký hợp đồng thất bại.');
            }
        } catch (error) {
            alert(`Lỗi: ${error.message}`);
        }
    }

    // --- GẮN KẾT SỰ KIỆN ---

    createContractButton.addEventListener('click', openCreateContractModal);
    modalCloseButton.addEventListener('click', closeCreateContractModal);
    modalCancelButton.addEventListener('click', closeCreateContractModal);
    createContractForm.addEventListener('submit', handleCreateContractSubmit);
    statusFilterSelect.addEventListener('change', () => updateDashboard(allContracts));
    vehicleFilterSelect.addEventListener('change', () => updateDashboard(allContracts));

    // --- CẬP NHẬT EVENT LISTENER CHO BẢNG ---
    tableBody.addEventListener('click', function(e) {
        const signButton = e.target.closest('.action-sign');
        if (signButton) {
            e.preventDefault();
            const contractId = signButton.dataset.id;
            handleSignContract(contractId); // Gọi hàm ký mới
        }

        // (Bạn có thể thêm logic cho các nút khác ở đây)
    });

    // --- TẢI DỮ LIỆU BAN ĐẦU ---
    loadContracts();
});