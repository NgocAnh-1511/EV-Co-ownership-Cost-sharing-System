document.addEventListener('DOMContentLoaded', function () {
    console.log('Staff script loaded');
    
    const editModal = document.getElementById('editGroupModal');
    const editForm = document.getElementById('editGroupForm');
    const closeModal = document.querySelector('.close-modal');
    const cancelBtn = document.getElementById('cancelEditBtn');
    const updateStatusMessage = document.getElementById('updateStatusMessage');

    // Kiểm tra xem các element có tồn tại không
    if (!editModal) {
        console.error('Modal không tìm thấy!');
        return;
    }

    console.log('Modal found:', editModal);
    console.log('Edit buttons found:', document.querySelectorAll('.btn-edit-group').length);

    // Mở modal sửa khi click nút Sửa
    const editButtons = document.querySelectorAll('.btn-edit-group');
    console.log('Số lượng nút Sửa:', editButtons.length);
    
    editButtons.forEach((btn, index) => {
        console.log(`Đăng ký event cho nút Sửa ${index + 1}`);
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
            console.log('Nút Sửa được click!');
            
            const groupId = this.getAttribute('data-group-id');
            const groupName = this.getAttribute('data-group-name');
            const vehicleCount = this.getAttribute('data-vehicle-count');
            const active = this.getAttribute('data-active');
            const description = this.getAttribute('data-description') || '';
            
            console.log('Dữ liệu nhóm xe:', { groupId, groupName, vehicleCount, active, description });
            
            // Điền dữ liệu vào form
            const editGroupId = document.getElementById('editGroupId');
            const editGroupName = document.getElementById('editGroupName');
            const editVehicleCount = document.getElementById('editVehicleCount');
            const editActive = document.getElementById('editActive');
            const editDescription = document.getElementById('editDescription');
            
            if (editGroupId) editGroupId.value = groupId || '';
            if (editGroupName) editGroupName.value = groupName || '';
            if (editVehicleCount) editVehicleCount.value = vehicleCount || 0;
            if (editActive) editActive.value = active || 'active';
            if (editDescription) editDescription.value = description || '';
            
            // Mở modal
            if (editModal) {
                editModal.classList.add('show');
                editModal.style.display = 'block';
                console.log('Modal đã được mở');
            } else {
                console.error('Không thể mở modal - editModal không tồn tại');
            }
        });
    });

    // Hàm đóng modal
    function closeEditModal() {
        if (editModal) {
            editModal.classList.remove('show');
            editModal.style.display = 'none';
        }
    }

    // Đóng modal khi click nút X hoặc Hủy
    if (closeModal) {
        closeModal.addEventListener('click', function () {
            closeEditModal();
        });
    }

    if (cancelBtn) {
        cancelBtn.addEventListener('click', function () {
            closeEditModal();
        });
    }

    // Đóng modal khi click bên ngoài modal
    if (editModal) {
        window.addEventListener('click', function (event) {
            if (event.target === editModal) {
                closeEditModal();
            }
        });
    }

    // Xử lý submit form sửa
    if (editForm) {
        editForm.addEventListener('submit', function (e) {
            e.preventDefault();
            
            const groupId = document.getElementById('editGroupId').value;
            const groupData = {
                name: document.getElementById('editGroupName').value.trim(),
                vehicleCount: parseInt(document.getElementById('editVehicleCount').value) || 0,
                active: document.getElementById('editActive').value,
                description: document.getElementById('editDescription').value.trim()
            };

            // Gọi API để cập nhật nhóm xe
            fetch(`http://localhost:8083/api/vehicle-groups/${groupId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(groupData)
            })
            .then(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    return response.text().then(text => {
                        throw new Error(text);
                    });
                }
            })
            .then(data => {
                // Hiển thị thông báo thành công
                showUpdateMessage('Nhóm xe đã được cập nhật thành công!', 'success');
                // Đóng modal
                if (editModal) {
                    editModal.classList.remove('show');
                    editModal.style.display = 'none';
                }
                // Reload trang sau 1 giây
                setTimeout(() => {
                    window.location.reload();
                }, 1500);
            })
            .catch(error => {
                // Hiển thị thông báo lỗi
                showUpdateMessage('Lỗi khi cập nhật nhóm xe: ' + error.message, 'error');
        });
    });
    }

    // Hàm hiển thị thông báo
    function showUpdateMessage(message, type) {
        updateStatusMessage.textContent = message;
        updateStatusMessage.className = type === 'success' ? 'alert alert-success' : 'alert alert-danger';
        updateStatusMessage.style.display = 'block';
        
        // Tự động ẩn sau 5 giây
        setTimeout(() => {
            updateStatusMessage.style.display = 'none';
        }, 5000);
    }
});
