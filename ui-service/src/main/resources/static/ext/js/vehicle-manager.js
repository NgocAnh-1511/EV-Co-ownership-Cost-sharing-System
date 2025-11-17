function applyFilter(event) {
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }

    const searchQuery = document.querySelector('.filter-group input[type="text"]').value;
    const serviceFilter = document.getElementById('serviceFilter').value;
    const url = new URL(window.location.href);
    url.searchParams.set('searchQuery', searchQuery);
    url.searchParams.set('serviceFilter', serviceFilter);
    url.searchParams.delete('page');
    window.location.href = url.toString();
}

document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.querySelector('.filter-group input[type="text"]');
    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                applyFilter(e);
            }
        });
    }

    const filterBtn = document.getElementById('btnFilter');
    if (filterBtn) {
        filterBtn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            applyFilter(e);
        });
    }

    document.querySelectorAll('.btn-view-detail').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            const vehicleId = this.getAttribute('data-vehicle-id');
            if (vehicleId) {
                openVehicleDetailModal(vehicleId);
            }
        });
    });
});

let statusChanges = {};
let currentVehicleId = null;

function openVehicleDetailModal(vehicleId) {
    console.log('Mở modal chi tiết cho xe: ' + vehicleId);
    const modal = document.getElementById('vehicleDetailModal');
    if (modal) {
        modal.style.display = 'block';
    }
    currentVehicleId = vehicleId;
    statusChanges = {};
    loadVehicleDetail(vehicleId);
}

function closeVehicleDetailModal(skipCheck = false) {
    if (!skipCheck && Object.keys(statusChanges).length > 0) {
        if (confirm('Bạn có thay đổi chưa lưu. Bạn có muốn lưu trước khi đóng không?')) {
            saveChangesAndClose();
            return;
        }
    }
    const modal = document.getElementById('vehicleDetailModal');
    if (modal) {
        modal.style.display = 'none';
    }
    statusChanges = {};
    currentVehicleId = null;
}

async function loadVehicleDetail(vehicleId) {
    try {
        const row = document.querySelector(`tr[data-vehicle-id="${vehicleId}"]`);
        if (row) {
            const vehicleName = row.querySelector('.vehicle-name')?.textContent || '-';
            const plateNumber = row.cells[1]?.textContent || '-';
            const vehicleType = row.cells[2]?.textContent || '-';
            document.getElementById('modalVehicleId').textContent = vehicleId;
            document.getElementById('modalVehicleName').textContent = vehicleName;
            document.getElementById('modalPlateNumber').textContent = plateNumber;
            document.getElementById('modalVehicleType').textContent = vehicleType;
        }
        const response = await fetch(`/admin/vehicle-services/api/vehicle/${vehicleId}/services`);
        const data = await response.json();
        if (data.success && data.services) {
            displayServices(data.services);
            updateSaveButtonState();
        } else {
            document.getElementById('modalServicesList').innerHTML = '<div class="error-message">Không thể tải danh sách dịch vụ</div>';
            document.getElementById('modalServicesHistory').innerHTML = '<div class="no-data">Không có lịch sử dịch vụ</div>';
        }
    } catch (error) {
        console.error('Lỗi khi load chi tiết xe:', error);
        document.getElementById('modalServicesList').innerHTML = '<div class="error-message">Đã xảy ra lỗi khi tải dữ liệu</div>';
        document.getElementById('modalServicesHistory').innerHTML = '<div class="error-message">Đã xảy ra lỗi khi tải lịch sử</div>';
    }
}

