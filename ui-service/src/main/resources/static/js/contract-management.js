document.addEventListener('DOMContentLoaded', function () {
    // Search functionality
    const searchInput = document.querySelector('.filter-group input');
    searchInput.addEventListener('input', function () {
        const searchTerm = this.value.toLowerCase();
        document.querySelectorAll('.contract-table tbody tr').forEach(row => {
            const contractId = row.cells[0].textContent.toLowerCase();
            const vehicle = row.cells[1].textContent.toLowerCase();
            row.style.display = (contractId.includes(searchTerm) || vehicle.includes(searchTerm)) ? '' : 'none';
        });
    });

    // Status filter
    document.querySelectorAll('.filter-group select')[0].addEventListener('change', function () {
        const status = this.value;
        document.querySelectorAll('.contract-table tbody tr').forEach(row => {
            if (status === 'Tất cả trạng thái' || row.querySelector('.status').textContent === status) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });
    });

    // Group filter
    document.querySelectorAll('.filter-group select')[1].addEventListener('change', function () {
        const group = this.value;
        document.querySelectorAll('.contract-table tbody tr').forEach(row => {
            if (group === 'Tất cả nhóm' || row.cells[1].textContent.includes(group)) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });
    });

    // Action buttons
    document.querySelectorAll('.btn-view').forEach(btn => {
        btn.addEventListener('click', function () {
            const contractId = this.closest('tr').cells[0].textContent;
            alert(`📄 Xem chi tiết hợp đồng: ${contractId}`);
        });
    });

    document.querySelectorAll('.btn-sign').forEach(btn => {
        btn.addEventListener('click', function () {
            const contractId = this.closest('tr').cells[0].textContent;
            if (confirm(`Ký hợp đồng ${contractId}?`)) {
                this.closest('tr').querySelector('.status').textContent = 'Đã hoàn thành';
                this.closest('tr').querySelector('.status').className = 'status status-completed';
                this.style.display = 'none';
                alert('✅ Đã ký hợp đồng thành công!');
            }
        });
    });

    document.querySelectorAll('.btn-download').forEach(btn => {
        btn.addEventListener('click', function () {
            const contractId = this.closest('tr').cells[0].textContent;
            alert(`⬇️ Đang tải hợp đồng: ${contractId}.pdf`);
        });
    });

    // Create contract button
    document.querySelector('.btn-create-contract').addEventListener('click', function () {
        alert('📝 Mở modal tạo hợp đồng mới!');
    });

    // Export Excel
    document.querySelector('.btn-export').addEventListener('click', function () {
        alert('📊 Đang xuất file Excel...');
    });

    // Pagination
    document.querySelectorAll('.pagination-buttons button:not(.prev):not(.next)').forEach(btn => {
        btn.addEventListener('click', function () {
            document.querySelector('.pagination-buttons .active').classList.remove('active');
            this.classList.add('active');
        });
    });

    // Notification
    document.querySelector('.notification').addEventListener('click', function () {
        alert('🔔 3 hợp đồng đang chờ ký:\n• #CON-2024-001: Tesla Group A\n• #CON-2024-003: Nissan Leaf\n• #CON-2024-004: BMW i3');
    });
});