// user-contracts.js
<<<<<<< HEAD
=======
// PHIÊN BẢN NÂNG CẤP HOÀN CHỈNH - CHỈ DÙNG PORT 8085

>>>>>>> 17c2e87 (Lưu tạm thay đổi trước khi pull)
document.addEventListener('DOMContentLoaded', function() {
    if (typeof checkAuthAndLoadUser === 'function') {
        checkAuthAndLoadUser();
    }
    const token = localStorage.getItem('jwtToken');
    const CONTRACT_API_URL = "http://localhost:8081/api/contracts";

<<<<<<< HEAD
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
=======
    // NÂNG CẤP: Cả hai API đều trỏ đến Port 8085
    const API_BASE_URL = 'http://localhost:8085'; // Service chính của bạn

    // DOM Elements
    const statsGrid = document.getElementById('stats-grid');
    const tableBody = document.getElementById('contract-table-body');
    const statusMessage = document.getElementById('status-message');
    const tabs = document.querySelectorAll('.tab-item');
    const searchInput = document.getElementById('searchInput');
>>>>>>> 17c2e87 (Lưu tạm thay đổi trước khi pull)

    let allContracts = [];
    let currentStatusFilter = 'all';

<<<<<<< HEAD
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
=======
    // --- DOM Elements cho Modal 3 Bước ---
    const createModal = document.getElementById('createContractModal');
    const createButton = document.getElementById('btn-create-contract');
    const createContractForm = document.getElementById('createContractForm');
    const closeButtons = document.querySelectorAll('.modal-close');

    // Step 1
    const step1 = document.getElementById('modal-step-1');
    const vehicleListContainer = document.getElementById('vehicle-selection-list');
    const vehicleListLoading = document.getElementById('vehicle-list-loading');
    const btnStep1Next = document.getElementById('btn-step1-next');
    const step1ErrorMessage = document.getElementById('step1-error-message');

    // Step 2
    const step2 = document.getElementById('modal-step-2');
    const btnStep2Back = document.getElementById('btn-step2-back');
    const btnStep2Next = document.getElementById('btn-step2-next');
    const modalErrorMessage = document.getElementById('modal-error-message');

    // Step 3
    const step3 = document.getElementById('modal-step-3');
    const btnStep3Back = document.getElementById('btn-step3-back');
    const summaryInfo = document.getElementById('summary-info');

    let selectedVehicleInfo = null;

    // 1. Hàm tải dữ liệu HỢP ĐỒNG (Port 8085)
    async function loadData() {
        if (!token) { /* (Giữ nguyên) */ }
        try {
            tableBody.innerHTML = '<tr><td colspan="6" style="text-align: center;">Đang tải dữ liệu hợp đồng...</td></tr>';

            // NÂNG CẤP: Gọi API hợp đồng trên Port 8085
            const response = await fetch(`${API_BASE_URL}/api/legalcontracts/my-contracts`, {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (!response.ok) { /* (Giữ nguyên) */ }
            allContracts = await response.json();
            loadStats();
            renderTable();
        } catch (error) { /* (Giữ nguyên) */ }
    }

    // 2. Hàm tải Thống kê (Dùng dữ liệu thật)
    function loadStats() {
        if (!statsGrid) return;

        const total = allContracts.length;
        const active = allContracts.filter(c => c.contractStatus === 'signed').length;
        const pending = allContracts.filter(c => c.contractStatus === 'pending' || c.contractStatus === 'draft').length;
        const expired = allContracts.filter(c => c.contractStatus === 'archived' || c.contractStatus === 'expired').length;
>>>>>>> 17c2e87 (Lưu tạm thay đổi trước khi pull)

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
                <div class="stat-info"><h3>Chờ ký/Dự thảo</h3><span>${pending}</span></div>
            </div>
            <div class="stat-card">
                <div class="stat-icon gray"><i class="fas fa-times-circle"></i></div>
                <div class="stat-info"><h3>Đã kết thúc</h3><span>${expired}</span></div>
            </div>`;
    }

<<<<<<< HEAD
    function renderTable(data) {
        // (Không thay đổi)
        tableBody.innerHTML = '';
        if (data.length === 0) {
=======
    // 3. Hàm render Bảng (Dùng dữ liệu thật)
    function renderTable() {
        tableBody.innerHTML = '';

        const searchQuery = searchInput.value.toLowerCase();

        // Lọc dữ liệu
        const filteredData = allContracts.filter(contract => {
            const matchesStatus = (currentStatusFilter === 'all') ||
                                  (currentStatusFilter === 'pending' && (contract.contractStatus === 'pending' || contract.contractStatus === 'draft')) ||
                                  (currentStatusFilter === 'signed' && contract.contractStatus === 'signed') ||
                                  (currentStatusFilter === 'archived' && (contract.contractStatus === 'archived' || contract.contractStatus === 'expired'));
            if (!matchesStatus) return false;

            // NÂNG CẤP: Tìm kiếm theo vehicleId
            const matchesSearch = !searchQuery ||
                                (contract.contractId && contract.contractId.toLowerCase().includes(searchQuery)) ||
                                (contract.vehicle && contract.vehicle.vehicleId && contract.vehicle.vehicleId.toLowerCase().includes(searchQuery)) ||
                                (contract.vehicle && contract.vehicle.vehicleNumber && contract.vehicle.vehicleNumber.toLowerCase().includes(searchQuery)) ||
                                (contract.contractType && contract.contractType.toLowerCase().includes(searchQuery));

            return matchesSearch;
        });

        if (filteredData.length === 0) {
>>>>>>> 17c2e87 (Lưu tạm thay đổi trước khi pull)
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

<<<<<<< HEAD
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
=======
            let statusClass = (contract.contractStatus || 'draft').toLowerCase();
            let statusText = getStatusText(contract.contractStatus);
            let formattedDate = contract.creationDate ? new Date(contract.creationDate).toLocaleDateString('vi-VN') : 'N/A';
            let contractType = contract.contractType || 'N/A';

            // NÂNG CẤP: Lấy thông tin xe từ object lồng nhau
            const vehicleName = (contract.vehicle && contract.vehicle.vehicleName) ? contract.vehicle.vehicleName : 'Xe (N/A)';
            const vehiclePlate = (contract.vehicle && contract.vehicle.vehicleNumber) ? contract.vehicle.vehicleNumber : 'N/A';

            let actions = `
                <a href="#" class="action-icon" title="Tải xuống PDF"><i class="fas fa-download"></i></a>
            `;

            if (contract.contractStatus === 'pending' || contract.contractStatus === 'draft') {
                actions += `<a href="#" class="action-icon action-sign" data-id="${contract.contractId}" title="Ký hợp đồng"><i class="fas fa-edit"></i></a>`;
            } else {
                actions += `<a href="#" class="action-icon" title="Xem chi tiết"><i class="fas fa-eye"></i></a>`;
            }

            row.innerHTML = `
                <td>
                    <div class="contract-id">
                        <h4>${contract.contractId}</h4>
                        <p>${contract.contractCode || 'HĐ Đồng sở hữu'}</p>
                    </div>
                </td>
                <td>
                    <div class="vehicle-info">
                        <img src="https://via.placeholder.com/40/0000FF/FFFFFF?text=VF" alt="Xe">
                        <div>
                            <span>${vehicleName}</span>
                            <p>${vehiclePlate}</p>
                        </div>
                    </div>
                </td>
                <td>${formattedDate}</td>
                <td>${contractType}</td>
                <td><span class="status-pill status-${statusClass}">${statusText}</span></td>
                <td class="action-icons">${actions}</td>
>>>>>>> 17c2e87 (Lưu tạm thay đổi trước khi pull)
            `;
            tableBody.appendChild(row);
        });
    }

<<<<<<< HEAD
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
=======
    // 4. Hàm Ký hợp đồng (Gọi API thật - Port 8085)
    async function signContract(contractId) {
        if (!token) { /* (Giữ nguyên) */ }
        showStatusMessage('Đang ký hợp đồng...', 'success');
        try {
            // NÂNG CẤP: Gọi API hợp đồng trên Port 8085
            const response = await fetch(`${API_BASE_URL}/api/legalcontracts/sign/${contractId}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Ký hợp đồng thất bại.');
            }
            showStatusMessage(`Hợp đồng #${contractId} đã được ký thành công!`, 'success');
            loadData();
        } catch (error) {
            console.error('Lỗi khi ký hợp đồng:', error);
            showStatusMessage(error.message, 'error');
        }
    }

    // 5. Hàm helper dịch trạng thái
    function getStatusText(status) { /* (Giữ nguyên) */ }

    // 6. Hàm helper hiển thị thông báo
    function showStatusMessage(message, type = 'success') { /* (Giữ nguyên) */ }


    // --- CÁC HÀM MỚI CHO MODAL TẠO HỢP ĐỒNG ---

    // (MỚI) Hàm 7: Tải danh sách xe từ Port 8085
    async function loadAvailableVehicles() {
        if (!token) return;

        vehicleListContainer.innerHTML = '';
        vehicleListLoading.textContent = 'Đang tải danh sách xe...';
        vehicleListLoading.style.display = 'block';
        step1ErrorMessage.textContent = '';
        selectedVehicleInfo = null;
        btnStep1Next.disabled = true;

        try {
            // Gọi API Port 8085
            const response = await fetch(`${API_BASE_URL}/api/vehicles`, {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (!response.ok) throw new Error('Không thể tải danh sách xe. (Lỗi ' + response.status + ')');

            const vehicles = await response.json();

            // Lọc các xe CÓ THỂ TẠO HỢP ĐỒNG (ví dụ: status = 'available')
            const availableVehicles = vehicles.filter(v => v.status === 'available');

            renderVehicleList(availableVehicles);

        } catch (error) {
            console.error('Lỗi tải xe:', error);
            vehicleListLoading.innerHTML = `<span style="color: red;">${error.message}</span>`;
        }
    }

    // (MỚI) Hàm 8: Vẽ danh sách xe ra Step 1
    function renderVehicleList(vehicles) {
        vehicleListContainer.innerHTML = '';
        if (vehicles.length === 0) {
            vehicleListLoading.textContent = 'Không có xe nào khả dụng (status="available") để tạo hợp đồng.';
            vehicleListLoading.style.color = '#6B7280';
            return;
        }

        vehicleListLoading.style.display = 'none';

        vehicles.forEach(vehicle => {
            const card = document.createElement('div');
            card.className = 'vehicle-card';

            // Lấy thông tin xe
            const vehicleId = vehicle.vehicleId || 'N/A';
            const vehicleName = vehicle.vehicleName || vehicle.vehicleType || 'Không tên';
            const licensePlate = vehicle.vehicleNumber || 'Chưa có BKS';

            card.dataset.vehicleId = vehicleId;
            card.dataset.vehicleName = vehicleName;
            card.dataset.licensePlate = licensePlate;

            card.innerHTML = `
                <img src="${vehicle.imageUrl || 'https://via.placeholder.com/150/EEEEEE/AAAAAA?text=No+Image'}" alt="${vehicleName}">
                <div class="vehicle-card-info">
                    <h4>${vehicleName}</h4>
                    <p>Biển số: <strong>${licensePlate}</strong></p>
                </div>
                <button type="button" class="btn-select-vehicle">Chọn</button>
            `;
            vehicleListContainer.appendChild(card);
        });
    }

    // (MỚI) Hàm 9: Xử lý khi click "Chọn" xe
    vehicleListContainer.addEventListener('click', function(e) {
        if (e.target.classList.contains('btn-select-vehicle')) {
            document.querySelectorAll('.vehicle-card').forEach(c => c.classList.remove('selected'));

            const selectedCard = e.target.closest('.vehicle-card');
            selectedCard.classList.add('selected');

            // Lưu thông tin xe đã chọn
            selectedVehicleInfo = {
                vehicleId: selectedCard.dataset.vehicleId,
                vehicleName: selectedCard.dataset.vehicleName,
                licensePlate: selectedCard.dataset.licensePlate
            };

            btnStep1Next.disabled = false;
            step1ErrorMessage.textContent = '';
        }
    });

    // (MỚI) Hàm 10: Điều hướng các bước
    function navigateToStep(stepNumber) {
        document.querySelectorAll('.modal-step').forEach(s => s.style.display = 'none');
        document.getElementById(`modal-step-${stepNumber}`).style.display = 'block';
    }

    btnStep1Next.addEventListener('click', () => {
        if (!selectedVehicleInfo) {
            step1ErrorMessage.textContent = 'Vui lòng chọn một xe.';
            return;
        }
        navigateToStep(2);
    });

    btnStep2Back.addEventListener('click', () => navigateToStep(1));

    btnStep2Next.addEventListener('click', () => {
        // Cập nhật tóm tắt ở bước 3
        summaryInfo.innerHTML = `
            <p><strong>Xe đã chọn:</strong> ${selectedVehicleInfo.vehicleName} (BKS: ${selectedVehicleInfo.licensePlate})</p>
            <p><strong>Mã Xe (Vehicle ID):</strong> ${selectedVehicleInfo.vehicleId}</p>
            <p><strong>Loại Hợp đồng:</strong> ${document.getElementById('contractType').value}</p>
            <p><strong>Mô tả:</strong> ${document.getElementById('contractDescription').value || '(Không có)'}</p>
        `;
        navigateToStep(3);
    });

    btnStep3Back.addEventListener('click', () => navigateToStep(2));

    // (CẬP NHẬT) Hàm 11: Mở/Đóng Modal
    function openCreateModal() {
        if (createModal) {
            createContractForm.reset();
            modalErrorMessage.style.display = 'none';
            selectedVehicleInfo = null;
            navigateToStep(1);
            createModal.style.display = 'flex';
            loadAvailableVehicles(); // Tải xe khi mở modal
        }
    }

    function closeCreateModal() {
        if (createModal) {
            createModal.style.display = 'none';
        }
    }

    if (createButton) {
        createButton.addEventListener('click', openCreateModal);
    }
    closeButtons.forEach(btn => btn.addEventListener('click', closeCreateModal));
    if (createModal) {
        createModal.addEventListener('click', (e) => {
            if (e.target === createModal) closeCreateModal();
        });
    }

    // (CẬP NHẬT) Xử lý sự kiện Submit form (Ở bước 3)
    if (createContractForm) {
        createContractForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            if (!token) { if (typeof logout === 'function') logout(); return; }
            if (!selectedVehicleInfo || !selectedVehicleInfo.vehicleId) {
                modalErrorMessage.textContent = 'Lỗi: Không có thông tin xe. Vui lòng quay lại bước 1.';
                modalErrorMessage.style.display = 'block';
                navigateToStep(2);
                return;
            }

            const contractType = document.getElementById('contractType').value;
            const contractCode = document.getElementById('contractCode').value;
            const description = document.getElementById('contractDescription').value;

            // NÂNG CẤP: Gửi vehicleId thay vì groupId
            const contractData = {
                vehicleId: selectedVehicleInfo.vehicleId, // <-- THAY ĐỔI QUAN TRỌNG
                contractType: contractType,
                contractCode: contractCode || null,
                description: description || null
            };

            const submitButton = createContractForm.querySelector('button[type="submit"]');
            submitButton.disabled = true;
            submitButton.textContent = 'Đang tạo...';

            try {
                // Gọi API TẠO HỢP ĐỒNG (Port 8085)
                const response = await fetch(`${API_BASE_URL}/api/legalcontracts/create`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(contractData)
                });

                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || 'Tạo hợp đồng thất bại.');
                }

                showStatusMessage('Tạo hợp đồng thành công! Hợp đồng mới ở trạng thái "Dự thảo".', 'success');
                closeCreateModal();
                loadData(); // Tải lại danh sách hợp đồng

            } catch (error) {
                console.error('Lỗi khi tạo hợp đồng:', error);
                modalErrorMessage.textContent = error.message;
                modalErrorMessage.style.display = 'block';
                navigateToStep(2);
            } finally {
                submitButton.disabled = false;
                submitButton.textContent = 'Xác nhận Tạo';
            }
        });
    }

    // (CẬP NHẬT) Gán sự kiện cho các chức năng cũ (Filter, Search, Ký)
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            currentStatusFilter = tab.dataset.status;
            renderTable();
        });
    });

    searchInput.addEventListener('input', renderTable);

>>>>>>> 17c2e87 (Lưu tạm thay đổi trước khi pull)
    tableBody.addEventListener('click', function(e) {
        const signButton = e.target.closest('.action-sign');
        if (signButton) {
            e.preventDefault();
            const contractId = signButton.dataset.id;
<<<<<<< HEAD
            handleSignContract(contractId); // Gọi hàm ký mới
=======
            if (confirm(`Bạn có chắc muốn ký hợp đồng ${contractId} không?\n(Hành động này sẽ chuyển hợp đồng sang trạng thái "Đã ký")`)) {
                signContract(contractId);
            }
>>>>>>> 17c2e87 (Lưu tạm thay đổi trước khi pull)
        }

        // (Bạn có thể thêm logic cho các nút khác ở đây)
    });

<<<<<<< HEAD
    // --- TẢI DỮ LIỆU BAN ĐẦU ---
    loadContracts();
=======
    // --- Tải dữ liệu ban đầu ---
    loadData();
>>>>>>> 17c2e87 (Lưu tạm thay đổi trước khi pull)
});