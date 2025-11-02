// Admin Dashboard JavaScript

// API Endpoints
const API = {
    GROUPS: '/groups/api/all',  // Updated to use UI service proxy
    COSTS: '/api/costs',
    AUTO_SPLIT: '/api/auto-split',
    USAGE: '/api/usage-tracking',
    PAYMENTS: '/api/payments'
};

// Global State
let currentSection = 'overview';
let chartsInitialized = false;
let monthlyChart = null;
let categoryChart = null;

// Initialize on DOM load
document.addEventListener('DOMContentLoaded', function() {
    initNavigation();
    initSplitMethodToggle();
    initAutoSplitForm();
    loadOverviewData();
    initCharts();
});

// ============ NAVIGATION ============
function initNavigation() {
    const navLinks = document.querySelectorAll('.admin-nav .nav-link');
    
    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const section = this.getAttribute('data-section');
            switchSection(section);
        });
    });
}

function switchSection(section) {
    // Update nav
    document.querySelectorAll('.admin-nav .nav-link').forEach(link => {
        link.classList.remove('active');
    });
    document.querySelector(`[data-section="${section}"]`).classList.add('active');
    
    // Update content
    document.querySelectorAll('.content-section').forEach(sec => {
        sec.classList.remove('active');
    });
    document.getElementById(`${section}-section`).classList.add('active');
    
    // Update title
    const titles = {
        'overview': 'Tổng quan',
        'cost-management': 'Quản lý chi phí',
        'auto-split': 'Chia tự động',
        'payment-tracking': 'Theo dõi thanh toán',
        'group-management': 'Quản lý nhóm',
        'reports': 'Báo cáo'
    };
    document.getElementById('page-title').textContent = titles[section];
    
    currentSection = section;
    
    // Load data for section
    switch(section) {
        case 'overview':
            loadOverviewData();
            break;
        case 'cost-management':
            loadCosts();
            break;
        case 'auto-split':
            loadGroupsForSplit();
            break;
        case 'payment-tracking':
            loadPayments();
            break;
        case 'group-management':
            loadGroups();
            break;
    }
}

// ============ OVERVIEW SECTION ============
async function loadOverviewData() {
    try {
        // Load stats
        const costs = await fetch(API.COSTS).then(r => r.json());
        
        // Calculate stats
        const totalCost = costs.reduce((sum, c) => sum + c.amount, 0);
        const currentMonth = new Date().getMonth() + 1;
        const currentYear = new Date().getFullYear();
        const monthCosts = costs.filter(c => {
            const date = new Date(c.createdAt);
            return date.getMonth() + 1 === currentMonth && date.getFullYear() === currentYear;
        });
        const monthTotal = monthCosts.reduce((sum, c) => sum + c.amount, 0);
        
        // Update stats
        document.getElementById('total-cost').textContent = formatCurrency(monthTotal);
        document.getElementById('paid-amount').textContent = formatCurrency(monthTotal * 0.6);
        document.getElementById('pending-amount').textContent = formatCurrency(monthTotal * 0.4);
        
        // Load groups for member count
        const groups = await fetch(API.GROUPS).then(r => r.json());
        let totalMembers = 0;
        groups.forEach(g => {
            if (g.members) totalMembers += g.members.length;
        });
        document.getElementById('total-members').textContent = totalMembers;
        
        // Load recent activities
        loadRecentActivities(costs);
        
    } catch (error) {
        console.error('Error loading overview:', error);
    }
}

function loadRecentActivities(costs) {
    const activityList = document.getElementById('activity-list');
    const recent = costs.slice(0, 5);
    
    activityList.innerHTML = recent.map(cost => `
        <div class="activity-item">
            <div class="activity-icon">
                <i class="fas fa-dollar-sign"></i>
            </div>
            <div class="activity-info">
                <div class="activity-title">Chi phí ${getCostTypeName(cost.costType)}</div>
                <div class="activity-time">${formatDate(cost.createdAt)}</div>
            </div>
            <div class="activity-amount" style="font-weight: 700; color: var(--primary);">
                ${formatCurrency(cost.amount)}
            </div>
        </div>
    `).join('');
}