function displayServices(services) {
    const servicesList = document.getElementById('modalServicesList');
    const servicesHistory = document.getElementById('modalServicesHistory');

    if (!services || services.length === 0) {
        servicesList.innerHTML = '<div class="no-data">Không có dịch vụ đang chờ</div>';
        servicesHistory.innerHTML = '<div class="no-data">Không có lịch sử dịch vụ</div>';
        return;
    }

    const pendingServices = [];
    const completedServices = [];

    services.forEach(service => {
        const status = (service.status || 'pending').toLowerCase().trim();
        if (status === 'completed' || status === 'complete') {
            completedServices.push(service);
        } else {
            pendingServices.push(service);
        }
    });

    console.log('📊 Phân tách dịch vụ từ bảng vehicleservice:');
    console.log('   - Dịch vụ đang chờ (pending/in_progress):', pendingServices.length);
    console.log('   - Lịch sử dịch vụ (completed):', completedServices.length);

    if (pendingServices.length === 0) {
        servicesList.innerHTML = '<div class="no-data">Không có dịch vụ đang chờ</div>';
    } else {
        let html = '<div class="service-items">';
        pendingServices.forEach(service => {
            html += buildServiceItem(service, false);
        });
        html += '</div>';
        servicesList.innerHTML = html;
    }

    if (completedServices.length === 0) {
        servicesHistory.innerHTML = '<div class="no-data">Không có lịch sử dịch vụ</div>';
    } else {
        let html = '<div class="service-items">';
        completedServices.forEach(service => {
            html += buildServiceItem(service, true);
        });
        html += '</div>';
        servicesHistory.innerHTML = html;
    }
}

function buildServiceItem(service, isHistory) {
    let id = '';
    let serviceId = '';
    let vehicleId = '';

    if (service.id !== undefined && service.id !== null) {
        if (typeof service.id === 'object') {
            id = '';
            serviceId = service.id.serviceId || '';
            vehicleId = service.id.vehicleId || '';
        } else {
            id = service.id;
            serviceId = service.serviceId || '';
            vehicleId = service.vehicleId || '';
        }
    } else {
        serviceId = service.serviceId || '';
        vehicleId = service.vehicleId || '';
    }

    const serviceName = service.serviceName || 'Dịch vụ không tên';
    const serviceType = service.serviceType || 'Không xác định';
    const serviceDescription = service.serviceDescription || '';
    const status = (service.status || 'pending').toLowerCase().trim();
    const requestDate = service.requestDate ? formatDate(service.requestDate) : '-';
    const completionDate = service.completionDate ? formatDate(service.completionDate) : null;
    const isCompleted = status === 'completed' || status === 'Completed' || isHistory;
    const disabledAttr = isCompleted ? 'disabled' : '';
    const readonlyClass = isCompleted ? 'status-readonly' : '';
    const historyClass = isHistory ? 'service-history-item' : '';

    return `<div class="service-item ${historyClass}" data-id="${id}" data-service-id="${serviceId}" data-vehicle-id="${vehicleId}">
        <div class="service-header">
            <h4>${serviceName}</h4>
            <select class="status-select ${readonlyClass}"
                    data-id="${id}"
                    data-service-id="${serviceId}"
                    data-vehicle-id="${vehicleId}"
                    data-original-status="${status}"
                    ${disabledAttr}
                    onchange="trackStatusChange(this)">
                <option value="pending" ${status === 'pending' ? 'selected' : ''}>Pending</option>
                <option value="in_progress" ${status === 'in_progress' || status === 'in progress' ? 'selected' : ''}>In Progress</option>
                <option value="completed" ${status === 'completed' ? 'selected' : ''}>Completed</option>
            </select>
            ${isCompleted ? '<span class="readonly-badge">Chỉ xem</span>' : ''}
        </div>
        <div class="service-details">
            <div class="detail-row"><label>Loại dịch vụ:</label><span>${serviceType}</span></div>
            <div class="detail-row"><label>Mô tả:</label><span>${serviceDescription}</span></div>
            <div class="detail-row"><label>Ngày yêu cầu:</label><span>${requestDate}</span></div>
            ${completionDate ? `<div class="detail-row"><label>Ngày hoàn thành:</label><span>${completionDate}</span></div>` : ''}
        </div>
    </div>`;
}

