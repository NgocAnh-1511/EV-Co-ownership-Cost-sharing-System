// Admin Groups Management Page JavaScript

let currentGroupsData = [];

// Initialize on DOM load
document.addEventListener('DOMContentLoaded', function() {
    console.log('Admin Groups page initializing...');
    loadGroups();
    initCreateGroupButton();
    initGroupForm();
});

// Load groups
async function loadGroups() {
    try {
        const response = await fetch('/api/groups');
        if (!response.ok) throw new Error('Failed to fetch groups');
        
        const groups = await response.json();
        currentGroupsData = groups || [];
        
        console.log('Groups loaded:', currentGroupsData);
        renderGroupsGrid(currentGroupsData);
        
    } catch (error) {
        console.error('Error loading groups:', error);
        const grid = document.getElementById('groups-grid');
        if (grid) {
            grid.innerHTML = `
                <div style="text-align: center; padding: 2rem; color: var(--danger);">
                    <i class="fas fa-exclamation-circle"></i> Lỗi khi tải dữ liệu: ${error.message}
                </div>
            `;
        }
    }
}

// Render groups grid
function renderGroupsGrid(groups) {
    const grid = document.getElementById('groups-grid');
    
    if (!grid) return;
    
    if (!groups || groups.length === 0) {
        grid.innerHTML = `
            <div style="text-align: center; padding: 2rem; color: var(--text-light); grid-column: 1 / -1;">
                <i class="fas fa-inbox"></i><br>Không có nhóm nào
            </div>
        `;
        return;
    }
    
    grid.innerHTML = groups.map(group => {
        // Backend trả về "Active" hoặc "Inactive", không phải "ACTIVE" hay "INACTIVE"
        const isActive = group.status === 'Active' || group.status === 'ACTIVE';
        const statusText = isActive ? 'Hoạt động' : 'Không hoạt động';
        const statusColor = isActive ? '#10B981' : '#F59E0B';
        
        return `
        <div class="group-card" style="background: white; border-radius: 8px; padding: 1.5rem; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
            <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 1rem;">
                <div>
                    <h3 style="margin: 0; color: var(--text-primary);">${escapeHtml(group.groupName || `Nhóm #${group.groupId}`)}</h3>
                    <p style="margin: 0.5rem 0 0 0; color: var(--text-light); font-size: 0.875rem;">
                        ID: ${group.groupId} | Xe: ${group.vehicleId || 'Chưa có'}
                    </p>
                </div>
                <span class="status-badge ${isActive ? 'paid' : 'pending'}" 
                      style="background: ${statusColor}; color: white; padding: 0.25rem 0.75rem; border-radius: 12px; font-size: 0.75rem; font-weight: 600;">
                    ${statusText}
                </span>
            </div>
            
            <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 1rem; margin-bottom: 1rem;">
                <div>
                    <div style="color: var(--text-light); font-size: 0.875rem;">Thành viên</div>
                    <div style="font-size: 1.25rem; font-weight: bold; color: var(--text-primary);">
                        ${group.memberCount || 0}
                    </div>
                </div>
                <div>
                    <div style="color: var(--text-light); font-size: 0.875rem;">Tỷ lệ sở hữu</div>
                    <div style="font-size: 1.25rem; font-weight: bold; color: var(--text-primary);">
                        ${group.totalOwnershipPercentage || 0}%
                    </div>
                </div>
            </div>
            
            <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
                <button class="btn btn-sm" style="background: var(--info); color: white; padding: 0.5rem 0.75rem;" 
                        onclick="viewGroupDetail(${group.groupId})" title="Xem chi tiết">
                    <i class="fas fa-eye"></i> Chi tiết
                </button>
                <button class="btn btn-sm" style="background: var(--primary); color: white; padding: 0.5rem 0.75rem;" 
                        onclick="editGroup(${group.groupId})" title="Sửa">
                    <i class="fas fa-edit"></i> Sửa
                </button>
                <button class="btn btn-sm" style="background: var(--danger); color: white; padding: 0.5rem 0.75rem;" 
                        onclick="deleteGroup(${group.groupId})" title="Xóa">
                    <i class="fas fa-trash"></i> Xóa
                </button>
            </div>
        </div>
    `;
    }).join('');
}

// Escape HTML để tránh XSS
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Initialize create group button
function initCreateGroupButton() {
    const btnCreate = document.getElementById('btn-create-group');
    if (btnCreate) {
        btnCreate.addEventListener('click', function() {
            openGroupModal();
        });
    }
}

// Initialize group form
function initGroupForm() {
    const form = document.getElementById('group-form');
    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            await saveGroup();
        });
    }
}

// Open group modal for create/edit
function openGroupModal(groupId = null) {
    const modal = document.getElementById('group-modal');
    const title = document.getElementById('group-modal-title');
    const form = document.getElementById('group-form');
    
    if (!modal || !title || !form) {
        console.error('Group modal elements not found');
        return;
    }
    
    // Reset form
    form.reset();
    document.getElementById('group-id').value = '';
    
    if (groupId) {
        title.textContent = 'Chỉnh sửa nhóm';
        loadGroupForEdit(groupId);
    } else {
        title.textContent = 'Tạo nhóm mới';
    }
    
    modal.classList.add('active');
    document.getElementById('modal-overlay').classList.add('active');
}

// Load group data for editing
async function loadGroupForEdit(groupId) {
    try {
        const response = await fetch(`/api/groups/${groupId}`);
        if (!response.ok) throw new Error('Failed to fetch group');
        
        const group = await response.json();
        
        document.getElementById('group-id').value = group.groupId || '';
        document.getElementById('group-name').value = group.groupName || '';
        document.getElementById('group-admin').value = group.adminId || '';
        document.getElementById('group-vehicle').value = group.vehicleId || '';
        
        // Set status - backend trả về "Active" hoặc "Inactive"
        const statusSelect = document.getElementById('group-status');
        if (statusSelect) {
            const status = group.status || 'Active';
            statusSelect.value = status === 'Active' || status === 'ACTIVE' ? 'Active' : 'Inactive';
        }
        
    } catch (error) {
        console.error('Error loading group for edit:', error);
        alert('Lỗi khi tải thông tin nhóm: ' + error.message);
    }
}

// Save group (create or update)
async function saveGroup() {
    const groupId = document.getElementById('group-id').value;
    const groupName = document.getElementById('group-name').value;
    const adminId = document.getElementById('group-admin').value;
    const vehicleId = document.getElementById('group-vehicle').value;
    const status = document.getElementById('group-status').value;
    
    if (!groupName || !adminId) {
        alert('Vui lòng điền đầy đủ thông tin bắt buộc');
        return;
    }
    
    const groupData = {
        groupName: groupName,
        adminId: parseInt(adminId),
        status: status
    };
    
    if (vehicleId) {
        groupData.vehicleId = parseInt(vehicleId);
    }
    
    try {
        let response;
        if (groupId) {
            // Update
            response = await fetch(`/api/groups/${groupId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(groupData)
            });
        } else {
            // Create
            response = await fetch('/api/groups', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(groupData)
            });
        }
        
        if (response.ok) {
            alert(groupId ? 'Cập nhật nhóm thành công!' : 'Tạo nhóm thành công!');
            closeGroupModal();
            loadGroups();
        } else {
            const errorData = await response.json().catch(() => ({}));
            alert('Lỗi: ' + (errorData.message || errorData.error || 'Không thể lưu nhóm'));
        }
    } catch (error) {
        console.error('Error saving group:', error);
        alert('Lỗi khi lưu nhóm: ' + error.message);
    }
}

