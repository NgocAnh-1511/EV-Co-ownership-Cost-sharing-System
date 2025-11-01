// API Configuration
const GROUP_API_URL = 'http://localhost:8083/api/vehiclegroups';
const HISTORY_API_URL = 'http://localhost:8083/api/vehiclehistory';
let currentFilter = 'all';
let groups = [];
let selectedGroupId = null;

document.addEventListener('DOMContentLoaded', function () {
    initializeListeners();
    loadGroups();
});

// Initialize Event Listeners
function initializeListeners() {
    // Filter tabs
    document.querySelectorAll('.filter-tab').forEach(tab => {
        tab.addEventListener('click', function () {
            document.querySelector('.filter-tab.active').classList.remove('active');
            this.classList.add('active');
            currentFilter = this.dataset.filter;
            renderGroupsList();
        });
    });

    // Confirm button
    document.getElementById('confirmBtn').addEventListener('click', handleConfirm);

    // Cancel button
    document.getElementById('cancelBtn').addEventListener('click', resetForm);
}

// Load Groups
function loadGroups() {
    fetch(`${GROUP_API_URL}/all`)
        .then(res => res.json())
        .then(data => {
            groups = data;
            renderGroupsList();
        })
        .catch(err => {
            console.error('Error loading groups:', err);
            showError('Không thể tải danh sách nhóm');
        });
}

// Render Groups List
function renderGroupsList() {
    const groupsList = document.getElementById('groups-list');
    groupsList.innerHTML = '';

    let filteredGroups = groups;
    if (currentFilter === 'active') {
        filteredGroups = groups; // Filter logic here
    }

    if (filteredGroups.length === 0) {
        groupsList.innerHTML = '<div class="empty-state">Không có nhóm nào</div>';
        return;
    }

    filteredGroups.forEach(group => {
        const item = createGroupItem(group);
        groupsList.appendChild(item);
    });
}

// Create Group Item
function createGroupItem(group) {
    const template = document.getElementById('group-item-template');
    const item = template.content.cloneNode(true);

    item.querySelector('.service-name').textContent = group.groupName || 'Chưa có tên';
    item.querySelector('.service-vehicle').textContent = group.description || 'Chưa có mô tả';
    
    const creationDate = new Date(group.creationDate);
    item.querySelector('.service-date').textContent = `Tạo: ${creationDate.toLocaleDateString('vi-VN')}`;

    // Add handlers
    item.querySelector('.btn-edit-group').addEventListener('click', () => editGroup(group));
    item.querySelector('.btn-delete-group').addEventListener('click', () => deleteGroup(group.id));
    item.querySelector('.btn-view-history').addEventListener('click', () => viewHistory(group.id));

    return item;
}

// Edit Group
function editGroup(group) {
    selectedGroupId = group.id;

    document.getElementById('group-name').value = group.groupName;
    document.getElementById('group-description').value = group.description || '';
}

// Delete Group
function deleteGroup(id) {
    if (!confirm('Bạn có chắc muốn xóa nhóm này?')) {
        return;
    }

    fetch(`${GROUP_API_URL}/${id}`, {
        method: 'DELETE'
    })
        .then(() => {
            loadGroups();
            showSuccess('Đã xóa nhóm thành công');
            resetForm();
        })
        .catch(err => {
            console.error('Error deleting group:', err);
            showError('❌ Không thể xóa nhóm');
        });
}

// View History
function viewHistory(groupId) {
    fetch(`${HISTORY_API_URL}/by-group/${groupId}`)
        .then(res => res.json())
        .then(data => {
            displayHistory(data);
            selectedGroupId = groupId;
        })
        .catch(err => {
            console.error('Error loading history:', err);
            showError('Không thể tải lịch sử');
        });
}

// Display History
function displayHistory(historyItems) {
    const historyContainer = document.getElementById('usage-history');
    historyContainer.innerHTML = '';

    if (historyItems.length === 0) {
        historyContainer.innerHTML = '<div class="empty-state">Chưa có lịch sử sử dụng</div>';
        return;
    }

    historyItems.forEach(item => {
        const div = document.createElement('div');
        div.className = 'history-item';
        
        const startDate = new Date(item.usageStart);
        const endDate = item.usageEnd ? new Date(item.usageEnd) : null;
        
        div.innerHTML = `
            <div class="history-info">
                <i class="fas fa-calendar-alt"></i>
                <span>Từ: ${startDate.toLocaleString('vi-VN')}</span>
                ${endDate ? `<span>Đến: ${endDate.toLocaleString('vi-VN')}</span>` : '<span>Đang sử dụng</span>'}
            </div>
            <div class="history-user">
                <i class="fas fa-user"></i>
                <span>User ID: ${item.userId}</span>
            </div>
        `;
        
        historyContainer.appendChild(div);
    });
}

// Handle Confirm
function handleConfirm() {
    const groupName = document.getElementById('group-name').value;
    const description = document.getElementById('group-description').value;

    if (!groupName) {
        showError('⚠️ Vui lòng nhập tên nhóm!');
        return;
    }

    const data = {
        groupName: groupName,
        description: description
    };

    if (selectedGroupId) {
        updateGroup(selectedGroupId, data);
    } else {
        createGroup(data);
    }
}

// Create Group
function createGroup(data) {
    fetch(`${GROUP_API_URL}/create`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(res => res.json())
        .then(group => {
            console.log('Group created:', group);
            showSuccess('✅ Tạo nhóm thành công!');
            resetForm();
            loadGroups();
        })
        .catch(err => {
            console.error('Error creating group:', err);
            showError('❌ Không thể tạo nhóm');
        });
}

// Update Group
function updateGroup(id, data) {
    fetch(`${GROUP_API_URL}/update/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(res => res.json())
        .then(group => {
            console.log('Group updated:', group);
            showSuccess('✅ Cập nhật nhóm thành công!');
            resetForm();
            loadGroups();
        })
        .catch(err => {
            console.error('Error updating group:', err);
            showError('❌ Không thể cập nhật nhóm');
        });
}

// Reset Form
function resetForm() {
    document.getElementById('group-name').value = '';
    document.getElementById('group-description').value = '';
    document.getElementById('usage-history').innerHTML = '';
    selectedGroupId = null;
}

// Show Success Message
function showSuccess(message) {
    alert(message);
}

// Show Error Message
function showError(message) {
    alert(message);
}




