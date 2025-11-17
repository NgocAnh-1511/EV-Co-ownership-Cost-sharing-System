/**
 * Vehicle Management Page JavaScript
 */

let currentVehicleId = null;
let deleteVehicleId = null;

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeSearch();
    initializeEventListeners();
    initializeModals();
});

/**
 * Initialize search functionality
 */
function initializeSearch() {
    const searchInput = document.getElementById('searchInput');
    const searchFilterForm = document.getElementById('searchFilterForm');

    if (!searchInput || !searchFilterForm) {
        return;
    }

    // Search form submission on Enter key
    searchInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            searchFilterForm.submit();
        }
    });
}

/**
 * Initialize event listeners
 */
function initializeEventListeners() {
    // Use event delegation for edit and delete buttons
    const tableBody = document.getElementById('vehicleTableBody');
    if (tableBody) {
        tableBody.addEventListener('click', function(e) {
            const editButton = e.target.closest('.btn-edit');
            if (editButton) {
                e.preventDefault();
                const vehicleId = editButton.getAttribute('data-vehicle-id');
                if (vehicleId) {
                    editVehicle(vehicleId);
                }
            }
            
            const deleteButton = e.target.closest('.btn-delete');
            if (deleteButton) {
                e.preventDefault();
                const vehicleId = deleteButton.getAttribute('data-vehicle-id');
                if (vehicleId) {
                    showDeleteModal(vehicleId);
                }
            }
        });
    }
}

/**
 * Initialize modals
 */
function initializeModals() {
    // Close modal when clicking outside
    const vehicleModal = document.getElementById('vehicleModal');
    const deleteModal = document.getElementById('deleteModal');
    
    if (vehicleModal) {
        vehicleModal.addEventListener('click', function(e) {
            if (e.target === vehicleModal) {
                closeVehicleModal();
            }
        });
    }
    
    if (deleteModal) {
        deleteModal.addEventListener('click', function(e) {
            if (e.target === deleteModal) {
                closeDeleteModal();
            }
        });
    }
}

/**
 * Open add vehicle modal
 */
function openAddVehicleModal() {
    currentVehicleId = null;
    document.getElementById('modalTitle').textContent = 'Thêm Xe Mới';
    document.getElementById('vehicleForm').reset();
    document.getElementById('vehicleId').value = '';
    document.getElementById('vehicleIdInput').disabled = false;
    
    // Load danh sách nhóm xe chưa có xe (không có currentGroupId)
    // Danh sách này đã được load từ server khi render trang, nhưng có thể reload để đảm bảo mới nhất
    // Hoặc có thể gọi API để reload danh sách nhóm
    // Hiện tại sẽ dùng danh sách đã có trong form (từ server render)
    // Nếu muốn reload, có thể gọi API: /api/vehicle-groups/available
    
    document.getElementById('vehicleModal').style.display = 'block';
}

/**
 * Edit vehicle
 * @param {string} id - Vehicle ID
 */
function editVehicle(id) {
    if (!id) {
        console.error('Vehicle ID is required');
        return;
    }
    
    currentVehicleId = id;
    document.getElementById('modalTitle').textContent = 'Sửa Thông Tin Xe';
    document.getElementById('vehicleIdInput').disabled = true;
    
    // Load vehicle data
    fetch('/ext/admin/vehicle-management/api/' + id)
        .then(response => {
            if (!response.ok) {
                throw new Error('Không thể tải thông tin xe');
            }
            return response.json();
        })
        .then(data => {
            // API trả về object có vehicle và availableGroups
            const vehicle = data.vehicle || data; // Support cả format cũ và mới
            const availableGroups = data.availableGroups || [];
            
            document.getElementById('vehicleId').value = vehicle.vehicleId || '';
            document.getElementById('vehicleIdInput').value = vehicle.vehicleId || '';
            document.getElementById('vehicleName').value = vehicle.name || '';
            document.getElementById('vehicleNumber').value = vehicle.vehicleNumber || '';
            document.getElementById('vehicleType').value = vehicle.type || '';
            document.getElementById('status').value = vehicle.status || 'ready';
            
            // Cập nhật danh sách nhóm xe available
            if (availableGroups && availableGroups.length > 0) {
                const groupSelect = document.getElementById('groupId');
                const currentGroupId = vehicle.groupId || '';
                
                // Clear và thêm option mặc định
                groupSelect.innerHTML = '<option value="">-- Chọn nhóm xe (tùy chọn) --</option>';
                
                // Thêm các nhóm available
                availableGroups.forEach(group => {
                    const option = document.createElement('option');
                    option.value = group.groupId;
                    option.textContent = group.name;
                    if (group.groupId === currentGroupId) {
                        option.selected = true;
                    }
                    groupSelect.appendChild(option);
                });
            } else if (vehicle.groupId) {
                // Nếu không có availableGroups, vẫn set groupId hiện tại
                document.getElementById('groupId').value = vehicle.groupId;
            }
            
            document.getElementById('vehicleModal').style.display = 'block';
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Không thể tải thông tin xe: ' + error.message);
        });
}

