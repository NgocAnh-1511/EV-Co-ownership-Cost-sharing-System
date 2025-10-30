// User Dashboard JavaScript

// API Endpoints
const API = {
    GROUPS: '/api/groups',
    COSTS: '/api/costs',
    USAGE: '/api/usage-tracking',
    PAYMENTS: '/api/payments',
    COST_SHARES: '/api/cost-shares'
};

// Current User (mock - in real app, get from session)
const CURRENT_USER_ID = 1;

// Global State
let currentPage = 'home';

// Initialize on DOM load
document.addEventListener('DOMContentLoaded', function() {
    initNavigation();
    initUsageForm();
    initPaymentMethods();
    loadHomePage();
});

// ============ NAVIGATION ============
function initNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    
    navItems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            const page = this.getAttribute('data-page');
            switchPage(page);
        });
    });
    
    // Handle view-all links
    document.querySelectorAll('.view-all').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const page = this.getAttribute('data-page');
            if (page) switchPage(page);
        });
    });
}

function switchPage(page) {
    // Update nav
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
    });
    document.querySelector(`[data-page="${page}"]`)?.classList.add('active');
    
    // Update page content
    document.querySelectorAll('.page').forEach(p => {
        p.classList.remove('active');
    });
    document.getElementById(`${page}-page`)?.classList.add('active');
    
    currentPage = page;
    
    // Load page data
    switch(page) {
        case 'home':
            loadHomePage();
            break;
        case 'costs':
            loadCostsPage();
            break;
        case 'usage':
            loadUsagePage();
            break;
        case 'payments':
            loadPaymentsPage();
            break;
    }
}

// ============ HOME PAGE ============
async function loadHomePage() {
    try {
        // Load quick stats
        await loadQuickStats();
        
        // Load my groups
        await loadMyGroups();
        
        // Load recent costs
        await loadRecentCosts();
        
    } catch (error) {
        console.error('Error loading home page:', error);
    }
}

async function loadQuickStats() {
    try {
        // Mock data - replace with actual API calls
        document.getElementById('my-pending').textContent = formatCurrency(450000);
        document.getElementById('my-paid').textContent = formatCurrency(1200000);
        document.getElementById('my-km').textContent = '350 km';
        document.getElementById('my-ownership').textContent = '33%';
        
    } catch (error) {
        console.error('Error loading stats:', error);
    }
}

async function loadMyGroups() {
    try {
        const response = await fetch(API.GROUPS);
        const groups = await response.json();
        
        const container = document.getElementById('my-groups-list');
        container.innerHTML = groups.map(group => `
            <div class="group-item">
                <h3>${group.groupName}</h3>
                <p>Quản lý bởi: User #${group.adminId}</p>
                <div class="group-stats">
                    <div class="group-stat">
                        <i class="fas fa-users"></i>
                        <span>${group.members ? group.members.length : 0} thành viên</span>
                    </div>
                    <div class="group-stat">
                        <i class="fas fa-car"></i>
                        <span>Xe #${group.vehicleId}</span>
                    </div>
                </div>
            </div>
        `).join('');
        
    } catch (error) {
        console.error('Error loading groups:', error);
        document.getElementById('my-groups-list').innerHTML = '<p style="text-align: center; color: var(--text-light);">Không có dữ liệu</p>';
    }
}

async function loadRecentCosts() {
    try {
        const response = await fetch(API.COSTS);
        const costs = await response.json();
        
        const recent = costs.slice(0, 5);
        const timeline = document.getElementById('recent-costs-timeline');
        
        timeline.innerHTML = recent.map(cost => `
            <div class="timeline-item">
                <div class="timeline-content">
                    <div class="timeline-header">
                        <div class="timeline-title">${getCostTypeName(cost.costType)}</div>
                        <div class="timeline-amount">${formatCurrency(cost.amount)}</div>
                    </div>
                    <div class="timeline-meta">
                        <i class="fas fa-calendar"></i> ${formatDate(cost.createdAt)}
                    </div>
                </div>
            </div>
        `).join('');
        
    } catch (error) {
        console.error('Error loading recent costs:', error);
    }
}