function formatDate(dateString) {
    if (!dateString) return '-';
    try {
        const date = new Date(dateString);
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = date.getFullYear();
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');
        return `${day}/${month}/${year} ${hours}:${minutes}:${seconds}`;
    } catch (e) {
        return dateString;
    }
}

function trackStatusChange(selectElement) {
    const id = selectElement.getAttribute('data-id');
    const serviceId = selectElement.getAttribute('data-service-id');
    const vehicleId = selectElement.getAttribute('data-vehicle-id');
    const originalStatus = selectElement.getAttribute('data-original-status');
    const newStatus = selectElement.value;

    const changeKey = id ? id : `${serviceId}_${vehicleId}`;

    if (!id && (!serviceId || !vehicleId)) {
        console.error('Không tìm thấy id hoặc serviceId/vehicleId');
        return;
    }

    if (newStatus === originalStatus) {
        delete statusChanges[changeKey];
    } else {
        statusChanges[changeKey] = {
            id: id,
            serviceId: serviceId,
            vehicleId: vehicleId,
            newStatus: newStatus,
            originalStatus: originalStatus
        };
    }

    updateSaveButtonState();
}

function updateSaveButtonState() {
    const saveBtn = document.querySelector('.btn-save');
    if (saveBtn) {
        const hasChanges = Object.keys(statusChanges).length > 0;
        if (hasChanges) {
            saveBtn.style.opacity = '1';
            saveBtn.style.cursor = 'pointer';
            saveBtn.disabled = false;
            saveBtn.textContent = `Lưu và Đóng (${Object.keys(statusChanges).length} thay đổi)`;
        } else {
            saveBtn.style.opacity = '0.6';
            saveBtn.style.cursor = 'not-allowed';
            saveBtn.disabled = true;
            saveBtn.textContent = 'Lưu và Đóng';
        }
    }
}

async function saveChangesAndClose() {
    const changes = Object.values(statusChanges);

    if (changes.length === 0) {
        closeVehicleDetailModal(true);
        setTimeout(() => {
            window.location.reload();
        }, 100);
        return;
    }

    const saveBtn = document.querySelector('.btn-save');
    if (saveBtn) {
        saveBtn.disabled = true;
        saveBtn.textContent = 'Đang lưu...';
    }

    try {
        const updatePromises = changes.map(change => {
            if (change.id) {
                return fetch(`/admin/vehicle-services/service/${change.id}/status`, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({ status: change.newStatus })
                });
            }
            return fetch(`/admin/vehicle-services/service/${change.serviceId}/vehicle/${change.vehicleId}/status`, {
                    method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({ status: change.newStatus })
            });
        });

        const responses = await Promise.all(updatePromises);
        const errorResponses = responses.filter(r => !r.ok);
        if (errorResponses.length > 0) {
            const errorTexts = await Promise.all(errorResponses.map(r => r.text()));
            console.error('Lỗi khi lưu:', errorTexts);
            alert('Có lỗi xảy ra khi lưu một số thay đổi. Vui lòng thử lại.\n' + errorTexts.join('\n'));
            if (saveBtn) {
                saveBtn.disabled = false;
                updateSaveButtonState();
            }
            return;
        }

        const results = await Promise.all(responses.map(r => r.json()));
        const failed = results.filter(r => !r.success);
        if (failed.length > 0) {
            const errorMessages = failed.map(r => r.message || 'Lỗi không xác định').join('\n');
            alert('Có lỗi xảy ra khi lưu một số thay đổi:\n' + errorMessages);
            if (saveBtn) {
                saveBtn.disabled = false;
                updateSaveButtonState();
            }
            return;
        }

        statusChanges = {};

        if (saveBtn) {
            saveBtn.textContent = 'Đã lưu thành công!';
            saveBtn.style.background = '#10B981';
            saveBtn.style.color = 'white';
        }

        closeVehicleDetailModal(true);

        console.log(`✅ Đã lưu thành công ${changes.length} thay đổi.`);

        setTimeout(() => {
            window.location.reload();
        }, 300);

    } catch (error) {
        console.error('Lỗi khi lưu thay đổi:', error);
        alert('Đã xảy ra lỗi khi lưu thay đổi: ' + error.message);
        if (saveBtn) {
            saveBtn.disabled = false;
            updateSaveButtonState();
        }
    }
}

