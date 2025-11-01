// API Configuration
const API_URL = 'http://localhost:8083/api/vehicleservices';
let currentMode = 'request';
let currentFilter = 'all';
let services = [];
let vehicles = [];
let selectedServiceId = null;

// On page load
document.addEventListener('DOMContentLoaded', function () {
    initializeListeners();
    loadVehicles();
    loadServices();
});

// Initialize Event Listeners
function initializeListeners() {
    // Mode toggle (Request/Update)
    document.querySelectorAll('.mode-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            document.querySelector('.mode-btn.active').classList.remove('active');
            this.classList.add('active');
            currentMode = this.dataset.mode;
            updateUIForMode(currentMode);
        });
    });

    // Filter tabs
    document.querySelectorAll('.filter-tab').forEach(tab => {
        tab.addEventListener('click', function () {
            document.querySelector('.filter-tab.active').classList.remove('active');
            this.classList.add('active');
            currentFilter = this.dataset.filter;
            renderServicesList();
        });
    });

    // Confirm button
    document.getElementById('confirmBtn').addEventListener('click', handleConfirm);

    // Cancel button
    document.getElementById('cancelBtn').addEventListener('click', resetForm);

    // Status select change
    document.getElementById('status-select').addEventListener('change', function() {
        const completionDateGroup = document.getElementById('completion-date-group');
        if (this.value === 'completed') {
            completionDateGroup.style.display = 'block';
            document.getElementById('completion-date').valueAsDate = new Date();
        } else {
            completionDateGroup.style.display = 'none';
        }
    });
}

// Load Vehicles
function loadVehicles() {
    fetch(`${API_URL}/vehicles`)
        .then(res => res.json())
        .then(data => {
            vehicles = data;
            const select = document.getElementById('vehicle-select');
            select.innerHTML = '<option value="">Chọn xe</option>';
            data.forEach(vehicle => {
                const option = document.createElement('option');
                option.value = vehicle.id;
                option.textContent = `${vehicle.vehicleNumber || 'N/A'} - ${vehicle.vehicleType || 'N/A'}`;
                select.appendChild(option);
            });
        })
        .catch(err => {
            console.error('Error loading vehicles:', err);
            showError('Không thể tải danh sách xe');
        });
}

// Load Services
function loadServices() {
    fetch(`${API_URL}/all`)
        .then(res => res.json())
        .then(data => {
            services = data;
            updateStats();
            renderServicesList();
        })
        .catch(err => {
            console.error('Error loading services:', err);
            showError('Không thể tải danh sách dịch vụ');
        });
}

// Render Services List
function renderServicesList() {
    const servicesList = document.getElementById('services-list');
    servicesList.innerHTML = '';

    let filteredServices = services;

    if (currentFilter !== 'all') {
        filteredServices = services.filter(s => s.status === currentFilter);
    }

    if (filteredServices.length === 0) {
        servicesList.innerHTML = '<div class="empty-state">Không có dịch vụ nào</div>';
        return;
    }

    filteredServices.sort((a, b) => {
        const dateA = new Date(a.requestDate);
        const dateB = new Date(b.requestDate);
        return dateB - dateA;
    });

    filteredServices.forEach(service => {
        const item = createServiceItem(service);
        servicesList.appendChild(item);
    });
}

// Create Service Item
function createServiceItem(service) {
    const template = document.getElementById('service-item-template');
    const item = template.content.cloneNode(true);
    const container = item.querySelector('.service-item');

    const vehicle = vehicles.find(v => v.id === service.vehicleId);

    item.querySelector('.service-name').textContent = service.serviceName || 'Chưa có tên';
    item.querySelector('.service-vehicle').textContent = vehicle 
        ? `${vehicle.vehicleNumber} - ${vehicle.vehicleType}`
        : `Xe #${service.vehicleId}`;
    
    const requestDate = new Date(service.requestDate);
    item.querySelector('.service-date').textContent = `Yêu cầu: ${requestDate.toLocaleDateString('vi-VN')}`;

    const statusBadge = item.querySelector('.status-badge');
    statusBadge.textContent = getStatusText(service.status);
    statusBadge.className = `status-badge status-${service.status}`;

    // Add edit/delete handlers
    item.querySelector('.btn-edit-service').addEventListener('click', () => editService(service));
    item.querySelector('.btn-delete-service').addEventListener('click', () => deleteService(service.id));

    // Change background based on status
    if (service.status === 'completed') {
        container.classList.add('success');
    } else if (service.status === 'in_progress') {
        container.classList.add('info');
    } else if (service.status === 'pending') {
        container.classList.add('warning');
    }

    return item;
}

// Get Status Text
function getStatusText(status) {
    const statusMap = {
        'pending': 'Chờ xử lý',
        'in_progress': 'Đang thực hiện',
        'completed': 'Hoàn thành',
        'cancelled': 'Đã hủy'
    };
    return statusMap[status] || status;
}

