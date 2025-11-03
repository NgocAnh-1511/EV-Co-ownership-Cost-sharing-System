// Admin Dashboard JavaScript

// API Endpoints
const API = {
    GROUPS: '/api/groups',           // GET all, POST create
    GROUP_DETAIL: '/api/groups',     // /api/groups/{id}
    GROUP_MEMBERS: '/api/groups',    // /api/groups/{groupId}/members
    GROUP_VOTES: '/api/groups',      // /api/groups/{groupId}/votes
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
let currentGroupId = null;  // For group management

// Initialize on DOM load
document.addEventListener('DOMContentLoaded', function() {
    console.log('Admin Dashboard initializing...');
    initNavigation();
    initSplitMethodToggle();
    initAutoSplitForm();
    initGroupManagement();
    initPaymentTracking();
    loadOverviewData();
    initCharts();
    console.log('Admin Dashboard initialized');
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
let currentPaymentsData = [];

async function loadPayments(filters = {}) {
    try {
        console.log('Loading payments with filters:', filters);
        
        // Build URL with filters
        let url = '/api/payments/admin/tracking?';
        if (filters.status) url += `status=${filters.status}&`;
        if (filters.startDate) url += `startDate=${filters.startDate}&`;
        if (filters.endDate) url += `endDate=${filters.endDate}&`;
        if (filters.search) url += `search=${encodeURIComponent(filters.search)}&`;
        
        const response = await fetch(url);
        const data = await response.json();
        
        console.log('Payments data:', data);
        
        currentPaymentsData = data.payments || [];
        const stats = data.statistics || { total: 0, totalAmount: 0, paidCount: 0, pendingCount: 0 };
        
        // Update statistics
        document.getElementById('total-payments').textContent = stats.total;
        document.getElementById('paid-count').textContent = stats.paidCount;
        document.getElementById('pending-count').textContent = stats.pendingCount;
        document.getElementById('total-amount').textContent = formatCurrency(stats.totalAmount);
        
        // Render table
        renderPaymentsTable(currentPaymentsData);
        
    } catch (error) {
        console.error('Error loading payments:', error);
        showNotification('Lỗi khi tải danh sách thanh toán', 'error');
    }
}

function renderPaymentsTable(payments) {
    const tbody = document.getElementById('payments-tbody');
    
    if (!payments || payments.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="9" style="text-align: center; padding: 2rem; color: var(--text-light);">
                    <i class="fas fa-inbox"></i><br>
                    Không có dữ liệu thanh toán
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = payments.map(payment => {
        const statusClass = getPaymentStatusClass(payment.status);
        const statusText = getPaymentStatusText(payment.status);
        const paymentDate = payment.paymentDate ? new Date(payment.paymentDate).toLocaleDateString('vi-VN') : '-';
        const costType = payment.costType || '-';
        const method = payment.method || '-';
        const transactionCode = payment.transactionCode || '-';
        
        return `
            <tr>
                <td>${payment.paymentId}</td>
                <td>User #${payment.userId}</td>
                <td>${costType}</td>
                <td style="font-weight: bold; color: var(--primary);">${formatCurrency(payment.amount)}</td>
                <td>${method}</td>
                <td style="font-family: monospace; font-size: 0.85rem;">${transactionCode}</td>
                <td>${paymentDate}</td>
                <td><span class="status-badge ${statusClass}">${statusText}</span></td>
                <td>
                    <div style="display: flex; gap: 0.5rem; flex-wrap: wrap; align-items: center;">
                        <button class="btn btn-sm" style="background: #10B981; color: white; padding: 0.5rem 0.75rem; border-radius: 6px; border: none; cursor: pointer; display: inline-flex; align-items: center; gap: 0.25rem; font-size: 0.875rem; transition: all 0.2s;" onmouseover="this.style.background='#059669'" onmouseout="this.style.background='#10B981'" onclick="openEditPaymentModal(${payment.paymentId})" title="Chỉnh sửa thanh toán">
                            <i class="fas fa-edit"></i>
                            <span>Sửa</span>
                        </button>
                        <button class="btn btn-sm" style="background: var(--info); color: white; padding: 0.5rem 0.75rem; border-radius: 6px; border: none; cursor: pointer; display: inline-flex; align-items: center; gap: 0.25rem; font-size: 0.875rem;" onclick="printPaymentInvoice(${payment.paymentId})" title="In hóa đơn">
                            <i class="fas fa-print"></i>
                        </button>
                        <button class="btn btn-sm" style="background: var(--danger); color: white; padding: 0.5rem 0.75rem; border-radius: 6px; border: none; cursor: pointer; display: inline-flex; align-items: center; gap: 0.25rem; font-size: 0.875rem;" onclick="deletePayment(${payment.paymentId})" title="Xóa thanh toán">
                            <i class="fas fa-trash"></i>
                        </button>
                        ${payment.status === 'PENDING' ? `
                            <button class="btn btn-sm btn-success" onclick="openConfirmPaymentModal(${payment.paymentId})" title="Xác nhận">
                                <i class="fas fa-check"></i>
                            </button>
                            <button class="btn btn-sm" style="background: var(--warning); color: white;" onclick="sendReminder(${payment.paymentId})" title="Nhắc nhở">
                                <i class="fas fa-bell"></i>
                            </button>
                        ` : ''}
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function getPaymentStatusClass(status) {
    const statusMap = {
        'PAID': 'paid',
        'PENDING': 'pending',
        'OVERDUE': 'overdue',
        'CANCELLED': 'cancelled'
    };
    return statusMap[status] || 'pending';
}

function getPaymentStatusText(status) {
    const statusMap = {
        'PAID': 'Đã thanh toán',
        'PENDING': 'Chờ thanh toán',
        'OVERDUE': 'Quá hạn',
        'CANCELLED': 'Đã hủy'
    };
    return statusMap[status] || status;
}

function initPaymentTracking() {
    console.log('Initializing payment tracking...');
    
    // Filter button
    const btnFilter = document.getElementById('btn-filter-payments');
    if (btnFilter) {
        btnFilter.addEventListener('click', applyPaymentFilters);
    }
    
    // Reset button
    const btnReset = document.getElementById('btn-reset-filters');
    if (btnReset) {
        btnReset.addEventListener('click', resetPaymentFilters);
    }
    
    // Export button
    const btnExport = document.getElementById('btn-export-payments');
    if (btnExport) {
        btnExport.addEventListener('click', exportPaymentsToExcel);
    }
    
    // Confirm payment form
    const confirmForm = document.getElementById('confirm-payment-form');
    if (confirmForm) {
        confirmForm.addEventListener('submit', handleConfirmPayment);
    }
    
    // Enter key on search input
    const searchInput = document.getElementById('payment-search');
    if (searchInput) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                applyPaymentFilters();
            }
        });
    }
    
    console.log('Payment tracking initialized');
}

function applyPaymentFilters() {
    const filters = {
        status: document.getElementById('payment-status-filter')?.value || '',
        startDate: document.getElementById('payment-date-from')?.value || '',
        endDate: document.getElementById('payment-date-to')?.value || '',
        search: document.getElementById('payment-search')?.value || ''
    };
    
    loadPayments(filters);
}

function resetPaymentFilters() {
    document.getElementById('payment-status-filter').value = '';
    document.getElementById('payment-date-from').value = '';
    document.getElementById('payment-date-to').value = '';
    document.getElementById('payment-search').value = '';
    
    loadPayments();
}

async function viewPaymentDetails(paymentId) {
    try {
        const response = await fetch(`/api/payments/${paymentId}/details`);
        const details = await response.json();
        
        if (details.error) {
            showNotification('Không tìm thấy thanh toán', 'error');
            return;
        }
        
        const modal = document.getElementById('payment-details-modal');
        const body = document.getElementById('payment-details-body');
        
        const paymentDate = details.paymentDate ? new Date(details.paymentDate).toLocaleString('vi-VN') : '-';
        const costDate = details.cost?.date ? new Date(details.cost.date).toLocaleDateString('vi-VN') : '-';
        
        body.innerHTML = `
            <div style="display: grid; gap: 1.5rem;">
                <div class="info-section">
                    <h4 style="margin-bottom: 1rem; color: var(--primary);">
                        <i class="fas fa-money-bill-wave"></i> Thông tin thanh toán
                    </h4>
                    <div class="info-grid">
                        <div class="info-row">
                            <span class="info-label">Mã thanh toán:</span>
                            <span class="info-value">#${details.paymentId}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Số tiền:</span>
                            <span class="info-value" style="color: var(--primary); font-weight: bold; font-size: 1.2rem;">
                                ${formatCurrency(details.amount)}
                            </span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Phương thức:</span>
                            <span class="info-value">${details.method || '-'}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Mã giao dịch:</span>
                            <span class="info-value" style="font-family: monospace;">${details.transactionCode || '-'}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Trạng thái:</span>
                            <span class="status-badge ${getPaymentStatusClass(details.status)}">
                                ${getPaymentStatusText(details.status)}
                            </span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Ngày thanh toán:</span>
                            <span class="info-value">${paymentDate}</span>
                        </div>
                    </div>
                </div>
                
                ${details.user ? `
                <div class="info-section">
                    <h4 style="margin-bottom: 1rem; color: var(--primary);">
                        <i class="fas fa-user"></i> Thông tin người dùng
                    </h4>
                    <div class="info-grid">
                        <div class="info-row">
                            <span class="info-label">User ID:</span>
                            <span class="info-value">#${details.userId}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Tên:</span>
                            <span class="info-value">${details.user.name || '-'}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Email:</span>
                            <span class="info-value">${details.user.email || '-'}</span>
                        </div>
                    </div>
                </div>
                ` : ''}
                
                ${details.cost ? `
                <div class="info-section">
                    <h4 style="margin-bottom: 1rem; color: var(--primary);">
                        <i class="fas fa-receipt"></i> Thông tin chi phí
                    </h4>
                    <div class="info-grid">
                        <div class="info-row">
                            <span class="info-label">Loại chi phí:</span>
                            <span class="info-value">${details.cost.costType || '-'}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Tổng chi phí:</span>
                            <span class="info-value">${formatCurrency(details.cost.amount || 0)}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Ngày phát sinh:</span>
                            <span class="info-value">${costDate}</span>
                        </div>
                        ${details.cost.description ? `
                        <div class="info-row">
                            <span class="info-label">Mô tả:</span>
                            <span class="info-value">${details.cost.description}</span>
                        </div>
                        ` : ''}
                    </div>
                </div>
                ` : ''}
            </div>
        `;
        
        modal.style.display = 'flex';
        
    } catch (error) {
        console.error('Error loading payment details:', error);
        showNotification('Lỗi khi tải chi tiết thanh toán', 'error');
    }
}

function closePaymentDetailsModal() {
    document.getElementById('payment-details-modal').style.display = 'none';
}

function openConfirmPaymentModal(paymentId) {
    const payment = currentPaymentsData.find(p => p.paymentId === paymentId);
    
    if (!payment) {
        showNotification('Không tìm thấy thanh toán', 'error');
        return;
    }
    
    document.getElementById('confirm-payment-id').value = paymentId;
    document.getElementById('confirm-user-id').textContent = `#${payment.userId}`;
    document.getElementById('confirm-amount').textContent = formatCurrency(payment.amount);
    document.getElementById('confirm-transaction-code').textContent = payment.transactionCode || '-';
    document.getElementById('confirm-note').value = '';
    
    document.getElementById('confirm-payment-modal').style.display = 'flex';
}

function closeConfirmPaymentModal() {
    document.getElementById('confirm-payment-modal').style.display = 'none';
}

async function handleConfirmPayment(event) {
    event.preventDefault();
    
    const paymentId = document.getElementById('confirm-payment-id').value;
    const note = document.getElementById('confirm-note').value;
    
    try {
        const response = await fetch(`/api/payments/${paymentId}/admin-confirm`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ note })
        });
        
        const result = await response.json();
        
        if (result.success) {
            showNotification('Đã xác nhận thanh toán thành công!', 'success');
            closeConfirmPaymentModal();
            applyPaymentFilters(); // Reload with current filters
        } else {
            showNotification(result.message || 'Lỗi khi xác nhận thanh toán', 'error');
        }
        
    } catch (error) {
        console.error('Error confirming payment:', error);
        showNotification('Lỗi khi xác nhận thanh toán', 'error');
    }
}

async function sendReminder(paymentId) {
    if (!confirm('Bạn có chắc chắn muốn gửi nhắc nhở thanh toán?')) {
        return;
    }
    
    try {
        const response = await fetch(`/api/payments/${paymentId}/remind`, {
            method: 'POST'
        });
        
        const result = await response.json();
        
        if (result.success) {
            showNotification('Đã gửi nhắc nhở thanh toán!', 'success');
        } else {
            showNotification(result.message || 'Lỗi khi gửi nhắc nhở', 'error');
        }
        
    } catch (error) {
        console.error('Error sending reminder:', error);
        showNotification('Lỗi khi gửi nhắc nhở', 'error');
    }
}

function exportPaymentsToExcel() {
    if (currentPaymentsData.length === 0) {
        showNotification('Không có dữ liệu để xuất', 'warning');
        return;
    }
    
    try {
        // Create CSV content
        let csv = 'ID,User ID,Chi phí,Số tiền,Phương thức,Mã giao dịch,Ngày,Trạng thái\n';
        
        currentPaymentsData.forEach(payment => {
            const date = payment.paymentDate ? new Date(payment.paymentDate).toLocaleDateString('vi-VN') : '-';
            csv += `${payment.paymentId},`;
            csv += `${payment.userId},`;
            csv += `"${payment.costType || '-'}",`;
            csv += `${payment.amount},`;
            csv += `${payment.method || '-'},`;
            csv += `"${payment.transactionCode || '-'}",`;
            csv += `${date},`;
            csv += `${getPaymentStatusText(payment.status)}\n`;
        });
        
        // Create download link
        const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);
        
        link.setAttribute('href', url);
        link.setAttribute('download', `payments_${new Date().toISOString().split('T')[0]}.csv`);
        link.style.visibility = 'hidden';
        
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        
        showNotification('Đã xuất file Excel thành công!', 'success');
        
    } catch (error) {
        console.error('Error exporting to Excel:', error);
        showNotification('Lỗi khi xuất file Excel', 'error');
    }
}

// ============ GROUP MANAGEMENT ============
function initGroupManagement() {
    // Create Group button
    const btnCreateGroup = document.getElementById('btn-create-group');
    if (btnCreateGroup) {
        btnCreateGroup.addEventListener('click', openCreateGroupModal);
    }

    // Group Form submit
    const groupForm = document.getElementById('group-form');
    if (groupForm) {
        groupForm.addEventListener('submit', handleGroupFormSubmit);
    }

    // Member Form submit
    const memberForm = document.getElementById('member-form');
    if (memberForm) {
        memberForm.addEventListener('submit', handleMemberFormSubmit);
    }
}

async function loadGroups() {
    try {
        const response = await fetch(API.GROUPS);
        const groups = await response.json();
        
        const grid = document.getElementById('groups-grid');
        grid.innerHTML = groups.map(group => `
            <div class="group-card">
                <h4>${group.groupName}</h4>
                <p>Admin: User #${group.adminId} | Xe ID: ${group.vehicleId || 'N/A'}</p>
                <div style="display: flex; gap: 1rem; margin-top: 1rem;">
                    <div style="flex: 1; padding: 0.75rem; background: var(--light); border-radius: 8px; text-align: center;">
                        <div style="font-size: 1.5rem; font-weight: 700; color: var(--primary);">
                            ${group.memberCount || 0}
                        </div>
                        <div style="font-size: 0.75rem; color: var(--text-light);">Thành viên</div>
                    </div>
                    <div style="flex: 1; padding: 0.75rem; background: var(--light); border-radius: 8px; text-align: center;">
                        <div style="font-size: 1.5rem; font-weight: 700; color: var(--success);">
                            ${group.status === 'Active' ? 'Hoạt động' : 'Không hoạt động'}
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
                    <button class="btn" style="flex: 1; background: var(--danger); color: white;" onclick="deleteGroup(${group.groupId}, '${group.groupName}')">
                        <i class="fas fa-trash"></i> Xóa
                    </button>
                </div>
            </div>
        `).join('');
        
    } catch (error) {
        console.error('Error loading groups:', error);
        showNotification('Lỗi khi tải danh sách nhóm', 'error');
    }
}

// ========== CREATE GROUP ==========
function openCreateGroupModal() {
    document.getElementById('group-modal-title').textContent = 'Tạo nhóm mới';
    document.getElementById('group-form').reset();
    document.getElementById('group-id').value = '';
    document.getElementById('group-status').value = 'Active';
    openModal('group-modal');
}

async function handleGroupFormSubmit(e) {
    e.preventDefault();
    
    const groupId = document.getElementById('group-id').value;
    const groupData = {
        groupName: document.getElementById('group-name').value,
        adminId: parseInt(document.getElementById('group-admin').value),
        vehicleId: document.getElementById('group-vehicle').value ? parseInt(document.getElementById('group-vehicle').value) : null,
        status: document.getElementById('group-status').value
    };

    try {
        let response;
        if (groupId) {
            // Update existing group
            response = await fetch(`${API.GROUP_DETAIL}/${groupId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(groupData)
            });
        } else {
            // Create new group
            response = await fetch(API.GROUPS, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(groupData)
            });
        }

        if (response.ok) {
            showNotification(groupId ? 'Cập nhật nhóm thành công!' : 'Tạo nhóm thành công!', 'success');
            closeGroupModal();
            loadGroups();
        } else {
            const error = await response.text();
            showNotification('Lỗi: ' + error, 'error');
        }
    } catch (error) {
        console.error('Error saving group:', error);
        showNotification('Lỗi khi lưu nhóm', 'error');
    }
}

// ========== VIEW GROUP DETAIL ==========
async function viewGroupDetail(groupId) {
    try {
        // Fetch group info
        const groupResponse = await fetch(`${API.GROUP_DETAIL}/${groupId}`);
        const group = await groupResponse.json();

        // Fetch members
        const membersResponse = await fetch(`${API.GROUP_MEMBERS}/${groupId}/members`);
        const members = await membersResponse.json();

        // Fetch votes
        const votesResponse = await fetch(`${API.GROUP_VOTES}/${groupId}/votes`);
        const votes = await votesResponse.json();

        // Display in modal
        document.getElementById('group-detail-title').textContent = `Chi tiết nhóm: ${group.groupName}`;
        
        const content = document.getElementById('group-detail-content');
        content.innerHTML = `
            <div style="margin-bottom: 2rem;">
                <h4 style="margin-bottom: 1rem; color: var(--primary);">
                    <i class="fas fa-info-circle"></i> Thông tin nhóm
                </h4>
                <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 1rem;">
                    <div class="info-item">
                        <strong>Tên nhóm:</strong> ${group.groupName}
                    </div>
                    <div class="info-item">
                        <strong>Admin ID:</strong> ${group.adminId}
                    </div>
                    <div class="info-item">
                        <strong>Vehicle ID:</strong> ${group.vehicleId || 'N/A'}
                    </div>
                    <div class="info-item">
                        <strong>Trạng thái:</strong> 
                        <span class="badge ${group.status === 'Active' ? 'badge-success' : 'badge-secondary'}">
                            ${group.status === 'Active' ? 'Hoạt động' : 'Không hoạt động'}
                        </span>
                    </div>
                    <div class="info-item">
                        <strong>Ngày tạo:</strong> ${formatDate(group.createdAt)}
                    </div>
                </div>
            </div>

            <div style="margin-bottom: 2rem;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                    <h4 style="color: var(--primary);">
                        <i class="fas fa-users"></i> Thành viên (${members.length})
                    </h4>
                    <button class="btn btn-primary btn-sm" onclick="openAddMemberModal(${groupId})">
                        <i class="fas fa-plus"></i> Thêm thành viên
                    </button>
                </div>
                ${members.length > 0 ? `
                    <table class="table">
                        <thead>
                            <tr>
                                <th>User ID</th>
                                <th>Vai trò</th>
                                <th>Sở hữu (%)</th>
                                <th>Ngày tham gia</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${members.map(member => `
                                <tr>
                                    <td>${member.userId}</td>
                                    <td>
                                        <span class="badge ${member.role === 'Admin' ? 'badge-primary' : 'badge-secondary'}">
                                            ${member.role === 'Admin' ? 'Quản trị' : 'Thành viên'}
                                        </span>
                                    </td>
                                    <td>${member.ownershipPercent || 0}%</td>
                                    <td>${formatDate(member.joinedAt)}</td>
                                    <td>
                                        <button class="btn btn-sm btn-secondary" onclick="editMember(${groupId}, ${member.memberId})">
                                            <i class="fas fa-edit"></i>
                                        </button>
                                        <button class="btn btn-sm" style="background: var(--danger); color: white;" onclick="deleteMember(${groupId}, ${member.memberId})">
                                            <i class="fas fa-trash"></i>
                                        </button>
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                ` : '<p style="color: var(--text-light); text-align: center; padding: 2rem;">Chưa có thành viên nào</p>'}
            </div>

            <div>
                <h4 style="margin-bottom: 1rem; color: var(--primary);">
                    <i class="fas fa-vote-yea"></i> Lịch sử bỏ phiếu (${votes.length})
                </h4>
                ${votes.length > 0 ? `
                    <div style="display: grid; gap: 1rem;">
                        ${votes.map(vote => `
                            <div class="vote-card" style="padding: 1rem; background: var(--light); border-radius: 8px;">
                                <h5 style="margin-bottom: 0.5rem;">${vote.topic}</h5>
                                <div style="display: flex; gap: 1rem; margin-top: 0.5rem;">
                                    <div><strong>Lựa chọn A:</strong> ${vote.optionA}</div>
                                    <div><strong>Lựa chọn B:</strong> ${vote.optionB}</div>
                                </div>
                                <div style="margin-top: 0.5rem;">
                                    <strong>Kết quả:</strong> 
                                    <span class="badge badge-success">${vote.finalResult || 'Đang bỏ phiếu'}</span>
                                    <span style="margin-left: 1rem; color: var(--text-light);">
                                        Tổng số phiếu: ${vote.totalVotes}
                                    </span>
                                </div>
                                <div style="margin-top: 0.5rem; color: var(--text-light); font-size: 0.875rem;">
                                    ${formatDate(vote.createdAt)}
                                </div>
                            </div>
                        `).join('')}
                    </div>
                ` : '<p style="color: var(--text-light); text-align: center; padding: 2rem;">Chưa có cuộc bỏ phiếu nào</p>'}
            </div>
        `;

        openModal('group-detail-modal');
        
    } catch (error) {
        console.error('Error loading group detail:', error);
        showNotification('Lỗi khi tải chi tiết nhóm', 'error');
    }
}

// ========== EDIT GROUP ==========
async function editGroup(groupId) {
    try {
        const response = await fetch(`${API.GROUP_DETAIL}/${groupId}`);
        const group = await response.json();

        document.getElementById('group-modal-title').textContent = 'Chỉnh sửa nhóm';
        document.getElementById('group-id').value = group.groupId;
        document.getElementById('group-name').value = group.groupName;
        document.getElementById('group-admin').value = group.adminId;
        document.getElementById('group-vehicle').value = group.vehicleId || '';
        document.getElementById('group-status').value = group.status;

        openModal('group-modal');
    } catch (error) {
        console.error('Error loading group for edit:', error);
        showNotification('Lỗi khi tải thông tin nhóm', 'error');
    }
}

// ========== DELETE GROUP ==========
async function deleteGroup(groupId, groupName) {
    if (!confirm(`Bạn có chắc chắn muốn xóa nhóm "${groupName}"?\n\nLưu ý: Tất cả thành viên và dữ liệu liên quan sẽ bị xóa!`)) {
        return;
    }

    try {
        const response = await fetch(`${API.GROUP_DETAIL}/${groupId}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            showNotification('Xóa nhóm thành công!', 'success');
            loadGroups();
        } else {
            showNotification('Lỗi khi xóa nhóm', 'error');
        }
    } catch (error) {
        console.error('Error deleting group:', error);
        showNotification('Lỗi khi xóa nhóm', 'error');
    }
}

// ========== MEMBER MANAGEMENT ==========
function openAddMemberModal(groupId) {
    currentGroupId = groupId;
    document.getElementById('member-modal-title').textContent = 'Thêm thành viên';
    document.getElementById('member-form').reset();
    document.getElementById('member-group-id').value = groupId;
    document.getElementById('member-id').value = '';
    document.getElementById('member-role').value = 'Member';
    document.getElementById('member-ownership').value = '0';
    
    closeGroupDetailModal();
    openModal('member-modal');
}

async function editMember(groupId, memberId) {
    try {
        const response = await fetch(`${API.GROUP_MEMBERS}/${groupId}/members`);
        const members = await response.json();
        const member = members.find(m => m.memberId === memberId);

        if (member) {
            currentGroupId = groupId;
            document.getElementById('member-modal-title').textContent = 'Chỉnh sửa thành viên';
            document.getElementById('member-group-id').value = groupId;
            document.getElementById('member-id').value = member.memberId;
            document.getElementById('member-user-id').value = member.userId;
            document.getElementById('member-role').value = member.role;
            document.getElementById('member-ownership').value = member.ownershipPercent || 0;

            closeGroupDetailModal();
            openModal('member-modal');
        }
    } catch (error) {
        console.error('Error loading member for edit:', error);
        showNotification('Lỗi khi tải thông tin thành viên', 'error');
    }
}

async function handleMemberFormSubmit(e) {
    e.preventDefault();
    
    const groupId = document.getElementById('member-group-id').value;
    const memberId = document.getElementById('member-id').value;
    
    const memberData = {
        userId: parseInt(document.getElementById('member-user-id').value),
        role: document.getElementById('member-role').value,
        ownershipPercent: parseFloat(document.getElementById('member-ownership').value) || 0
    };

    try {
        let response;
        if (memberId) {
            // Update member - need to implement PUT endpoint
            response = await fetch(`${API.GROUP_MEMBERS}/${groupId}/members/${memberId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(memberData)
            });
        } else {
            // Add new member
            response = await fetch(`${API.GROUP_MEMBERS}/${groupId}/members`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(memberData)
            });
        }

        if (response.ok) {
            showNotification(memberId ? 'Cập nhật thành viên thành công!' : 'Thêm thành viên thành công!', 'success');
            closeMemberModal();
            viewGroupDetail(groupId);
        } else {
            const error = await response.text();
            showNotification('Lỗi: ' + error, 'error');
        }
    } catch (error) {
        console.error('Error saving member:', error);
        showNotification('Lỗi khi lưu thành viên', 'error');
    }
}

async function deleteMember(groupId, memberId) {
    if (!confirm('Bạn có chắc chắn muốn xóa thành viên này khỏi nhóm?')) {
        return;
    }

    try {
        const response = await fetch(`${API.GROUP_MEMBERS}/${groupId}/members/${memberId}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            showNotification('Xóa thành viên thành công!', 'success');
            viewGroupDetail(groupId);
        } else {
            showNotification('Lỗi khi xóa thành viên', 'error');
        }
    } catch (error) {
        console.error('Error deleting member:', error);
        showNotification('Lỗi khi xóa thành viên', 'error');
    }
}

// ========== MODAL CONTROLS ==========
function openModal(modalId) {
    document.getElementById('modal-overlay').classList.add('active');
    document.getElementById(modalId).classList.add('active');
}

function closeModal(modalId) {
    document.getElementById('modal-overlay').classList.remove('active');
    document.getElementById(modalId).classList.remove('active');
}

function closeGroupModal() {
    closeModal('group-modal');
}

function closeGroupDetailModal() {
    closeModal('group-detail-modal');
}

function closeMemberModal() {
    closeModal('member-modal');
}

// Close modal when clicking overlay
document.addEventListener('DOMContentLoaded', function() {
    const overlay = document.getElementById('modal-overlay');
    if (overlay) {
        overlay.addEventListener('click', function() {
            document.querySelectorAll('.modal.active').forEach(modal => {
                modal.classList.remove('active');
            });
            this.classList.remove('active');
        });
    }
});

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

// ============================================
// EDIT PAYMENT FUNCTIONS
// ============================================

function openEditPaymentModal(paymentId) {
    // Fetch payment details first
    fetch(`/api/payments/${paymentId}/details`)
        .then(response => response.json())
        .then(payment => {
            document.getElementById('edit-payment-id').value = payment.paymentId;
            document.getElementById('edit-user-id').value = payment.userId;
            document.getElementById('edit-cost-id').value = payment.costId;
            document.getElementById('edit-amount').value = payment.amount;
            document.getElementById('edit-method').value = payment.method || '';
            document.getElementById('edit-transaction-code').value = payment.transactionCode || '';
            document.getElementById('edit-status').value = payment.status;
            
            // Format date for input
            if (payment.paymentDate) {
                const date = new Date(payment.paymentDate);
                document.getElementById('edit-payment-date').value = date.toISOString().split('T')[0];
            }
            
            // Show modal
            document.getElementById('edit-payment-modal').classList.add('active');
            document.getElementById('modal-overlay').classList.add('active');
        })
        .catch(error => {
            console.error('Error loading payment details:', error);
            showNotification('Lỗi khi tải thông tin thanh toán', 'error');
        });
}

function closeEditPaymentModal() {
    document.getElementById('edit-payment-modal').classList.remove('active');
    document.getElementById('modal-overlay').classList.remove('active');
    document.getElementById('edit-payment-form').reset();
}

// Handle edit payment form submission
document.addEventListener('DOMContentLoaded', function() {
    const editForm = document.getElementById('edit-payment-form');
    if (editForm) {
        editForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const paymentId = document.getElementById('edit-payment-id').value;
            const paymentData = {
                userId: parseInt(document.getElementById('edit-user-id').value),
                costId: parseInt(document.getElementById('edit-cost-id').value),
                amount: parseFloat(document.getElementById('edit-amount').value),
                method: document.getElementById('edit-method').value,
                transactionCode: document.getElementById('edit-transaction-code').value,
                paymentDate: document.getElementById('edit-payment-date').value || null,
                status: document.getElementById('edit-status').value
            };
            
            try {
                const response = await fetch(`/api/payments/${paymentId}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(paymentData)
                });
                
                if (response.ok) {
                    showNotification('Cập nhật thanh toán thành công!', 'success');
                    closeEditPaymentModal();
                    loadPayments(); // Reload the table
                } else {
                    const error = await response.text();
                    showNotification('Lỗi: ' + error, 'error');
                }
            } catch (error) {
                console.error('Error updating payment:', error);
                showNotification('Lỗi khi cập nhật thanh toán', 'error');
            }
        });
    }
});