/**
 * Close vehicle modal
 */
function closeVehicleModal() {
    document.getElementById('vehicleModal').style.display = 'none';
    document.getElementById('vehicleForm').reset();
    currentVehicleId = null;
}

/**
 * Save vehicle (add or update)
 */
function saveVehicle(event) {
    event.preventDefault();
    
    const formData = {
        groupId: document.getElementById('groupId').value,
        vehicles: [{
            vehicleId: document.getElementById('vehicleIdInput').value,
            vehicleName: document.getElementById('vehicleName').value,
            vehicleNumber: document.getElementById('vehicleNumber').value,
            type: document.getElementById('vehicleType').value,
            status: document.getElementById('status').value
        }]
    };
    
    // Nhóm xe không bắt buộc nữa
    // if (!formData.groupId) {
    //     alert('Vui lòng chọn nhóm xe');
    //     return;
    // }
    
    const url = currentVehicleId 
        ? '/ext/admin/vehicle-management/update/' + currentVehicleId
        : '/ext/admin/vehicle-management/add';
    
    const method = currentVehicleId ? 'PUT' : 'POST';
    // Xử lý groupId: nếu rỗng hoặc không có thì gửi null
    const groupId = formData.groupId && formData.groupId.trim() !== '' ? formData.groupId.trim() : null;
    
    const requestData = currentVehicleId 
        ? {
            groupId: groupId, // Cho phép null
            vehicleName: formData.vehicles[0].vehicleName,
            vehicleNumber: formData.vehicles[0].vehicleNumber,
            type: formData.vehicles[0].type,
            status: formData.vehicles[0].status
          }
        : {
            groupId: groupId, // Cho phép null
            vehicles: formData.vehicles
          };
    
    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(data => {
                throw new Error(data.message || 'Có lỗi xảy ra');
            }).catch(() => {
                return response.text().then(text => {
                    throw new Error(text || 'Có lỗi xảy ra');
                });
            });
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            alert(data.message || (currentVehicleId ? 'Cập nhật xe thành công!' : 'Thêm xe thành công!'));
            closeVehicleModal();
            window.location.reload();
        } else {
            alert(data.message || 'Không thể lưu xe');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Lỗi: ' + error.message);
    });
}

/**
 * Show delete confirmation modal
 */
function showDeleteModal(id) {
    deleteVehicleId = id;
    document.getElementById('deleteModal').style.display = 'block';
}

/**
 * Close delete modal
 */
function closeDeleteModal() {
    document.getElementById('deleteModal').style.display = 'none';
    deleteVehicleId = null;
}

/**
 * Confirm delete vehicle
 */
function confirmDelete() {
    if (!deleteVehicleId) {
        return;
    }
    
    fetch('/ext/admin/vehicle-management/delete/' + deleteVehicleId, {
        method: 'DELETE'
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(data => {
                throw new Error(data.message || 'Có lỗi xảy ra khi xóa xe');
            }).catch(() => {
                return response.text().then(text => {
                    throw new Error(text || 'Có lỗi xảy ra khi xóa xe');
                });
            });
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            alert(data.message || 'Xóa xe thành công!');
            closeDeleteModal();
            window.location.reload();
        } else {
            alert(data.message || 'Không thể xóa xe');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Lỗi: ' + error.message);
    });
}