function openAddNewServiceModal() {
    const modal = document.getElementById('addNewServiceModal');
    if (modal) {
        modal.style.display = 'block';
        const form = document.getElementById('addNewServiceForm');
        if (form) {
            form.reset();
        }
        const messageDiv = document.getElementById('addNewServiceMessage');
        if (messageDiv) {
            messageDiv.style.display = 'none';
        }
    }
}

function closeAddNewServiceModal() {
    const modal = document.getElementById('addNewServiceModal');
    if (modal) {
        modal.style.display = 'none';
        const form = document.getElementById('addNewServiceForm');
        if (form) {
            form.reset();
        }
        const messageDiv = document.getElementById('addNewServiceMessage');
        if (messageDiv) {
            messageDiv.style.display = 'none';
        }
    }
}

async function submitAddNewService() {
    const serviceId = document.getElementById('newServiceId').value.trim();
    const serviceName = document.getElementById('newServiceName').value.trim();
    const serviceType = document.getElementById('newServiceType').value;

    if (!serviceId) {
        showAddNewServiceMessage('Vui lòng nhập mã dịch vụ', 'error');
        return;
    }

    if (!serviceName) {
        showAddNewServiceMessage('Vui lòng nhập tên dịch vụ', 'error');
        return;
    }

    if (!serviceType) {
        showAddNewServiceMessage('Vui lòng chọn loại dịch vụ', 'error');
        return;
    }

    const submitBtn = document.querySelector('#addNewServiceModal .btn-save');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Đang thêm...';
    }

    try {
        const requestData = {
            serviceId: serviceId,
            serviceName: serviceName,
            serviceType: serviceType
        };

        console.log('📡 [ADD NEW SERVICE] Gửi request thêm dịch vụ mới:', requestData);

        const response = await fetch('/admin/vehicle-services/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        });

        const data = await response.json();

        if (data.success) {
            console.log('✅ [ADD NEW SERVICE] Đã thêm dịch vụ mới thành công');
            showAddNewServiceMessage('Đã thêm dịch vụ mới vào hệ thống thành công!', 'success');

            setTimeout(() => {
                closeAddNewServiceModal();
                window.location.reload();
            }, 1500);
        } else {
            console.error('❌ [ADD NEW SERVICE] Lỗi khi thêm dịch vụ:', data.message);
            showAddNewServiceMessage(data.message || 'Lỗi khi thêm dịch vụ', 'error');
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Thêm Dịch Vụ';
            }
        }
    } catch (error) {
        console.error('❌ [ADD NEW SERVICE] Lỗi khi thêm dịch vụ:', error);
        showAddNewServiceMessage('Lỗi khi thêm dịch vụ: ' + error.message, 'error');
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Thêm Dịch Vụ';
        }
    }
}

function showAddNewServiceMessage(message, type) {
    const messageDiv = document.getElementById('addNewServiceMessage');
    if (messageDiv) {
        messageDiv.textContent = message;
        messageDiv.className = type === 'success' ? 'alert alert-success' : 'alert alert-danger';
        messageDiv.style.display = 'block';

        if (type === 'success') {
            setTimeout(() => {
                messageDiv.style.display = 'none';
            }, 3000);
        }
    }
}

window.addEventListener('click', function(event) {
    const vehicleDetailModal = document.getElementById('vehicleDetailModal');
    const addNewServiceModal = document.getElementById('addNewServiceModal');

    if (event.target === vehicleDetailModal) {
        closeVehicleDetailModal();
    }

    if (event.target === addNewServiceModal) {
        closeAddNewServiceModal();
    }
});