// ============================================
// DELETE PAYMENT FUNCTIONS
// ============================================

function deletePayment(paymentId) {
    if (!confirm('Bạn có chắc chắn muốn xóa thanh toán này?\n\nThao tác này không thể hoàn tác!')) {
        return;
    }
    
    fetch(`/api/payments/${paymentId}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (response.ok) {
            showNotification('Xóa thanh toán thành công!', 'success');
            loadPayments(); // Reload the table
        } else {
            return response.text().then(error => {
                throw new Error(error);
            });
        }
    })
    .catch(error => {
        console.error('Error deleting payment:', error);
        showNotification('Lỗi khi xóa thanh toán: ' + error.message, 'error');
    });
}

// ============================================
// PRINT INVOICE FUNCTIONS
// ============================================

function printPaymentInvoice(paymentId) {
    // Fetch payment details
    fetch(`/api/payments/${paymentId}/details`)
        .then(response => response.json())
        .then(payment => {
            // Generate invoice HTML
            const invoiceHTML = generateInvoiceHTML(payment);
            document.getElementById('invoice-content').innerHTML = invoiceHTML;
            
            // Show modal
            document.getElementById('invoice-modal').classList.add('active');
            document.getElementById('modal-overlay').classList.add('active');
        })
        .catch(error => {
            console.error('Error loading payment for invoice:', error);
            showNotification('Lỗi khi tải thông tin thanh toán', 'error');
        });
}

function generateInvoiceHTML(payment) {
    const invoiceDate = new Date().toLocaleDateString('vi-VN');
    const paymentDate = payment.paymentDate ? new Date(payment.paymentDate).toLocaleDateString('vi-VN') : 'Chưa thanh toán';
    const statusText = getPaymentStatusText(payment.status);
    
    return `
        <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 800px; margin: 0 auto;">
            <!-- Header -->
            <div style="text-align: center; border-bottom: 3px solid #2196F3; padding-bottom: 1.5rem; margin-bottom: 2rem;">
                <h1 style="color: #2196F3; margin: 0; font-size: 2rem;">HÓA ĐƠN THANH TOÁN</h1>
                <p style="color: #666; margin: 0.5rem 0 0 0; font-size: 0.95rem;">Hệ thống quản lý chi phí xe điện</p>
            </div>
            
            <!-- Invoice Info -->
            <div style="display: flex; justify-content: space-between; margin-bottom: 2rem;">
                <div>
                    <p style="margin: 0.3rem 0; color: #333;"><strong>Số hóa đơn:</strong> #${payment.paymentId}</p>
                    <p style="margin: 0.3rem 0; color: #333;"><strong>Ngày lập:</strong> ${invoiceDate}</p>
                    <p style="margin: 0.3rem 0; color: #333;"><strong>Trạng thái:</strong> <span style="color: ${payment.status === 'PAID' ? '#4CAF50' : '#FF9800'}; font-weight: bold;">${statusText}</span></p>
                </div>
                <div style="text-align: right;">
                    <p style="margin: 0.3rem 0; color: #333;"><strong>User ID:</strong> #${payment.userId}</p>
                    <p style="margin: 0.3rem 0; color: #333;"><strong>Mã GD:</strong> ${payment.transactionCode || 'N/A'}</p>
                    <p style="margin: 0.3rem 0; color: #333;"><strong>Ngày TT:</strong> ${paymentDate}</p>
                </div>
            </div>
            
            <!-- Payment Details -->
            <div style="background: #f5f5f5; padding: 1.5rem; border-radius: 8px; margin-bottom: 2rem;">
                <h3 style="margin: 0 0 1rem 0; color: #333; font-size: 1.2rem;">Chi tiết thanh toán</h3>
                <table style="width: 100%; border-collapse: collapse;">
                    <thead>
                        <tr style="background: #e0e0e0;">
                            <th style="padding: 0.75rem; text-align: left; border: 1px solid #ccc;">Mô tả</th>
                            <th style="padding: 0.75rem; text-align: right; border: 1px solid #ccc;">Số tiền</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td style="padding: 0.75rem; border: 1px solid #ccc;">
                                <strong>Chi phí ID:</strong> #${payment.costId}<br>
                                <span style="color: #666; font-size: 0.9rem;">Phương thức: ${payment.method || 'Chưa xác định'}</span>
                            </td>
                            <td style="padding: 0.75rem; text-align: right; border: 1px solid #ccc; font-size: 1.1rem;">
                                <strong>${formatCurrency(payment.amount)}</strong>
                            </td>
                        </tr>
                    </tbody>
                    <tfoot>
                        <tr style="background: #2196F3; color: white;">
                            <td style="padding: 1rem; border: 1px solid #1976D2; font-size: 1.2rem;"><strong>TỔNG CỘNG</strong></td>
                            <td style="padding: 1rem; text-align: right; border: 1px solid #1976D2; font-size: 1.3rem;">
                                <strong>${formatCurrency(payment.amount)}</strong>
                            </td>
                        </tr>
                    </tfoot>
                </table>
            </div>
            
            <!-- Notes -->
            <div style="margin-bottom: 2rem; padding: 1rem; background: #fff3cd; border-left: 4px solid #ffc107; border-radius: 4px;">
                <p style="margin: 0; color: #856404; font-size: 0.9rem;">
                    <strong>Lưu ý:</strong> Vui lòng giữ hóa đơn này để đối chiếu. Mọi thắc mắc xin liên hệ bộ phận hỗ trợ.
                </p>
            </div>
            
            <!-- Footer -->
            <div style="text-align: center; padding-top: 1.5rem; border-top: 2px solid #e0e0e0; color: #666; font-size: 0.85rem;">
                <p style="margin: 0.3rem 0;">Cảm ơn bạn đã sử dụng dịch vụ!</p>
                <p style="margin: 0.3rem 0;">Hệ thống quản lý chi phí xe điện - EV Co-ownership System</p>
                <p style="margin: 0.3rem 0;">Email: support@evsharing.com | Hotline: 1900-xxxx</p>
            </div>
        </div>
    `;
}

function closeInvoiceModal() {
    document.getElementById('invoice-modal').classList.remove('active');
    document.getElementById('modal-overlay').classList.remove('active');
}

function printInvoice() {
    const invoiceContent = document.getElementById('invoice-content').innerHTML;
    const printWindow = window.open('', '_blank');
    printWindow.document.write(`
        <!DOCTYPE html>
        <html>
        <head>
            <title>Hóa đơn thanh toán</title>
            <style>
                body { margin: 0; padding: 20px; }
                @media print {
                    body { margin: 0; }
                }
            </style>
        </head>
        <body>
            ${invoiceContent}
            <script>
                window.onload = function() {
                    window.print();
                    setTimeout(function() { window.close(); }, 100);
                }
            </script>
        </body>
        </html>
    `);
    printWindow.document.close();
}

function downloadInvoicePDF() {
    showNotification('Tính năng tải PDF đang được phát triển...', 'info');
    // TODO: Implement PDF download using jsPDF or similar library
    // For now, users can use the print function and "Save as PDF"
}