// Close group modal
function closeGroupModal() {
    const modal = document.getElementById('group-modal');
    const overlay = document.getElementById('modal-overlay');
    
    if (modal) modal.classList.remove('active');
    if (overlay) overlay.classList.remove('active');
}

// View group detail
async function viewGroupDetail(groupId) {
    const modal = document.getElementById('group-detail-modal');
    const title = document.getElementById('group-detail-title');
    const content = document.getElementById('group-detail-content');
    
    if (!modal || !title || !content) {
        console.error('Group detail modal elements not found');
        return;
    }
    
    try {
        // Load group data
        const groupResponse = await fetch(`/api/groups/${groupId}`);
        if (!groupResponse.ok) throw new Error('Failed to fetch group');
        const group = await groupResponse.json();
        
        // Load members
        const membersResponse = await fetch(`/api/groups/${groupId}/members`);
        const members = membersResponse.ok ? await membersResponse.json() : [];
        
        const isActive = group.status === 'Active' || group.status === 'ACTIVE';
        const statusText = isActive ? 'Hoạt động' : 'Không hoạt động';
        const statusColor = isActive ? '#10B981' : '#F59E0B';
        
        // Render detail content
        content.innerHTML = `
            <div style="display: grid; gap: 1.5rem;">
                <div style="background: var(--light); padding: 1.5rem; border-radius: 8px;">
                    <h4 style="margin: 0 0 1rem 0; color: var(--primary);">Thông tin nhóm</h4>
                    <div style="display: grid; gap: 1rem;">
                        <div style="display: flex; justify-content: space-between;">
                            <strong>ID nhóm:</strong>
                            <span>${group.groupId}</span>
                        </div>
                        <div style="display: flex; justify-content: space-between;">
                            <strong>Tên nhóm:</strong>
                            <span>${escapeHtml(group.groupName || 'N/A')}</span>
                        </div>
                        <div style="display: flex; justify-content: space-between;">
                            <strong>Admin ID:</strong>
                            <span>${group.adminId || 'N/A'}</span>
                        </div>
                        <div style="display: flex; justify-content: space-between;">
                            <strong>Vehicle ID:</strong>
                            <span>${group.vehicleId || 'Chưa có'}</span>
                        </div>
                        <div style="display: flex; justify-content: space-between;">
                            <strong>Trạng thái:</strong>
                            <span style="background: ${statusColor}; color: white; padding: 0.25rem 0.75rem; border-radius: 12px; font-size: 0.875rem;">
                                ${statusText}
                            </span>
                        </div>
                        <div style="display: flex; justify-content: space-between;">
                            <strong>Ngày tạo:</strong>
                            <span>${group.createdAt ? new Date(group.createdAt).toLocaleString('vi-VN') : 'N/A'}</span>
                        </div>
                        <div style="display: flex; justify-content: space-between;">
                            <strong>Số thành viên:</strong>
                            <span>${members.length}</span>
                        </div>
                        <div style="display: flex; justify-content: space-between;">
                            <strong>Tỷ lệ sở hữu tổng:</strong>
                            <span>${group.totalOwnershipPercentage || 0}%</span>
                        </div>
                    </div>
                </div>
                
                <div style="background: var(--light); padding: 1.5rem; border-radius: 8px;">
                    <h4 style="margin: 0 0 1rem 0; color: var(--primary);">Danh sách thành viên</h4>
                    ${members.length > 0 ? `
                        <div style="display: grid; gap: 0.75rem;">
                            ${members.map(member => `
                                <div style="display: flex; justify-content: space-between; padding: 0.75rem; background: white; border-radius: 4px;">
                                    <div>
                                        <strong>User ID: ${member.userId}</strong>
                                        <div style="font-size: 0.875rem; color: var(--text-light);">
                                            Vai trò: ${member.role || 'Member'} | 
                                            Sở hữu: ${member.ownershipPercent || 0}%
                                        </div>
                                    </div>
                                </div>
                            `).join('')}
                        </div>
                    ` : '<p style="color: var(--text-light);">Chưa có thành viên nào</p>'}
                </div>
            </div>
        `;
        
        title.textContent = `Chi tiết nhóm: ${escapeHtml(group.groupName || `#${group.groupId}`)}`;
        modal.classList.add('active');
        document.getElementById('modal-overlay').classList.add('active');
        
    } catch (error) {
        console.error('Error loading group detail:', error);
        alert('Lỗi khi tải chi tiết nhóm: ' + error.message);
    }
}

// Close group detail modal
function closeGroupDetailModal() {
    const modal = document.getElementById('group-detail-modal');
    const overlay = document.getElementById('modal-overlay');
    
    if (modal) modal.classList.remove('active');
    if (overlay) overlay.classList.remove('active');
}

// Edit group
function editGroup(groupId) {
    openGroupModal(groupId);
}

// Delete group
async function deleteGroup(groupId) {
    if (!confirm(`Bạn có chắc chắn muốn xóa nhóm #${groupId}?`)) {
        return;
    }
    
    try {
        const response = await fetch(`/api/groups/${groupId}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            alert('Xóa nhóm thành công!');
            loadGroups();
        } else {
            alert('Lỗi khi xóa nhóm');
        }
    } catch (error) {
        console.error('Error deleting group:', error);
        alert('Lỗi khi xóa nhóm: ' + error.message);
    }
}

// Close modals when clicking overlay
document.addEventListener('DOMContentLoaded', function() {
    const overlay = document.getElementById('modal-overlay');
    if (overlay) {
        overlay.addEventListener('click', function() {
            closeGroupModal();
            closeGroupDetailModal();
        });
    }
});