// ============ COSTS PAGE ============
async function loadCostsPage() {
    try {
        const response = await fetch(API.COSTS);
        const costs = await response.json();
        
        // For each cost, get the share amount for current user
        const grid = document.getElementById('user-costs-grid');
        
        grid.innerHTML = costs.map(cost => {
            // Mock calculation - replace with actual share data
            const myShare = cost.amount / 3; // Assume equal split for now
            const isPaid = Math.random() > 0.5;
            
            return `
                <div class="cost-card">
                    <div class="cost-header">
                        <div class="cost-type">${getCostTypeName(cost.costType)}</div>
                        <div class="cost-status ${isPaid ? 'paid' : 'pending'}">
                            ${isPaid ? 'Đã thanh toán' : 'Chưa thanh toán'}
                        </div>
                    </div>
                    <div class="cost-amount">${formatCurrency(myShare)}</div>
                    <div class="cost-details">
                        ${cost.description || 'Không có mô tả'}
                    </div>
                    <div class="cost-footer">
                        <div class="cost-date">
                            <i class="fas fa-calendar"></i> ${formatDate(cost.createdAt)}
                        </div>
                        ${!isPaid ? `
                            <button class="btn btn-success" style="padding: 0.5rem 1rem;" onclick="payCost(${cost.costId})">
                                <i class="fas fa-credit-card"></i> Thanh toán
                            </button>
                        ` : ''}
                    </div>
                </div>
            `;
        }).join('');
        
    } catch (error) {
        console.error('Error loading costs:', error);
        document.getElementById('user-costs-grid').innerHTML = '<p style="text-align: center; color: var(--text-light);">Không có dữ liệu</p>';
    }
}

function payCost(costId) {
    // Switch to payments page
    switchPage('payments');
    showToast('Chuyển đến trang thanh toán', 'success');
}

// ============ USAGE PAGE ============
async function loadUsagePage() {
    try {
        // Load groups for selection
        const response = await fetch(API.GROUPS);
        const groups = await response.json();
        
        const select = document.getElementById('usage-group');
        select.innerHTML = '<option value="">-- Chọn nhóm --</option>' +
            groups.map(g => `<option value="${g.groupId}">${g.groupName}</option>`).join('');
        
        // Load usage history
        await loadUsageHistory();
        
    } catch (error) {
        console.error('Error loading usage page:', error);
    }
}

function initUsageForm() {
    const form = document.getElementById('usage-form');
    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            await saveUsage();
        });
    }
    
    // Set current month/year
    const now = new Date();
    const monthSelect = document.getElementById('usage-month');
    const yearInput = document.getElementById('usage-year');
    if (monthSelect) monthSelect.value = now.getMonth() + 1;
    if (yearInput) yearInput.value = now.getFullYear();
}

