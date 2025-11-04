document.addEventListener('DOMContentLoaded', function () {
    console.log('Staff script loaded');
    
    const editModal = document.getElementById('editGroupModal');
    const editForm = document.getElementById('editGroupForm');
    const closeModal = editModal?.querySelector('.close-modal');
    const cancelBtn = document.getElementById('cancelEditBtn');
    const updateStatusMessage = document.getElementById('updateStatusMessage');

    // Kiểm tra xem các element có tồn tại không
    if (!editModal) {
        console.error('Modal không tìm thấy!');
        return;
    }

    console.log('Modal found:', editModal);
    console.log('Edit buttons found:', document.querySelectorAll('.btn-edit-group').length);

    // Biến lưu số lượng xe hiện tại khi mở modal
    let currentVehicleCountInModal = 0;
    let currentGroupIdInModal = '';

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
            
            // Lưu số lượng xe hiện tại và groupId
            currentVehicleCountInModal = parseInt(vehicleCount) || 0;
            currentGroupIdInModal = groupId || '';
            console.log('🔹 Lưu số lượng xe hiện tại:', currentVehicleCountInModal);
            console.log('🔹 Lưu groupId:', currentGroupIdInModal);
            
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
            
            if (editModal) {
                editModal.classList.add('show');
                editModal.style.display = 'block';
                console.log('Modal đã được mở');
                
                // Lấy số lượng xe thực tế trong nhóm và tính số lượng xe cần thêm
                if (groupId) {
                    fetch(`http://localhost:8083/api/vehicle-groups/${groupId}/vehicles`)
                        .then(response => response.json())
                        .then(vehiclesList => {
                            const actualVehicleCount = vehiclesList ? vehiclesList.length : 0;
                            console.log('🔹 Số lượng xe thực tế trong nhóm:', actualVehicleCount);
                            
                            // Tính số lượng xe cần thêm = số lượng nhập - số lượng thực tế
                            const inputCount = parseInt(vehicleCount) || 0;
                            const vehiclesToAdd = Math.max(0, inputCount - actualVehicleCount);
                            
                            console.log('🔹 Số lượng xe cần thêm:', vehiclesToAdd);
                            
                            // Chỉ hiện form khi cần thêm xe
                            if (vehiclesToAdd > 0 && window.generateVehicleRows) {
                                window.generateVehicleRows(vehiclesToAdd);
                            } else {
                                const container = document.getElementById('vehiclesToAddContainer');
                                if (container) {
                                    container.innerHTML = '<p style="color: #6B7280; font-size: 14px; text-align: center; padding: 20px; margin: 0;">Nhóm xe đã có đủ số lượng xe. Nhập số lượng lớn hơn để thêm xe mới.</p>';
                                }
                            }
                        })
                        .catch(error => {
                            console.error('❌ Lỗi khi lấy danh sách xe:', error);
                            // Nếu lỗi, vẫn hiện form theo số lượng trong attribute
                            const initialCount = parseInt(vehicleCount) || 0;
                            if (window.generateVehicleRows && initialCount > 0) {
                                window.generateVehicleRows(initialCount);
                            }
                        });
                }
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
            const groupName = document.getElementById('editGroupName').value.trim();
            const vehicleCount = parseInt(document.getElementById('editVehicleCount').value) || 0;
            const active = document.getElementById('editActive').value;
            const description = document.getElementById('editDescription').value.trim();
            
            // Kiểm tra nếu số lượng xe giảm xuống
            const vehiclesNeeded = vehicleCount - currentVehicleCountInModal;
            
            console.log('🔹 Kiểm tra trước khi submit:');
            console.log('  - Số lượng xe hiện tại (khi mở modal):', currentVehicleCountInModal);
            console.log('  - Số lượng xe mới:', vehicleCount);
            console.log('  - Số lượng xe cần thay đổi:', vehiclesNeeded);
            
            // Nếu số lượng xe mới < số lượng xe hiện tại, cần chọn xe để xóa
            if (vehiclesNeeded < 0) {
                const vehiclesToDelete = Math.abs(vehiclesNeeded);
                console.log('🔹 Cần xóa ' + vehiclesToDelete + ' xe');
                
                // Đóng modal chỉnh sửa nhóm xe
                if (editModal) {
                    editModal.classList.remove('show');
                    editModal.style.display = 'none';
                    console.log('✅ Đã đóng modal chỉnh sửa nhóm xe');
                }
                
                // Lấy danh sách xe hiện tại trong nhóm
                fetch(`http://localhost:8083/api/vehicle-groups/${groupId}/vehicles`)
                    .then(response => response.json())
                    .then(vehiclesList => {
                        console.log('🔹 Danh sách xe trong nhóm:', vehiclesList);
                        
                        // Mở modal chọn xe xóa
                        const deleteVehiclesModal = document.getElementById('deleteVehiclesModal');
                        const deleteVehiclesContainer = document.getElementById('deleteVehiclesContainer');
                        const deleteVehiclesGroupId = document.getElementById('deleteVehiclesGroupId');
                        const deleteVehiclesCount = document.getElementById('deleteVehiclesCount');
                        const vehiclesToDeleteCount = document.getElementById('vehiclesToDeleteCount');
                        
                        if (deleteVehiclesModal && deleteVehiclesContainer && deleteVehiclesGroupId && deleteVehiclesCount && vehiclesToDeleteCount) {
                            // Lưu thông tin vào modal (bao gồm cả thông tin nhóm xe để cập nhật sau khi xóa)
                            deleteVehiclesGroupId.value = groupId;
                            deleteVehiclesCount.value = vehiclesToDelete;
                            vehiclesToDeleteCount.textContent = vehiclesToDelete;
                            
                            // Lưu thông tin nhóm xe vào data attributes để sử dụng sau khi xóa
                            deleteVehiclesModal.setAttribute('data-group-name', groupName);
                            deleteVehiclesModal.setAttribute('data-group-active', active);
                            deleteVehiclesModal.setAttribute('data-group-description', description);
                            deleteVehiclesModal.setAttribute('data-new-vehicle-count', vehicleCount);
                            
                            // Tạo danh sách checkbox để chọn xe xóa
                            deleteVehiclesContainer.innerHTML = '';
                            
                            vehiclesList.forEach(function(vehicle) {
                                const vehicleId = vehicle.vehicleId || vehicle.vehicle_id;
                                const vehicleNumber = vehicle.vehicleNumber || vehicle.vehicle_number || '';
                                const vehicleType = vehicle.vehicleType || vehicle.vehicle_type || '';
                                const status = vehicle.status || '';
                                
                                const row = document.createElement('div');
                                row.style.cssText = 'display: flex; align-items: center; padding: 12px; margin-bottom: 10px; background: #F9FAFB; border-radius: 8px; border: 1px solid #E5E7EB;';
                                row.innerHTML = `
                                    <input type="checkbox" class="vehicle-delete-checkbox" value="${vehicleId}" style="margin-right: 15px; width: 20px; height: 20px; cursor: pointer;">
                                    <div style="flex: 1;">
                                        <div style="font-weight: 600; color: #111827; margin-bottom: 4px;">${vehicleNumber || vehicleId}</div>
                                        <div style="font-size: 14px; color: #6B7280;">${vehicleType || 'N/A'} - ${status || 'N/A'}</div>
                                    </div>
                                `;
                                deleteVehiclesContainer.appendChild(row);
                            });
                            
                            // Mở modal
                            deleteVehiclesModal.classList.add('show');
                            deleteVehiclesModal.style.display = 'block';
                            deleteVehiclesModal.style.visibility = 'visible';
                            deleteVehiclesModal.style.opacity = '1';
                            deleteVehiclesModal.style.zIndex = '1000';
                            
                            console.log('✅ Đã mở modal chọn xe xóa với ' + vehiclesList.length + ' xe');
                        } else {
                            console.error('❌ Không tìm thấy modal xóa xe hoặc các elements liên quan');
                            alert('Lỗi: Không tìm thấy modal xóa xe. Vui lòng làm mới trang và thử lại.');
                        }
                    })
                    .catch(error => {
                        console.error('❌ Lỗi khi lấy danh sách xe:', error);
                        alert('Lỗi khi lấy danh sách xe: ' + error.message);
                    });
                
                return false; // Không submit form, đợi user chọn xe xóa
            }
            
            // Nếu số lượng xe tăng lên, cần thêm xe mới
            if (vehiclesNeeded > 0) {
                console.log('🔹 Cần thêm ' + vehiclesNeeded + ' xe mới');
                
                // Thu thập thông tin xe từ các form trong modal edit
                const vehicleRows = document.querySelectorAll('#vehiclesToAddContainer .vehicle-row');
                const vehicles = [];
                vehicleRows.forEach(function(row) {
                    const vehicleId = row.querySelector('.vehicle-id-input')?.value.trim();
                    const vehicleType = row.querySelector('.vehicle-type-input')?.value.trim();
                    const vehicleNumber = row.querySelector('.vehicle-number-input')?.value.trim();
                    const status = row.querySelector('.vehicle-status-input')?.value;
                    
                    if (vehicleId && vehicleType && vehicleNumber) {
                        vehicles.push({
                            vehicleId: vehicleId,
                            type: vehicleType,
                            vehicleNumber: vehicleNumber,
                            status: status || 'available'
                        });
                    }
                });
                
                // Nếu chưa có đủ xe trong form, đóng modal edit và mở modal thêm xe
                if (vehicles.length < vehiclesNeeded) {
                    const remainingVehicles = vehiclesNeeded - vehicles.length;
                    console.log('🔹 Chưa đủ xe, cần thêm ' + remainingVehicles + ' xe nữa');
                    
                    // Đóng modal chỉnh sửa nhóm xe
                    if (editModal) {
                        editModal.classList.remove('show');
                        editModal.style.display = 'none';
                        console.log('✅ Đã đóng modal chỉnh sửa nhóm xe');
                    }
                    
                    // Cập nhật nhóm xe trước (không có vehicles)
                    const groupData = {
                        name: groupName,
                        vehicleCount: vehicleCount,
                        active: active,
                        description: description
                    };
                    
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
                        console.log('✅ Đã cập nhật nhóm xe thành công');
                        
                        // Mở modal thêm xe
                        const addVehiclesModal = document.getElementById('addVehiclesModal');
                        const addVehiclesContainer = document.getElementById('addVehiclesContainer');
                        const addVehiclesGroupId = document.getElementById('addVehiclesGroupId');
                        const addVehiclesCount = document.getElementById('addVehiclesCount');
                        const vehiclesToAddCount = document.getElementById('vehiclesToAddCount');
                        
                        if (addVehiclesModal && addVehiclesContainer && addVehiclesGroupId && addVehiclesCount && vehiclesToAddCount) {
                            // Lưu thông tin vào modal
                            addVehiclesGroupId.value = groupId;
                            addVehiclesCount.value = remainingVehicles;
                            vehiclesToAddCount.textContent = remainingVehicles;
                            
                            // Lưu vehicleCount mong muốn vào data attribute
                            addVehiclesModal.setAttribute('data-desired-vehicle-count', vehicleCount);
                            
                            // Xóa container và tạo form nhập xe
                            addVehiclesContainer.innerHTML = '';
                            
                            if (window.generateVehicleRows) {
                                const tempContainer = document.getElementById('vehiclesToAddContainer');
                                if (tempContainer) {
                                    tempContainer.innerHTML = '';
                                    window.generateVehicleRows(remainingVehicles);
                                    
                                    setTimeout(function() {
                                        const vehicleRows = document.querySelectorAll('#vehiclesToAddContainer .vehicle-row');
                                        vehicleRows.forEach(function(row) {
                                            addVehiclesContainer.appendChild(row);
                                        });
                                        
                                        // Mở modal
                                        addVehiclesModal.classList.add('show');
                                        addVehiclesModal.style.display = 'block';
                                        addVehiclesModal.style.visibility = 'visible';
                                        addVehiclesModal.style.opacity = '1';
                                        addVehiclesModal.style.zIndex = '1000';
                                        
                                        console.log('✅ Đã mở modal thêm xe với ' + vehicleRows.length + ' form nhập xe');
                                    }, 100);
                                } else {
                                    console.error('❌ Không tìm thấy vehiclesToAddContainer');
                                }
                            } else {
                                console.error('❌ window.generateVehicleRows không tồn tại!');
                            }
                        } else {
                            console.error('❌ Không tìm thấy modal thêm xe hoặc các elements liên quan');
                            alert('Lỗi: Không tìm thấy modal thêm xe. Vui lòng làm mới trang và thử lại.');
                        }
                    })
                    .catch(error => {
                        console.error('❌ Lỗi khi cập nhật nhóm xe:', error);
                        showUpdateMessage('Lỗi khi cập nhật nhóm xe: ' + error.message, 'error');
                    });
                    
                    return false; // Không submit form, đợi user nhập xe
                }
            }
            
            // Nếu có xe trong form edit modal, gửi kèm theo
            const vehicleRows = document.querySelectorAll('#vehiclesToAddContainer .vehicle-row');
            const vehicles = [];
            vehicleRows.forEach(function(row) {
                const vehicleId = row.querySelector('.vehicle-id-input')?.value.trim();
                const vehicleType = row.querySelector('.vehicle-type-input')?.value.trim();
                const vehicleNumber = row.querySelector('.vehicle-number-input')?.value.trim();
                const status = row.querySelector('.vehicle-status-input')?.value;
                
                if (vehicleId && vehicleType && vehicleNumber) {
                    vehicles.push({
                        vehicleId: vehicleId,
                        type: vehicleType,
                        vehicleNumber: vehicleNumber,
                        status: status || 'available'
                    });
                }
            });
            
            // Nếu không giảm số lượng xe, cập nhật bình thường
            const groupData = {
                name: groupName,
                vehicleCount: vehicleCount,
                active: active,
                description: description
            };
            
            // Nếu có xe trong form, gửi qua FormData
            if (vehicles.length > 0) {
                const formData = new FormData();
                formData.append('groupId', groupId);
                formData.append('name', groupName);
                formData.append('vehicleCount', vehicleCount);
                formData.append('active', active);
                formData.append('description', description);
                formData.append('vehicles', JSON.stringify(vehicles));
                
                fetch('/admin/staff-management/update/' + groupId, {
                    method: 'POST',
                    body: formData,
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest'
                    }
                })
                .then(response => {
                    if (response.ok || response.status === 302 || response.redirected) {
                        showUpdateMessage('Nhóm xe đã được cập nhật thành công!', 'success');
                        if (editModal) {
                            editModal.classList.remove('show');
                            editModal.style.display = 'none';
                        }
                        setTimeout(() => {
                            window.location.reload();
                        }, 1500);
                    } else {
                        return response.text().then(text => {
                            throw new Error(text);
                        });
                    }
                })
                .catch(error => {
                    showUpdateMessage('Lỗi khi cập nhật nhóm xe: ' + error.message, 'error');
                });
                
                return false;
            }

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

    // Xử lý modal thêm xe vào nhóm
    const addVehiclesModal = document.getElementById('addVehiclesModal');
    const addVehiclesForm = document.getElementById('addVehiclesForm');
    const cancelAddVehiclesBtn = document.getElementById('cancelAddVehiclesBtn');
    const addVehiclesModalClose = addVehiclesModal?.querySelector('.close-modal');

    // Hàm đóng modal thêm xe
    function closeAddVehiclesModal() {
        if (addVehiclesModal) {
            addVehiclesModal.classList.remove('show');
            addVehiclesModal.style.display = 'none';
        }
    }

    // Đóng modal khi click nút X hoặc Hủy
    if (addVehiclesModalClose) {
        addVehiclesModalClose.addEventListener('click', function() {
            closeAddVehiclesModal();
        });
    }

    if (cancelAddVehiclesBtn) {
        cancelAddVehiclesBtn.addEventListener('click', function() {
            closeAddVehiclesModal();
        });
    }

    // Đóng modal khi click bên ngoài modal
    if (addVehiclesModal) {
        window.addEventListener('click', function (event) {
            if (event.target === addVehiclesModal) {
                closeAddVehiclesModal();
            }
        });
    }

    // Xử lý submit form thêm xe
    if (addVehiclesForm) {
        addVehiclesForm.addEventListener('submit', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            console.log('🔹 Form submit thêm xe được trigger');
            
            const groupId = document.getElementById('addVehiclesGroupId').value;
            const vehicleRows = document.querySelectorAll('#addVehiclesContainer .vehicle-row');
            const vehicles = [];
            
            vehicleRows.forEach(function(row) {
                const vehicleId = row.querySelector('.vehicle-id-input')?.value.trim();
                const vehicleType = row.querySelector('.vehicle-type-input')?.value.trim();
                const vehicleNumber = row.querySelector('.vehicle-number-input')?.value.trim();
                const status = row.querySelector('.vehicle-status-input')?.value;
                
                if (vehicleId && vehicleType && vehicleNumber) {
                    vehicles.push({
                        vehicleId: vehicleId,
                        type: vehicleType,
                        vehicleNumber: vehicleNumber,
                        status: status || 'available'
                    });
                }
            });
            
            if (vehicles.length === 0) {
                alert('Vui lòng nhập ít nhất một xe!');
                return false;
            }
            
            // Lấy thông tin nhóm xe hiện tại để cập nhật lại
            fetch(`http://localhost:8083/api/vehicle-groups/${groupId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
            .then(response => response.json())
            .then(groupData => {
                // Lấy vehicleCount mong muốn từ modal (nếu có)
                const addVehiclesModal = document.getElementById('addVehiclesModal');
                const desiredVehicleCount = addVehiclesModal?.getAttribute('data-desired-vehicle-count');
                const newVehicleCount = desiredVehicleCount ? parseInt(desiredVehicleCount) : ((groupData.vehicleCount || 0) + vehicles.length);
                
                console.log('🔹 Cập nhật vehicleCount:', {
                    'Hiện tại': groupData.vehicleCount,
                    'Mong muốn': desiredVehicleCount,
                    'Số xe thêm': vehicles.length,
                    'Số lượng mới': newVehicleCount
                });
                
                // Cập nhật lại thông tin nhóm xe với vehicles và vehicleCount mới
                const formData = new FormData();
                formData.append('groupId', groupId);
                formData.append('name', groupData.name || '');
                formData.append('vehicleCount', newVehicleCount);
                formData.append('active', groupData.active || 'active');
                formData.append('description', groupData.description || '');
                formData.append('vehicles', JSON.stringify(vehicles));
                
                return fetch('/admin/staff-management/update/' + groupId, {
                    method: 'POST',
                    body: formData,
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest'
                    }
                });
            })
            .then(response => {
                if (response.ok || response.status === 302 || response.redirected) {
                    showUpdateMessage('Đã thêm ' + vehicles.length + ' xe vào nhóm thành công!', 'success');
                    closeAddVehiclesModal();
                    setTimeout(() => {
                        window.location.reload();
                    }, 1500);
                } else {
                    return response.text().then(text => {
                        throw new Error(text);
                    });
                }
            })
            .catch(error => {
                console.error('❌ Lỗi khi thêm xe:', error);
                showUpdateMessage('Lỗi khi thêm xe: ' + error.message, 'error');
            });
            
            return false;
        });
    }

    // Xử lý modal chọn xe xóa
    const deleteVehiclesModal = document.getElementById('deleteVehiclesModal');
    const deleteVehiclesForm = document.getElementById('deleteVehiclesForm');
    const cancelDeleteVehiclesBtn = document.getElementById('cancelDeleteVehiclesBtn');
    const deleteVehiclesModalClose = deleteVehiclesModal?.querySelector('.close-modal');

    // Hàm đóng modal xóa xe
    function closeDeleteVehiclesModal() {
        if (deleteVehiclesModal) {
            deleteVehiclesModal.classList.remove('show');
            deleteVehiclesModal.style.display = 'none';
        }
    }

    // Đóng modal khi click nút X hoặc Hủy
    if (deleteVehiclesModalClose) {
        deleteVehiclesModalClose.addEventListener('click', function() {
            closeDeleteVehiclesModal();
        });
    }

    if (cancelDeleteVehiclesBtn) {
        cancelDeleteVehiclesBtn.addEventListener('click', function() {
            closeDeleteVehiclesModal();
        });
    }

    // Đóng modal khi click bên ngoài modal
    if (deleteVehiclesModal) {
        window.addEventListener('click', function (event) {
            if (event.target === deleteVehiclesModal) {
                closeDeleteVehiclesModal();
            }
        });
    }

    // Hàm xử lý xóa xe (để có thể gọi từ nhiều nơi)
    function handleDeleteVehicles() {
        console.log('🔹 handleDeleteVehicles được gọi');
        
        // Kiểm tra nếu đang xử lý, không cho phép click lại
        const deleteVehiclesSubmitBtn = document.getElementById('deleteVehiclesSubmitBtn');
        if (deleteVehiclesSubmitBtn && deleteVehiclesSubmitBtn.disabled) {
            console.log('⚠️ Đang xử lý, không cho phép click lại');
            return false;
        }
        
        // Disable nút để tránh multiple clicks
        if (deleteVehiclesSubmitBtn) {
            deleteVehiclesSubmitBtn.disabled = true;
            deleteVehiclesSubmitBtn.textContent = 'Đang xóa...';
            deleteVehiclesSubmitBtn.style.opacity = '0.6';
            deleteVehiclesSubmitBtn.style.cursor = 'not-allowed';
        }
        
        const groupId = document.getElementById('deleteVehiclesGroupId')?.value;
        const vehiclesToDelete = parseInt(document.getElementById('deleteVehiclesCount')?.value) || 0;
        const checkedBoxes = document.querySelectorAll('#deleteVehiclesContainer .vehicle-delete-checkbox:checked');
        
        console.log('🔹 GroupId:', groupId);
        console.log('🔹 Số lượng xe cần xóa:', vehiclesToDelete);
        console.log('🔹 Số lượng xe đã chọn:', checkedBoxes.length);
        
        if (!groupId) {
            alert('Lỗi: Không tìm thấy mã nhóm xe!');
            return false;
        }
        
        if (checkedBoxes.length === 0) {
            alert('Vui lòng chọn ít nhất 1 xe để xóa!');
            return false;
        }
        
        if (checkedBoxes.length !== vehiclesToDelete) {
            alert('Vui lòng chọn đúng ' + vehiclesToDelete + ' xe cần xóa! (Đã chọn: ' + checkedBoxes.length + ')');
            return false;
        }
        
        const vehicleIdsToDelete = Array.from(checkedBoxes).map(cb => cb.value);
        console.log('🔹 Các xe cần xóa:', vehicleIdsToDelete);
        
        // Biến để lưu số lượng xe đã xóa thành công
        let actualDeletedCount = vehicleIdsToDelete.length;
        
        // Xóa các xe được chọn - xử lý từng xe để theo dõi lỗi chi tiết
        const deletePromises = vehicleIdsToDelete.map(async (vehicleId) => {
            try {
                const response = await fetch(`http://localhost:8083/api/vehicles/${vehicleId}`, {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });
                
                const responseText = await response.text();
                
                if (response.ok) {
                    console.log(`✅ Đã xóa xe ${vehicleId} thành công`);
                    return { vehicleId, success: true, message: 'Xóa thành công' };
                } else if (response.status === 404) {
                    console.warn(`⚠️ Xe ${vehicleId} không tồn tại (có thể đã bị xóa trước đó)`);
                    return { vehicleId, success: true, message: 'Đã bị xóa trước đó', warning: true };
                } else {
                    console.error(`❌ Lỗi khi xóa xe ${vehicleId}: ${response.status} - ${responseText}`);
                    return { vehicleId, success: false, message: responseText || `Lỗi ${response.status}` };
                }
            } catch (error) {
                console.error(`❌ Lỗi khi xóa xe ${vehicleId}:`, error);
                return { vehicleId, success: false, message: error.message };
            }
        });
        
        Promise.all(deletePromises)
        .then(results => {
            const successCount = results.filter(r => r.success).length;
            const failCount = results.filter(r => !r.success).length;
            const warningCount = results.filter(r => r.warning).length;
            
            console.log(`✅ Đã xóa ${successCount}/${vehicleIdsToDelete.length} xe`);
            console.log('🔹 Chi tiết kết quả:', results);
            
            // Nếu có xe không xóa được
            if (failCount > 0) {
                const failedVehicles = results.filter(r => !r.success);
                const failedMessages = failedVehicles.map(r => `${r.vehicleId}: ${r.message}`).join('; ');
                throw new Error(`Không thể xóa ${failCount} xe: ${failedMessages}`);
            }
            
            // Nếu tất cả đều thành công (bao gồm cả các xe đã bị xóa trước đó)
            if (successCount === vehicleIdsToDelete.length) {
                const deletedCount = results.filter(r => r.success && !r.warning).length;
                actualDeletedCount = deletedCount; // Lưu số lượng xe thực sự đã xóa
                if (warningCount > 0) {
                    console.log(`⚠️ ${warningCount} xe đã bị xóa trước đó nhưng không ảnh hưởng`);
                }
                // Trả về số lượng xe thực sự đã xóa (không tính các xe đã bị xóa trước đó)
                return { deletedCount, warningCount };
            }
            
            // Trường hợp này không nên xảy ra nhưng để an toàn
            throw new Error('Có lỗi không xác định khi xóa xe');
        })
        .then((result) => {
            // Đợi một chút để đảm bảo database đã cập nhật
            return new Promise(resolve => setTimeout(resolve, 200));
        })
        .then(() => {
            // Lấy lại thông tin nhóm xe sau khi xóa để có số lượng xe chính xác
            return fetch(`http://localhost:8083/api/vehicle-groups/${groupId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
        })
        .then(response => response.json())
        .then(groupData => {
            console.log('🔹 Thông tin nhóm xe sau khi xóa:', groupData);
            
            // Lấy thông tin nhóm xe từ modal để cập nhật
            const deleteVehiclesModal = document.getElementById('deleteVehiclesModal');
            const groupName = deleteVehiclesModal?.getAttribute('data-group-name') || groupData.name || '';
            const active = deleteVehiclesModal?.getAttribute('data-group-active') || groupData.active || 'active';
            const description = deleteVehiclesModal?.getAttribute('data-group-description') || groupData.description || '';
            const newVehicleCount = deleteVehiclesModal?.getAttribute('data-new-vehicle-count') || groupData.vehicleCount || 0;
            
            // Cập nhật vehicleCount theo số lượng xe thực tế sau khi xóa
            const formData = new FormData();
            formData.append('groupId', groupId);
            formData.append('name', groupName);
            formData.append('vehicleCount', newVehicleCount);
            formData.append('active', active);
            formData.append('description', description);
            
            return fetch('/admin/staff-management/update/' + groupId, {
                method: 'POST',
                body: formData,
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });
        })
        .then(response => {
            if (response.ok || response.status === 302 || response.redirected) {
                showUpdateMessage('Đã xóa ' + actualDeletedCount + ' xe khỏi nhóm thành công!', 'success');
                closeDeleteVehiclesModal();
                setTimeout(() => {
                    window.location.reload();
                }, 1500);
            } else {
                return response.text().then(text => {
                    throw new Error(text);
                });
            }
        })
        .catch(error => {
            console.error('❌ Lỗi khi xóa xe:', error);
            showUpdateMessage('Lỗi khi xóa xe: ' + error.message, 'error');
        })
        .finally(() => {
            // Enable lại nút sau khi hoàn thành
            if (deleteVehiclesSubmitBtn) {
                deleteVehiclesSubmitBtn.disabled = false;
                deleteVehiclesSubmitBtn.textContent = 'Xóa Các Xe Đã Chọn';
                deleteVehiclesSubmitBtn.style.opacity = '1';
                deleteVehiclesSubmitBtn.style.cursor = 'pointer';
            }
        });
        
        return false;
    }
    
    // Xử lý submit form xóa xe
    if (deleteVehiclesForm) {
        console.log('✅ Đã đăng ký event listener cho form xóa xe');
        
        deleteVehiclesForm.addEventListener('submit', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            console.log('🔹 Form submit xóa xe được trigger');
            handleDeleteVehicles();
            return false;
        });
    }
    
    // Thêm event listener trực tiếp cho nút submit để đảm bảo hoạt động
    const deleteVehiclesSubmitBtn = document.getElementById('deleteVehiclesSubmitBtn');
    if (deleteVehiclesSubmitBtn) {
        console.log('✅ Đã đăng ký event listener trực tiếp cho nút xóa xe');
        deleteVehiclesSubmitBtn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            console.log('🔹 Nút xóa xe được click trực tiếp');
            handleDeleteVehicles();
        });
    } else {
        console.warn('⚠️ Không tìm thấy nút deleteVehiclesSubmitBtn');
    }
    
    // Sử dụng event delegation để đảm bảo hoạt động ngay cả khi modal được tạo sau
    if (deleteVehiclesModal) {
        deleteVehiclesModal.addEventListener('click', function(e) {
            if (e.target && e.target.id === 'deleteVehiclesSubmitBtn') {
                e.preventDefault();
                e.stopPropagation();
                console.log('🔹 Nút xóa xe được click qua event delegation');
                handleDeleteVehicles();
            }
        });
    }
});
