// user-contracts.js
// (File user-guard.js đã được chèn vào <head> để bảo vệ)

document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('jwtToken');

    // --- DOM Elements ---
    const tableBody = document.getElementById('contract-table-body');
    const statsGrid = document.getElementById('stats-grid');
    const searchInput = document.getElementById('searchInput');
    const statusFilter = document.getElementById('statusFilter');
    const vehicleFilter = document.getElementById('vehicleFilter');
    const paginationInfo = document.getElementById('pagination-info');

    // --- API URL ---
    // (Trỏ đến LegalContractService trên cổng 8084 như đã tư vấn)
    const CONTRACT_API_URL = "http://localhost:8084/api/contracts";

    let allContracts = []; // Biến này sẽ lưu dữ liệu gốc từ API

    // --- 1. Hàm tải dữ liệu chính từ API ---
    async function loadContracts() {
        showLoading(tableBody);
        try {
            // QUAN TRỌNG: Endpoint này ("/my-contracts") cần được tạo trong LegalContractService
            // (Xem hướng dẫn ở dưới)
            const response = await fetch(`${CONTRACT_API_URL}/my-contracts`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    // (Hàm logout() từ auth-utils.js)
                    if (typeof logout === 'function') logout();
                    else window.location.href = '/login';
                }
                throw new Error(`Lỗi ${response.status}: Không thể tải hợp đồng.`);
            }

            allContracts = await response.json();

            // Nạp dữ liệu vào các thẻ thống kê và bảng
            loadStats(allContracts);
            // Render lần đầu với dữ liệu đầy đủ (bộ lọc mặc định là "all")
            applyFiltersAndRender();

        } catch (error) {
            console.error('Lỗi tải danh sách hợp đồng:', error);
            showError(tableBody, error.message);
        }
    }

    // --- 2. Hàm tải Thống kê (dùng dữ liệu thật) ---
    function loadStats(contracts) {
        const total = contracts.length;
        // Đảm bảo tên trường status (ví dụ: 'ACTIVE', 'PENDING') khớp với JSON từ API
        const active = contracts.filter(c => c.status === 'ACTIVE').length;
        const pending = contracts.filter(c => c.status === 'PENDING').length;
        const expired = contracts.filter(c => c.status === 'EXPIRED').length;

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
            </div>
        `;
    }

    // --- 3. Hàm Lọc và Render ---
    function applyFiltersAndRender() {
        const searchText = searchInput.value.toLowerCase();
        const statusValue = statusFilter.value;
        // const vehicleValue = vehicleFilter.value; // (Bạn có thể thêm logic lọc xe ở đây)

        let filteredData = allContracts.filter(contract => {
            // 1. Lọc theo Trạng thái
            if (statusValue && contract.status !== statusValue) {
                return false;
            }

            // 2. Lọc theo Từ khóa tìm kiếm
            // (API của bạn cần trả về các trường này, ví dụ: title, vehicleName, vehiclePlate)
            const searchFields = [
                `HĐ#${contract.id}`,
                contract.title || '',
                contract.vehicleName || '',
                contract.vehiclePlate || ''
            ];

            if (searchText && !searchFields.some(field => field.toString().toLowerCase().includes(searchText))) {
                return false;
            }

            // Nếu qua hết các bộ lọc
            return true;
        });

        // 3. Render bảng với dữ liệu đã lọc
        renderTable(filteredData);
    }

    // --- 4. Hàm Render Bảng (Hàm con) ---
    function renderTable(contracts) {
        tableBody.innerHTML = '';

        if (contracts.length === 0) {
            showError(tableBody, "Không tìm thấy hợp đồng nào khớp.");
            paginationInfo.textContent = "Hiển thị 0 của 0 kết quả";
            return;
        }

        // (Đây là logic phân trang đơn giản, bạn có thể nâng cấp sau)
        paginationInfo.textContent = `Hiển thị 1-${contracts.length} của ${contracts.length} kết quả`;

        contracts.forEach(contract => {
            const row = document.createElement('tr');

            // Đảm bảo các tên trường (status, id, title,...) khớp với JSON từ API
            let statusClass = (contract.status || 'pending').toLowerCase();
            let statusText = 'N/A';
            if (contract.status === 'ACTIVE') statusText = 'Đang hoạt động';
            else if (contract.status === 'PENDING') statusText = 'Chờ ký';
            else if (contract.status === 'EXPIRED') statusText = 'Đã kết thúc';

            // Giả định API trả về các trường:
            // id, title, vehicleName, vehiclePlate, vehicleImage,
            // startDate, duration, endDate, status
            const vehicleImg = contract.vehicleImage || 'https://via.placeholder.com/40/FF0000/FFFFFF?text=CAR';
            const vehicleName = contract.vehicleName || "Tên xe (N/A)";
            const vehiclePlate = contract.vehiclePlate || "Biển số (N/A)";
            const title = contract.title || `Hợp đồng #${contract.id}`;

            // Format ngày tháng (nếu API trả về kiểu timestamp/date string)
            const startDate = contract.startDate ? new Date(contract.startDate).toLocaleDateString('vi-VN') : 'N/A';
            const endDate = contract.endDate ? new Date(contract.endDate).toLocaleDateString('vi-VN') : 'N/A';
            const duration = contract.duration || 'N/A'; // Ví dụ: "24 tháng"

            // Tạo các nút hành động
            let actions = `
                <a href="#" title="Tải xuống"><i class="fas fa-download"></i></a>
                <a href="#" title="Chi tiết xe"><i class="fas fa-car"></i></a>
            `;
            if (contract.status === 'PENDING') {
                actions += `<a href="#" class="action-sign" data-id="${contract.id}" title="Ký hợp đồng"><i class="fas fa-edit"></i></a>`;
            } else {
                actions += `<a href="#" title="Xem chi tiết"><i class="fas fa-eye"></i></a>`;
            }
            actions += `<a href="#" title="Thêm..."><i class="fas fa-ellipsis-v"></i></a>`;

            // Đổ dữ liệu vào HTML
            row.innerHTML = `
                <td>
                    <div class="contract-id"><h4>HĐ#${contract.id}</h4><p>${title}</p></div>
                </td>
                <td>
                    <div class="vehicle-info">
                        <img src="${vehicleImg}" alt="Xe">
                        <div><span>${vehicleName}</span><p>${vehiclePlate}</p></div>
                    </div>
                </td>
                <td>${startDate}</td>
                <td>${duration}<br><small>Đến ${endDate}</small></td>
                <td><span class="status-pill ${statusClass}">${statusText}</span></td>
                <td class="action-icons">${actions}</td>
            `;
            tableBody.appendChild(row);
        });
    }

    // --- 5. Hàm xử lý Ký Hợp đồng ---
    async function signContract(contractId) {
        alert(`Đang thực hiện ký hợp đồng #${contractId}...`);

        try {
            // Cần tạo endpoint này trong LegalContractService: PUT /api/contracts/{id}/sign
            const response = await fetch(`${CONTRACT_API_URL}/${contractId}/sign`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.message || 'Ký hợp đồng thất bại.');
            }

            alert(`Hợp đồng #${contractId} đã được ký thành công!`);
            loadContracts(); // Tải lại toàn bộ dữ liệu

        } catch (error) {
            alert(`Lỗi: ${error.message}`);
        }
    }

    // --- 6. Các hàm tiện ích ---
    function showLoading(element) {
        element.innerHTML = '<tr><td colspan="6" style="text-align: center;"><i class="fas fa-spinner fa-spin"></i> Đang tải dữ liệu...</td></tr>';
    }
    function showError(element, message) {
        element.innerHTML = `<tr><td colspan="6" style="text-align: center; color: red;">${message}</td></tr>`;
    }

    // --- 7. Gắn các Event Listeners ---

    // Listeners cho thanh filter
    searchInput.addEventListener('input', applyFiltersAndRender);
    statusFilter.addEventListener('change', applyFiltersAndRender);
    vehicleFilter.addEventListener('change', applyFiltersAndRender);

    // Listener cho các nút hành động (ký, xem,...)
    tableBody.addEventListener('click', function(e) {
        const signButton = e.target.closest('.action-sign');
        if (signButton) {
            e.preventDefault();
            const contractId = signButton.dataset.id;
            if (confirm(`Bạn có chắc muốn ký hợp đồng ${contractId} không?`)) {
                signContract(contractId);
            }
        }
        // (Thêm logic cho các nút khác nếu cần, ví dụ: xem chi tiết, tải xuống)
    });

    // --- 8. Tải dữ liệu lần đầu khi trang mở ---
    loadContracts();
});