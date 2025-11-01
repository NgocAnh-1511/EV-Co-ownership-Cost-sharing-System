document.addEventListener('DOMContentLoaded', function () {
    // Search functionality
    const searchInput = document.querySelector('.filter-group input');
    searchInput.addEventListener('input', function () {
        const searchTerm = this.value.toLowerCase();
        document.querySelectorAll('.vehicle-table tbody tr').forEach(row => {
            const vehicleName = row.querySelector('.vehicle-name').textContent.toLowerCase();
            row.style.display = vehicleName.includes(searchTerm) ? '' : 'none';
        });
    });

    // Add vehicle button
    document.querySelector('.btn-add-vehicle').addEventListener('click', function () {
        alert('Mở modal thêm xe mới!');
    });

    // Status filter
    document.querySelector('.filter-group select').addEventListener('change', function () {
        const status = this.value;
        document.querySelectorAll('.vehicle-table tbody tr').forEach(row => {
            if (status === 'Tất cả trạng thái' || row.querySelector('.status').textContent === status) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });
    });

    // Action buttons
    document.querySelectorAll('.btn-edit').forEach(btn => {
        btn.addEventListener('click', function () {
            const vehicleName = this.closest('tr').querySelector('.vehicle-name').textContent;
            alert(`Sửa thông tin xe: ${vehicleName}`);
        });
    });

    document.querySelectorAll('.btn-delete').forEach(btn => {
        btn.addEventListener('click', function () {
            if (confirm('Bạn có chắc muốn xóa xe này?')) {
                this.closest('tr').style.opacity = '0.5';
                this.closest('tr').style.pointerEvents = 'none';
            }
        });
    });

    // Export Excel
    document.querySelector('.btn-export').addEventListener('click', function () {
        alert('Đang xuất file Excel...');
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
        alert('3 thông báo mới!\n• Toyota Camry sẵn sàng\n• Ford Transit bảo dưỡng xong\n• Mazda CX-5 cần sửa chữa');
    });
});