// ============ CHARTS ============
function initCharts() {
    if (chartsInitialized) return;
    
    // Monthly Chart
    const monthlyCtx = document.getElementById('monthly-chart');
    if (monthlyCtx) {
        monthlyChart = new Chart(monthlyCtx, {
            type: 'line',
            data: {
                labels: ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'],
                datasets: [{
                    label: 'Chi phí',
                    data: [1200000, 1900000, 1500000, 2100000, 1800000, 2400000, 2200000, 1900000, 2100000, 2300000, 2500000, 2700000],
                    borderColor: '#3B82F6',
                    backgroundColor: 'rgba(59, 130, 246, 0.1)',
                    tension: 0.4,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function(value) {
                                return (value / 1000000).toFixed(1) + 'M';
                            }
                        }
                    }
                }
            }
        });
    }
    
    // Category Chart
    const categoryCtx = document.getElementById('category-chart');
    if (categoryCtx) {
        categoryChart = new Chart(categoryCtx, {
            type: 'doughnut',
            data: {
                labels: ['Sạc điện', 'Bảo dưỡng', 'Bảo hiểm', 'Đăng kiểm', 'Vệ sinh', 'Khác'],
                datasets: [{
                    data: [35, 25, 15, 10, 10, 5],
                    backgroundColor: [
                        '#3B82F6',
                        '#10B981',
                        '#F59E0B',
                        '#EF4444',
                        '#8B5CF6',
                        '#6B7280'
                    ]
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
    }
    
    chartsInitialized = true;
}

// ============ COST MANAGEMENT ============
async function loadCosts() {
    try {
        const response = await fetch(API.COSTS);
        const costs = await response.json();
        
        const tbody = document.getElementById('costs-tbody');
        tbody.innerHTML = costs.map(cost => `
            <tr>
                <td>${cost.costId}</td>
                <td>${getCostTypeName(cost.costType)}</td>
                <td>${formatCurrency(cost.amount)}</td>
                <td>${getSplitMethodName(cost.splitMethod || 'N/A')}</td>
                <td>${formatDate(cost.createdAt)}</td>
                <td>
                    <span class="status-badge ${getStatusClass(cost.status)}">
                        ${getStatusName(cost.status)}
                    </span>
                </td>
                <td>
                    <button class="btn btn-secondary" style="padding: 0.5rem 1rem;" onclick="viewCostDetail(${cost.costId})">
                        <i class="fas fa-eye"></i> Xem
                    </button>
                    <button class="btn btn-secondary" style="padding: 0.5rem 1rem; background: var(--danger); color: white;" onclick="deleteCost(${cost.costId})">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            </tr>
        `).join('');
        
    } catch (error) {
        console.error('Error loading costs:', error);
    }
}

function viewCostDetail(costId) {
    // Show modal with cost details
    alert('Chi tiết chi phí #' + costId);
}

async function deleteCost(costId) {
    if (confirm('Bạn có chắc muốn xóa chi phí này?')) {
        try {
            await fetch(`${API.COSTS}/${costId}`, { method: 'DELETE' });
            loadCosts();
            showNotification('Đã xóa chi phí thành công', 'success');
        } catch (error) {
            showNotification('Lỗi khi xóa chi phí', 'error');
        }
    }
}

// ============ AUTO SPLIT ============
function initSplitMethodToggle() {
    const splitMethod = document.getElementById('split-method');
    const usagePeriod = document.getElementById('usage-period');
    
    if (splitMethod) {
        splitMethod.addEventListener('change', function() {
            if (this.value === 'BY_USAGE') {
                usagePeriod.style.display = 'block';
            } else {
                usagePeriod.style.display = 'none';
            }
        });
    }
}

async function loadGroupsForSplit() {
    try {
        const response = await fetch(API.GROUPS);
        const groups = await response.json();
        
        const select = document.getElementById('group-select');
        select.innerHTML = '<option value="">-- Chọn nhóm --</option>' +
            groups.map(g => `<option value="${g.groupId}">${g.groupName}</option>`).join('');
            
    } catch (error) {
        console.error('Error loading groups:', error);
    }
}

function initAutoSplitForm() {
    const form = document.getElementById('auto-split-form');
    const btnPreview = document.getElementById('btn-preview');
    
    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            await createAndSplit();
        });
    }
    
    if (btnPreview) {
        btnPreview.addEventListener('click', async function() {
            await previewSplit();
        });
    }
}

async function previewSplit() {
    const data = getFormData();
    
    try {
        const response = await fetch(`${API.AUTO_SPLIT}/preview`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        
        const result = await response.json();
        
        const previewCard = document.getElementById('preview-result');
        const previewContent = document.getElementById('preview-content');
        
        previewContent.innerHTML = `
            <div style="margin-bottom: 1rem;">
                <strong>Tổng chi phí:</strong> ${formatCurrency(data.amount)}
            </div>
            <table class="preview-table" style="width: 100%; border-collapse: collapse;">
                <thead>
                    <tr style="background: var(--light);">
                        <th style="padding: 0.75rem; text-align: left;">Người dùng</th>
                        <th style="padding: 0.75rem; text-align: right;">Tỷ lệ</th>
                        <th style="padding: 0.75rem; text-align: right;">Số tiền</th>
                    </tr>
                </thead>
                <tbody>
                    ${result.shares ? result.shares.map(share => `
                        <tr>
                            <td style="padding: 0.75rem;">User #${share.userId}</td>
                            <td style="padding: 0.75rem; text-align: right;">${share.percent}%</td>
                            <td style="padding: 0.75rem; text-align: right; font-weight: 700;">${formatCurrency(share.amountShare)}</td>
                        </tr>
                    `).join('') : '<tr><td colspan="3">Không có dữ liệu</td></tr>'}
                </tbody>
            </table>
        `;
        
        previewCard.style.display = 'block';
        
    } catch (error) {
        console.error('Error previewing split:', error);
        showNotification('Lỗi khi xem trước', 'error');
    }
}

async function createAndSplit() {
    const data = getFormData();
    
    console.log('=== CREATING COST ===');
    console.log('Data:', data);
    
    try {
        const response = await fetch(`${API.AUTO_SPLIT}/create-and-split`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        
        console.log('Response status:', response.status);
        
        if (response.ok) {
            const result = await response.json();
            console.log('Result:', result);
            
            showNotification('Đã tạo và chia chi phí thành công!', 'success');
            document.getElementById('auto-split-form').reset();
            document.getElementById('preview-result').style.display = 'none';
            
            // Reload costs
            setTimeout(() => {
                switchSection('cost-management');
            }, 1000);
        } else {
            const errorText = await response.text();
            console.error('Error response:', errorText);
            showNotification('Lỗi khi tạo chi phí: ' + errorText, 'error');
        }
        
    } catch (error) {
        console.error('Error creating cost:', error);
        showNotification('Lỗi khi tạo chi phí: ' + error.message, 'error');
    }
}

function getFormData() {
    return {
        vehicleId: parseInt(document.getElementById('group-select').value),
        costType: document.getElementById('cost-type').value,
        amount: parseFloat(document.getElementById('amount').value),
        description: document.getElementById('description').value,
        splitMethod: document.getElementById('split-method').value,
        groupId: parseInt(document.getElementById('group-select').value),
        month: parseInt(document.getElementById('month').value),
        year: parseInt(document.getElementById('year').value)
    };
}

// ============ PAYMENT TRACKING ============
async function loadPayments() {
    try {
        // Load all cost shares
        const costs = await fetch(API.COSTS).then(r => r.json());
        const tbody = document.getElementById('payments-tbody');
        
        // Mock payment data
        tbody.innerHTML = `
            <tr>
                <td>User #1</td>
                <td>Chi phí sạc điện</td>
                <td>${formatCurrency(500000)}</td>
                <td>${formatCurrency(500000)}</td>
                <td>E-Wallet</td>
                <td><span class="status-badge paid">Đã thanh toán</span></td>
                <td>
                    <button class="btn btn-secondary" style="padding: 0.5rem 1rem;">
                        <i class="fas fa-check"></i> Xác nhận
                    </button>
                </td>
            </tr>
            <tr>
                <td>User #2</td>
                <td>Chi phí bảo dưỡng</td>
                <td>${formatCurrency(300000)}</td>
                <td>${formatCurrency(0)}</td>
                <td>-</td>
                <td><span class="status-badge pending">Chưa thanh toán</span></td>
                <td>
                    <button class="btn btn-secondary" style="padding: 0.5rem 1rem; background: var(--warning); color: white;">
                        <i class="fas fa-bell"></i> Nhắc nhở
                    </button>
                </td>
            </tr>
        `;
        
    } catch (error) {
        console.error('Error loading payments:', error);
    }
}

// ============ GROUP MANAGEMENT ============
async function loadGroups() {
    try {
        const response = await fetch(API.GROUPS);
        const groups = await response.json();
        
        const grid = document.getElementById('groups-grid');
        grid.innerHTML = groups.map(group => `
            <div class="group-card">
                <h4>${group.groupName}</h4>
                <p>Admin: User #${group.adminId} | Xe ID: ${group.vehicleId}</p>
                <div style="display: flex; gap: 1rem; margin-top: 1rem;">
                    <div style="flex: 1; padding: 0.75rem; background: var(--light); border-radius: 8px; text-align: center;">
                        <div style="font-size: 1.5rem; font-weight: 700; color: var(--primary);">
                            ${group.members ? group.members.length : 0}
                        </div>
                        <div style="font-size: 0.75rem; color: var(--text-light);">Thành viên</div>
                    </div>
                    <div style="flex: 1; padding: 0.75rem; background: var(--light); border-radius: 8px; text-align: center;">
                        <div style="font-size: 1.5rem; font-weight: 700; color: var(--success);">
                            ${group.status === 'ACTIVE' ? 'Hoạt động' : 'Không hoạt động'}
                        </div>
                        <div style="font-size: 0.75rem; color: var(--text-light);">Trạng thái</div>
                    </div>
                </div>
                <div style="display: flex; gap: 0.5rem; margin-top: 1rem;">
                    <button class="btn btn-primary" style="flex: 1;" onclick="viewGroupDetail(${group.groupId})">
                        <i class="fas fa-eye"></i> Xem
                    </button>
                    <button class="btn btn-secondary" style="flex: 1;" onclick="editGroup(${group.groupId})">
                        <i class="fas fa-edit"></i> Sửa
                    </button>
                </div>
            </div>
        `).join('');
        
    } catch (error) {
        console.error('Error loading groups:', error);
    }
}

function viewGroupDetail(groupId) {
    alert('Chi tiết nhóm #' + groupId);
}

function editGroup(groupId) {
    alert('Chỉnh sửa nhóm #' + groupId);
}

// ============ UTILITY FUNCTIONS ============
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
}

function getCostTypeName(type) {
    const types = {
        'ElectricCharge': 'Sạc điện',
        'Maintenance': 'Bảo dưỡng',
        'Insurance': 'Bảo hiểm',
        'Inspection': 'Đăng kiểm',
        'Cleaning': 'Vệ sinh',
        'Other': 'Khác'
    };
    return types[type] || type;
}

function getSplitMethodName(method) {
    const methods = {
        'BY_OWNERSHIP': 'Theo sở hữu',
        'BY_USAGE': 'Theo km',
        'EQUAL': 'Chia đều',
        'CUSTOM': 'Tùy chỉnh',
        'N/A': 'Chưa chia'
    };
    return methods[method] || method;
}

function getStatusClass(status) {
    return status === 'PAID' ? 'paid' : status === 'OVERDUE' ? 'overdue' : 'pending';
}

function getStatusName(status) {
    const statuses = {
        'PENDING': 'Chưa thanh toán',
        'PAID': 'Đã thanh toán',
        'OVERDUE': 'Quá hạn'
    };
    return statuses[status] || status;
}

function showNotification(message, type) {
    // Create toast notification
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i>
        <span>${message}</span>
    `;
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.style.opacity = '1';
        toast.style.transform = 'translateY(0)';
    }, 100);
    
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(20px)';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