async function saveUsage() {
    const data = {
        groupId: parseInt(document.getElementById('usage-group').value),
        userId: CURRENT_USER_ID,
        month: parseInt(document.getElementById('usage-month').value),
        year: parseInt(document.getElementById('usage-year').value),
        kmDriven: parseFloat(document.getElementById('km-driven').value)
    };
    
    try {
        const response = await fetch(API.USAGE, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        
        if (response.ok) {
            showToast('Đã lưu thông tin sử dụng!', 'success');
            document.getElementById('usage-form').reset();
            
            // Set back to current month/year
            const now = new Date();
            document.getElementById('usage-month').value = now.getMonth() + 1;
            document.getElementById('usage-year').value = now.getFullYear();
            
            await loadUsageHistory();
        } else {
            showToast('Lỗi khi lưu dữ liệu', 'error');
        }
        
    } catch (error) {
        console.error('Error saving usage:', error);
        showToast('Lỗi khi lưu dữ liệu', 'error');
    }
}

async function loadUsageHistory() {
    try {
        const response = await fetch(`${API.USAGE}/user/${CURRENT_USER_ID}/history`);
        const history = await response.json();
        
        const container = document.getElementById('usage-history-list');
        
        if (history && history.length > 0) {
            container.innerHTML = history.map(item => `
                <div class="usage-item">
                    <div class="usage-item-left">
                        <div class="usage-period">Tháng ${item.month}/${item.year}</div>
                        <div class="usage-note">Nhóm #${item.groupId}</div>
                    </div>
                    <div class="usage-item-right">
                        <div class="usage-km">${item.kmDriven} km</div>
                        <div class="usage-percent">${item.percentage || 0}%</div>
                    </div>
                </div>
            `).join('');
        } else {
            container.innerHTML = '<p style="text-align: center; color: var(--text-light);">Chưa có dữ liệu</p>';
        }
        
    } catch (error) {
        console.error('Error loading usage history:', error);
        document.getElementById('usage-history-list').innerHTML = '<p style="text-align: center; color: var(--text-light);">Không thể tải dữ liệu</p>';
    }
}

// ============ PAYMENTS PAGE ============
async function loadPaymentsPage() {
    try {
        await loadPendingPayments();
        await loadPaymentHistory();
    } catch (error) {
        console.error('Error loading payments page:', error);
    }
}

function initPaymentMethods() {
    const methods = document.querySelectorAll('.method-card');
    methods.forEach(method => {
        method.addEventListener('click', function() {
            methods.forEach(m => m.classList.remove('active'));
            this.classList.add('active');
        });
    });
}

async function loadPendingPayments() {
    const container = document.getElementById('pending-payments-list');
    
    try {
        // Load pending payments from API
        const response = await fetch(`${API.PAYMENTS}/user/${CURRENT_USER_ID}/pending`);
        const pendingPayments = await response.json();
        
        if (pendingPayments && pendingPayments.length > 0) {
            container.innerHTML = pendingPayments.map(payment => {
                // Get cost description or use default
                const description = payment.description || `Thanh toán #${payment.paymentId}`;
                const paymentDate = payment.paymentDate || new Date().toISOString();
                
                return `
                    <div class="payment-item">
                        <div class="payment-item-left">
                            <h4>${description}</h4>
                            <p><i class="fas fa-calendar"></i> ${formatDate(paymentDate)}</p>
                            <small style="color: var(--text-light);">Mã giao dịch: ${payment.transactionCode || 'N/A'}</small>
                        </div>
                        <div class="payment-item-right">
                            <div class="payment-amount">${formatCurrency(payment.amount)}</div>
                            <button class="btn btn-success" onclick="processPayment(${payment.paymentId})">
                                <i class="fas fa-credit-card"></i> Thanh toán
                            </button>
                        </div>
                    </div>
                `;
            }).join('');
        } else {
            container.innerHTML = `
                <div style="text-align: center; padding: 40px; color: var(--text-light);">
                    <i class="fas fa-check-circle" style="font-size: 48px; margin-bottom: 16px; color: var(--success);"></i>
                    <p>Bạn không có khoản thanh toán nào đang chờ</p>
                </div>
            `;
        }
    } catch (error) {
        console.error('Error loading pending payments:', error);
        container.innerHTML = `
            <div style="text-align: center; padding: 40px; color: var(--text-light);">
                <i class="fas fa-exclamation-circle" style="font-size: 48px; margin-bottom: 16px; color: var(--danger);"></i>
                <p>Không thể tải dữ liệu thanh toán</p>
            </div>
        `;
    }
}

async function loadPaymentHistory() {
    const container = document.getElementById('payment-history-list');
    
    try {
        // Load payment history from API
        const response = await fetch(`${API.PAYMENTS}/user/${CURRENT_USER_ID}/history`);
        const history = await response.json();
        
        if (history && history.length > 0) {
            container.innerHTML = history.map(item => {
                // Map payment method to Vietnamese
                const methodNames = {
                    'EWallet': 'Ví điện tử',
                    'Banking': 'Chuyển khoản',
                    'Cash': 'Tiền mặt'
                };
                const methodName = methodNames[item.method] || item.method;
                const description = item.description || `Thanh toán #${item.paymentId}`;
                
                return `
                    <div class="payment-history-item">
                        <div class="payment-history-left">
                            <div class="payment-title">${description}</div>
                            <div class="payment-date">
                                <i class="fas fa-calendar"></i> ${formatDate(item.paymentDate)}
                            </div>
                            <small style="color: var(--text-light);">Mã: ${item.transactionCode}</small>
                        </div>
                        <div class="payment-history-right">
                            <div class="payment-history-amount">${formatCurrency(item.amount)}</div>
                            <div class="payment-method">
                                <i class="fas ${getPaymentMethodIcon(item.method)}"></i>
                                ${methodName}
                            </div>
                        </div>
                    </div>
                `;
            }).join('');
        } else {
            container.innerHTML = `
                <div style="text-align: center; padding: 40px; color: var(--text-light);">
                    <i class="fas fa-history" style="font-size: 48px; margin-bottom: 16px;"></i>
                    <p>Chưa có lịch sử thanh toán</p>
                </div>
            `;
        }
    } catch (error) {
        console.error('Error loading payment history:', error);
        container.innerHTML = `
            <div style="text-align: center; padding: 40px; color: var(--text-light);">
                <i class="fas fa-exclamation-circle" style="font-size: 48px; margin-bottom: 16px; color: var(--danger);"></i>
                <p>Không thể tải lịch sử thanh toán</p>
            </div>
        `;
    }
}

async function processPayment(paymentId) {
    const selectedMethod = document.querySelector('.method-card.active');
    if (!selectedMethod) {
        showToast('Vui lòng chọn phương thức thanh toán', 'error');
        return;
    }
    
    const method = selectedMethod.getAttribute('data-method');
    
    // Show loading state
    showToast('Đang xử lý thanh toán...', 'info');
    
    try {
        // Call API to process payment
        const response = await fetch(`${API.PAYMENTS}/${paymentId}/process`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                method: method
            })
        });
        
        const result = await response.json();
        
        if (result.success) {
            showToast(`Thanh toán thành công! Mã GD: ${result.transactionCode}`, 'success');
            
            // Reload payment lists
            await loadPendingPayments();
            await loadPaymentHistory();
            
            // Update quick stats
            await loadQuickStats();
        } else {
            showToast(result.message || 'Lỗi khi xử lý thanh toán', 'error');
        }
    } catch (error) {
        console.error('Error processing payment:', error);
        showToast('Có lỗi xảy ra khi thanh toán. Vui lòng thử lại!', 'error');
    }
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
        'ElectricCharge': 'Phí sạc điện',
        'Maintenance': 'Bảo dưỡng',
        'Insurance': 'Bảo hiểm',
        'Inspection': 'Đăng kiểm',
        'Cleaning': 'Vệ sinh',
        'Other': 'Khác'
    };
    return types[type] || type;
}

function getPaymentMethodIcon(method) {
    const icons = {
        'EWallet': 'fa-mobile-alt',
        'Banking': 'fa-university',
        'Cash': 'fa-money-bill'
    };
    return icons[method] || 'fa-credit-card';
}

function showToast(message, type) {
    const toast = document.getElementById('toast');
    const icon = toast.querySelector('.toast-icon');
    const messageEl = toast.querySelector('.toast-message');
    
    toast.className = `toast ${type}`;
    
    if (type === 'success') {
        icon.innerHTML = '<i class="fas fa-check-circle"></i>';
    } else {
        icon.innerHTML = '<i class="fas fa-exclamation-circle"></i>';
    }
    
    messageEl.textContent = message;
    
    toast.classList.add('show');
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