// Update Stats
function updateStats() {
    document.getElementById('total-services').textContent = services.length;
    document.getElementById('pending-services').textContent = services.filter(s => s.status === 'pending').length;
    document.getElementById('active-services').textContent = services.filter(s => s.status === 'in_progress').length;
    document.getElementById('completed-services').textContent = services.filter(s => s.status === 'completed').length;
}

// Edit Service
function editService(service) {
    selectedServiceId = service.id;
    currentMode = 'update';
    
    document.querySelectorAll('.mode-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.mode === 'update');
    });

    // Populate form
    document.getElementById('vehicle-select').value = service.vehicleId;
    document.getElementById('service-type').value = service.serviceType || 'maintenance';
    document.getElementById('service-name').value = service.serviceName || '';
    document.getElementById('service-description').value = service.serviceDescription || '';
    document.getElementById('status-select').value = service.status;
    document.getElementById('notes').value = service.notes || '';

    if (service.requestDate) {
        document.getElementById('request-date').value = new Date(service.requestDate).toISOString().split('T')[0];
    }

    if (service.completionDate) {
        document.getElementById('completion-date').value = new Date(service.completionDate).toISOString().split('T')[0];
        document.getElementById('completion-date-group').style.display = 'block';
    }

    updateUIForMode('update');
}

// Delete Service
function deleteService(id) {
    if (!confirm('Bạn có chắc muốn xóa dịch vụ này?')) {
        return;
    }

    fetch(`${API_URL}/${id}`, {
        method: 'DELETE'
    })
        .then(() => {
            loadServices();
            showSuccess('Đã xóa dịch vụ thành công');
        })
        .catch(err => {
            console.error('Error deleting service:', err);
            showError('Không thể xóa dịch vụ');
        });
}

// Handle Confirm
function handleConfirm() {
    const vehicleId = document.getElementById('vehicle-select').value;
    const serviceName = document.getElementById('service-name').value;
    const serviceType = document.getElementById('service-type').value;
    const serviceDescription = document.getElementById('service-description').value;
    const notes = document.getElementById('notes').value;

    if (!vehicleId) {
        showError('⚠️ Vui lòng chọn xe!');
        return;
    }

    if (!serviceName) {
        showError('⚠️ Vui lòng nhập tên dịch vụ!');
        return;
    }

    const data = {
        vehicleId: parseInt(vehicleId),
        serviceName: serviceName,
        serviceType: serviceType,
        serviceDescription: serviceDescription,
        notes: notes
    };

    if (currentMode === 'update') {
        if (!selectedServiceId) {
            showError('Không tìm thấy dịch vụ cần cập nhật');
            return;
        }
        updateService(selectedServiceId, data);
    } else {
        createService(data);
    }
}

// Create Service
function createService(data) {
    fetch(`${API_URL}/create`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(res => res.json())
        .then(service => {
            console.log('Service created:', service);
            showSuccess('✅ Tạo dịch vụ thành công!');
            resetForm();
            loadServices();
        })
        .catch(err => {
            console.error('Error creating service:', err);
            showError('❌ Không thể tạo dịch vụ');
        });
}

// Update Service
function updateService(id, data) {
    data.status = document.getElementById('status-select').value;

    if (data.status === 'completed' && !data.completionDate) {
        data.completionDate = new Date().toISOString();
    }

    fetch(`${API_URL}/update/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(res => res.json())
        .then(service => {
            console.log('Service updated:', service);
            showSuccess('✅ Cập nhật dịch vụ thành công!');
            resetForm();
            loadServices();
        })
        .catch(err => {
            console.error('Error updating service:', err);
            showError('❌ Không thể cập nhật dịch vụ');
        });
}

// Update UI for Mode
function updateUIForMode(mode) {
    const confirmBtn = document.getElementById('confirmBtn');
    const statusGroup = document.getElementById('status-group');

    if (mode === 'update') {
        confirmBtn.textContent = 'Cập Nhật Dịch Vụ';
        statusGroup.style.display = 'block';
    } else {
        confirmBtn.textContent = 'Tạo Dịch Vụ';
        statusGroup.style.display = 'none';
    }
}

// Reset Form
function resetForm() {
    document.getElementById('vehicle-select').value = '';
    document.getElementById('service-name').value = '';
    document.getElementById('service-description').value = '';
    document.getElementById('notes').value = '';
    document.getElementById('status-select').value = 'pending';
    document.getElementById('completion-date-group').style.display = 'none';
    
    selectedServiceId = null;
    currentMode = 'request';
    
    document.querySelectorAll('.mode-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.mode === 'request');
    });
    
    updateUIForMode('request');
}

// Show Success Message
function showSuccess(message) {
    // Simple alert for now
    alert(message);
}

// Show Error Message
function showError(message) {
    alert(message);
}




