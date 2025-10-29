document.addEventListener('DOMContentLoaded', function () {
    // Mode toggle
    document.querySelectorAll('.mode-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            document.querySelector('.mode-btn.active').classList.remove('active');
            this.classList.add('active');

            const mode = this.dataset.mode;
            const confirmBtn = document.querySelector('.btn-confirm');
            if (mode === 'checkin') {
                confirmBtn.textContent = 'Hoàn Thành Check-in';
            } else {
                confirmBtn.textContent = 'Hoàn Thành Check-out';
            }
        });
    });

    // Checklist functionality
    document.querySelectorAll('.checkbox-item input').forEach(checkbox => {
        checkbox.addEventListener('change', function () {
            this.parentElement.classList.toggle('checked', this.checked);
        });
    });

    // Confirm button
    document.querySelector('.btn-confirm').addEventListener('click', function () {
        const checkedCount = document.querySelectorAll('.checkbox-item input:checked').length;
        const totalCount = document.querySelectorAll('.checkbox-item input').length;

        if (checkedCount === totalCount) {
            alert('✅ Hoàn thành check-in/check-out thành công!');
        } else {
            alert(`⚠️ Vui lòng hoàn thành ${totalCount - checkedCount} mục checklist!`);
        }
    });

    // Cancel button
    document.querySelector('.btn-cancel').addEventListener('click', function () {
        if (confirm('Hủy thao tác này?')) {
            document.querySelector('.form-content').reset();
        }
    });

    // Notification
    document.querySelector('.notification').addEventListener('click', function () {
        alert('🔔 3 thông báo mới:\n• Toyota Camry cần check-in lúc 14:00\n• Honda Civic trễ check-out\n• BMW X5 sẵn sàng nhận');
    });

    // Auto-fill demo data
    document.querySelector('select').addEventListener('change', function () {
        if (this.value) {
            document.querySelector('textarea').value = 'Xe trong tình trạng tốt, đầy đủ giấy tờ, nhiên liệu 100%. Khách hàng đã ký biên nhận.';
        }
    });
});