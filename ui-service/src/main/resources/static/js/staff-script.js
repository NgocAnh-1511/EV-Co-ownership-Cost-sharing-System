document.addEventListener('DOMContentLoaded', function () {
    // Modal Thêm nhân viên
    document.querySelector('.btn-add-staff').addEventListener('click', function () {
        alert('Mở modal thêm nhân viên mới!');
        // Mở modal thêm nhân viên ở đây nếu có
    });

    // Chức năng lọc
    document.querySelector('.btn-filter').addEventListener('click', function () {
        alert('Áp dụng bộ lọc!');
        // Thực hiện bộ lọc khi người dùng nhấn nút lọc
        const groupFilter = document.querySelector('.filter-group select').value;  // Lấy giá trị lọc nhóm
        const statusFilter = document.querySelector('.filter-group select:nth-child(4)').value;  // Lấy giá trị lọc trạng thái

        // Có thể thêm logic để lọc danh sách nhóm ở đây
        console.log(`Lọc nhóm: ${groupFilter}, Trạng thái: ${statusFilter}`);
    });

    // Các nút hành động cho từng nhóm nhân viên (xem, quản lý thành viên, quản lý xe, xóa nhóm)
    document.querySelectorAll('.btn-action').forEach(btn => {
        btn.addEventListener('click', function (e) {
            const icon = this.querySelector('i').classList[1];  // Lấy tên icon để xác định hành động

            switch (icon) {
                case 'fa-eye':
                    alert('Xem chi tiết nhóm!');
                    // Thực hiện mở chi tiết nhóm nếu cần
                    break;
                case 'fa-users':
                    alert('Quản lý thành viên!');
                    // Thực hiện mở trang quản lý thành viên của nhóm
                    break;
                case 'fa-car':
                    alert('Quản lý xe!');
                    // Thực hiện mở trang quản lý xe của nhóm
                    break;
                case 'fa-trash':
                    if (confirm('Bạn có chắc chắn muốn xóa nhóm này?')) {
                        // Thực hiện xóa nhóm
                        this.closest('.staff-card').style.opacity = '0.5';  // Làm mờ nhóm để cho thấy nhóm đang bị xóa
                        this.closest('.staff-card').style.pointerEvents = 'none';  // Tắt khả năng tương tác với nhóm
                    }
                    break;
            }
        });
    });

    // Chức năng phân trang
    document.querySelectorAll('.pagination-buttons button:not(.prev):not(.next)').forEach(btn => {
        btn.addEventListener('click', function () {
            document.querySelector('.pagination-buttons .active').classList.remove('active');
            this.classList.add('active');
            // Thực hiện chuyển trang hoặc tải lại dữ liệu ở đây (nếu cần)
        });
    });

    // Chức năng tìm kiếm
    document.querySelector('.filter-group input').addEventListener('input', function () {
        const searchTerm = this.value.toLowerCase();  // Lấy từ khóa tìm kiếm và chuyển về chữ thường
        document.querySelectorAll('.staff-card').forEach(card => {
            const name = card.querySelector('.staff-name').textContent.toLowerCase();  // Lấy tên nhóm
            card.style.display = name.includes(searchTerm) ? 'block' : 'none';  // Hiển thị nhóm nếu tên nhóm chứa từ khóa tìm kiếm
        });
    });
});
