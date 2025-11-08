// User Dashboard JavaScript

// API Endpoints
const API = {
    GROUPS: '/api/groups',
    COSTS: '/api/costs',
    USAGE: '/api/usage-tracking',
    PAYMENTS: '/api/payments',
    COST_SHARES: '/api/cost-shares',
    FUND: '/api/fund'
};

// Current User - get from URL parameter or default to 1
const urlParams = new URLSearchParams(window.location.search);
const CURRENT_USER_ID = parseInt(urlParams.get('userId')) || 2;

// Global State
let currentPage = 'home';
let fundAutoRefreshInterval = null;
let lastPendingVoteCount = 0;

// Initialize on DOM load
document.addEventListener('DOMContentLoaded', function() {
    initNavigation();
    initUsageForm();
    initPaymentMethods();
    initFundModals();
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
    // Dừng auto-refresh nếu không ở trang Fund
    if (page !== 'fund') {
        stopFundAutoRefresh();
    }
    
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
        case 'fund':
            loadFundPage();
            break;
        case 'browse-groups':
            loadBrowseGroupsPage();
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

// Store user role for each group
let userGroupRoles = {}; // { groupId: 'Admin' | 'Member' }

async function loadMyGroups() {
    try {
        const response = await fetch(`${API.GROUPS}/user/${CURRENT_USER_ID}`);
        if (!response.ok) throw new Error('Failed to load groups');
        
        const groups = await response.json();
        
        const container = document.getElementById('my-groups-list');
        if (!container) return;
        
        if (groups.length === 0) {
            container.innerHTML = '<p style="text-align: center; color: var(--text-light);">Bạn chưa tham gia nhóm nào</p>';
            return;
        }
        
        // Fetch user role for each group
        for (const group of groups) {
            try {
                const membersResponse = await fetch(`${API.GROUPS}/${group.groupId}/members`);
                if (membersResponse.ok) {
                    const members = await membersResponse.json();
                    const userMember = members.find(m => m.userId === CURRENT_USER_ID);
                    if (userMember) {
                        userGroupRoles[group.groupId] = userMember.role || 'Member';
                    } else {
                        userGroupRoles[group.groupId] = 'Member';
                    }
                }
            } catch (e) {
                console.warn(`Failed to fetch role for group ${group.groupId}:`, e);
                userGroupRoles[group.groupId] = 'Member';
            }
        }
        
        container.innerHTML = groups.map(group => {
            const isAdmin = userGroupRoles[group.groupId] === 'Admin';
            return `
            <div class="group-item" data-group-id="${group.groupId}">
                <div class="group-item-header">
                    <h3>${escapeHtml(group.groupName)}</h3>
                    ${isAdmin ? '<span class="badge badge-admin"><i class="fas fa-crown"></i> Admin</span>' : ''}
                </div>
                <p>Quản lý bởi: User #${group.adminId}</p>
                <div class="group-stats">
                    <div class="group-stat">
                        <i class="fas fa-users"></i>
                        <span>${group.memberCount || 0} thành viên</span>
                    </div>
                    <div class="group-stat">
                        <i class="fas fa-car"></i>
                        <span>Xe #${group.vehicleId || 'N/A'}</span>
                    </div>
                </div>
                ${isAdmin ? `
                <div class="group-actions">
                    <button class="btn btn-primary btn-sm manage-group-btn" data-group-id="${group.groupId}" data-group-name="${escapeHtml(group.groupName)}">
                        <i class="fas fa-cog"></i> Quản lý nhóm
                    </button>
            </div>
                ` : ''}
            </div>
        `}).join('');
        
        // Bind click handlers for manage group buttons
        document.querySelectorAll('.manage-group-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const groupId = parseInt(this.getAttribute('data-group-id'));
                const groupName = this.getAttribute('data-group-name');
                openManageGroupModal(groupId, groupName);
            });
        });
        
    } catch (error) {
        console.error('Error loading groups:', error);
        const container = document.getElementById('my-groups-list');
        if (container) {
            container.innerHTML = '<p style="text-align: center; color: var(--text-light);">Không có dữ liệu</p>';
        }
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
    const grid = document.getElementById('user-costs-grid');
    
    try {
        console.log('Loading costs page for user:', CURRENT_USER_ID);
        
        // Load all cost shares for current user (both paid and pending)
        const pendingUrl = `${API.COST_SHARES}/user/${CURRENT_USER_ID}/pending`;
        const historyUrl = `${API.COST_SHARES}/user/${CURRENT_USER_ID}/history`;
        
        console.log('Fetching pending from:', pendingUrl);
        console.log('Fetching history from:', historyUrl);
        
        const [pendingResponse, historyResponse] = await Promise.all([
            fetch(pendingUrl),
            fetch(historyUrl)
        ]);
        
        console.log('Pending response status:', pendingResponse.status);
        console.log('History response status:', historyResponse.status);
        
        if (!pendingResponse.ok) {
            const errorText = await pendingResponse.text();
            console.error('Pending response error:', errorText);
        }
        
        if (!historyResponse.ok) {
            const errorText = await historyResponse.text();
            console.error('History response error:', errorText);
        }
        
        const pendingShares = pendingResponse.ok ? await pendingResponse.json() : [];
        const paidShares = historyResponse.ok ? await historyResponse.json() : [];
        
        console.log('Pending shares count:', pendingShares.length);
        console.log('Paid shares count:', paidShares.length);
        console.log('Pending shares:', pendingShares);
        console.log('Paid shares:', paidShares);
        
        // Validate that we got arrays
        if (!Array.isArray(pendingShares)) {
            console.warn('Pending shares is not an array:', pendingShares);
        }
        if (!Array.isArray(paidShares)) {
            console.warn('Paid shares is not an array:', paidShares);
        }
        
        // Combine and mark paid status
        const allShares = [
            ...(Array.isArray(pendingShares) ? pendingShares.map(s => ({...s, isPaid: false})) : []),
            ...(Array.isArray(paidShares) ? paidShares.map(s => ({...s, isPaid: true})) : [])
        ].sort((a, b) => {
            const dateA = a.calculatedAt ? new Date(a.calculatedAt) : new Date(0);
            const dateB = b.calculatedAt ? new Date(b.calculatedAt) : new Date(0);
            return dateB - dateA;
        });
        
        console.log('Total shares to display:', allShares.length);
        
        if (allShares.length === 0) {
            grid.innerHTML = `
                <div style="grid-column: 1/-1; text-align: center; padding: 40px; color: var(--text-light);">
                    <i class="fas fa-inbox" style="font-size: 48px; margin-bottom: 16px; opacity: 0.5;"></i>
                    <p>Bạn chưa có chi phí nào</p>
                    <small style="display: block; margin-top: 10px; opacity: 0.7;">User ID: ${CURRENT_USER_ID}</small>
                </div>
            `;
            return;
        }
        
        grid.innerHTML = allShares.map(share => {
            const description = share.description || `Chi phí #${share.costId || 'N/A'}`;
            const safeDescription = description.replace(/'/g, "\\'").replace(/"/g, '&quot;');
            const amount = share.amountShare || share.shareAmount || 0;
            const shareId = share.shareId || share.share_id || 'N/A';
            
            // Handle calculatedAt - could be string or already a date
            let calculatedDate = new Date().toISOString();
            if (share.calculatedAt) {
                try {
                    calculatedDate = share.calculatedAt;
                } catch (e) {
                    console.warn('Error parsing calculatedAt:', e);
                }
            }
            
            return `
                <div class="cost-card">
                    <div class="cost-header">
                        <div class="cost-type">Chi phí chung</div>
                        <div class="cost-status ${share.isPaid ? 'paid' : 'pending'}">
                            ${share.isPaid ? 'Đã thanh toán' : 'Chưa thanh toán'}
                        </div>
                    </div>
                    <div class="cost-amount">${formatCurrency(amount)}</div>
                    <div class="cost-details">
                        ${description}
                    </div>
                    <div class="cost-footer">
                        <div class="cost-date">
                            <i class="fas fa-calendar"></i> ${formatDate(calculatedDate)}
                        </div>
                        ${!share.isPaid ? `
                            <button class="btn btn-success" style="padding: 0.5rem 1rem;" 
                                    onclick="payCostShare(${shareId}, ${amount}, '${safeDescription}')">
                                <i class="fas fa-credit-card"></i> Thanh toán
                            </button>
                        ` : ''}
                    </div>
                </div>
            `;
        }).join('');
        
        console.log('Successfully rendered', allShares.length, 'cost shares');
        
    } catch (error) {
        console.error('Error loading costs:', error);
        console.error('Error stack:', error.stack);
        grid.innerHTML = `
            <div style="grid-column: 1/-1; text-align: center; padding: 40px; color: var(--danger);">
                <i class="fas fa-exclamation-triangle" style="font-size: 48px; margin-bottom: 16px;"></i>
                <p>Không thể tải dữ liệu chi phí</p>
                <small style="display: block; margin-top: 10px; opacity: 0.7;">${error.message}</small>
            </div>
        `;
    }
}

function payCostShare(shareId, amount, description) {
    // Store payment info for later use
    window.pendingCostPayment = {
        shareId: shareId,
        amount: amount,
        description: description
    };
    
    // Switch to payments page
    switchPage('payments');
    showToast('Đã chuyển đến trang thanh toán. Vui lòng chọn phương thức!', 'info');
    
    // After page loads, scroll to payment methods
    setTimeout(() => {
        const methodSection = document.querySelector('.payment-methods-grid');
        if (methodSection) {
            methodSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    }, 300);
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
            
            // If there's a pending cost payment, show QR immediately
            if (window.pendingCostPayment) {
                const methodType = this.getAttribute('data-method');
                const { shareId, amount, description } = window.pendingCostPayment;
                
                // Show QR modal
                showQRCodeModalForCostShare(shareId, amount, description, methodType);
                
                // Clear pending payment
                window.pendingCostPayment = null;
            }
        });
    });
}

async function loadPendingPayments() {
    const container = document.getElementById('pending-payments-list');
    
    try {
        // Load pending cost shares (chưa thanh toán) from API
        const response = await fetch(`${API.COST_SHARES}/user/${CURRENT_USER_ID}/pending`);
        const pendingShares = await response.json();
        
        if (pendingShares && pendingShares.length > 0) {
            container.innerHTML = pendingShares.map(share => {
                // Get cost description or use default
                const description = share.costDescription || share.description || `Chi phí #${share.costId}`;
                const safeDescription = description.replace(/'/g, "\\'").replace(/"/g, '&quot;');
                const createdDate = share.calculatedAt || share.createdAt || new Date().toISOString();
                const amount = share.amountShare || share.shareAmount || 0;
                
                return `
                    <div class="payment-item">
                        <div class="payment-item-left">
                            <h4>${description}</h4>
                            <p><i class="fas fa-calendar"></i> ${formatDate(createdDate)}</p>
                            <small style="color: var(--text-light);">Phần chia của bạn</small>
                        </div>
                        <div class="payment-item-right">
                            <div class="payment-amount">${formatCurrency(amount)}</div>
                            <button class="btn btn-success" onclick="processCostSharePayment(${share.shareId}, ${amount}, '${safeDescription}')">
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

// New function to handle cost share payment
async function processCostSharePayment(shareId, amount, description) {
    const selectedMethod = document.querySelector('.method-card.active');
    if (!selectedMethod) {
        showToast('Vui lòng chọn phương thức thanh toán', 'error');
        return;
    }
    
    const method = selectedMethod.getAttribute('data-method');
    
    // Show QR code modal directly with cost share info
    showQRCodeModalForCostShare(shareId, amount, description, method);
}

async function processPayment(paymentId) {
    const selectedMethod = document.querySelector('.method-card.active');
    if (!selectedMethod) {
        showToast('Vui lòng chọn phương thức thanh toán', 'error');
        return;
    }
    
    const method = selectedMethod.getAttribute('data-method');
    
    // Get payment details
    try {
        const response = await fetch(`${API.PAYMENTS}/${paymentId}`);
        const payment = await response.json();
        
        // Show QR code modal
        showQRCodeModal(paymentId, payment, method);
        
    } catch (error) {
        console.error('Error loading payment details:', error);
        showToast('Không thể tải thông tin thanh toán', 'error');
    }
}

// New function to show QR modal for cost share payment
function showQRCodeModalForCostShare(shareId, amount, description, method) {
    // Get method info
    const methodInfo = {
        'ewallet': { name: 'Ví điện tử', bank: 'MoMo', account: '0123456789', accountName: 'NGUYEN VAN A' },
        'banking': { name: 'Chuyển khoản', bank: 'Vietcombank', account: '0987654321', accountName: 'NGUYEN VAN A' },
        'cash': { name: 'Tiền mặt', bank: 'Tiền mặt', account: 'Thanh toán trực tiếp', accountName: 'Admin' }
    };
    
    const info = methodInfo[method] || methodInfo['ewallet']; // Default to ewallet
    
    // Generate QR content (for demo - in production use real QR API)
    const qrContent = `Bank: ${info.bank}\nAccount: ${info.account}\nAmount: ${amount}\nContent: SHARE${shareId}`;
    const qrCodeUrl = `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodeURIComponent(qrContent)}`;
    
    // Create modal HTML
    const modalHTML = `
        <div class="payment-modal-overlay" id="qr-modal">
            <div class="payment-modal">
                <div class="modal-header">
                    <h2><i class="fas fa-qrcode"></i> Thanh toán ${info.name}</h2>
                    <button class="close-modal" onclick="closeQRModal()">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
                
                <div class="modal-body">
                    <!-- Payment Info Box with Gradient -->
                    <div class="payment-info-section">
                        <div class="info-row">
                            <span class="info-label">Số tiền:</span>
                            <span class="info-value amount">${formatCurrency(amount)}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Nội dung:</span>
                            <span class="info-value">SHARE${shareId}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Chi phí:</span>
                            <span class="info-value">${description}</span>
                        </div>
                    </div>
                    
                    <!-- Bank Info Box -->
                    <div class="bank-info-box">
                        <div class="bank-row">
                            <i class="fas fa-university"></i>
                            <span class="label">Ngân hàng:</span>
                            <span>${info.bank}</span>
                        </div>
                        <div class="bank-row">
                            <i class="fas fa-credit-card"></i>
                            <span class="label">Số TK:</span>
                            <span>${info.account}</span>
                        </div>
                        <div class="bank-row">
                            <i class="fas fa-user"></i>
                            <span class="label">Tên TK:</span>
                            <span>${info.accountName}</span>
                        </div>
                    </div>
                    
                    ${method !== 'cash' ? `
                        <div class="qr-code-section">
                            <div class="qr-code-display">
                                <h4>Quét mã QR để thanh toán</h4>
                                <div class="qr-code-img">
                                    <img src="${qrCodeUrl}" alt="QR Code">
                                </div>
                                <p class="qr-note">
                                    <i class="fas fa-info-circle"></i>
                                    Quét mã QR bằng app ${info.bank} của bạn
                                </p>
                            </div>
                        </div>
                    ` : `
                        <div class="cash-payment-section">
                            <div class="cash-notice">
                                <i class="fas fa-hand-holding-usd"></i>
                                <p>Vui lòng thanh toán tiền mặt trực tiếp cho admin</p>
                                <p class="cash-amount">${formatCurrency(amount)}</p>
                            </div>
                        </div>
                    `}
                    
                    <div class="payment-instructions">
                        <h4><i class="fas fa-clipboard-list"></i> Hướng dẫn thanh toán:</h4>
                        <ol>
                            ${method === 'ewallet' ? `
                                <li>Mở app MoMo trên điện thoại</li>
                                <li>Chọn "Quét QR" hoặc "Chuyển tiền"</li>
                                <li>Quét mã QR hoặc nhập số: ${info.account}</li>
                                <li>Kiểm tra số tiền: ${formatCurrency(amount)}</li>
                                <li>Kiểm tra nội dung: SHARE${shareId}</li>
                                <li>Xác nhận thanh toán trên app</li>
                                <li>Sau khi chuyển khoản thành công, bấm "Xác nhận thanh toán" bên dưới</li>
                            ` : method === 'banking' ? `
                                <li>Mở app ngân hàng trên điện thoại</li>
                                <li>Chọn "Chuyển khoản" hoặc "QR Pay"</li>
                                <li>Quét mã QR hoặc nhập số TK: ${info.account}</li>
                                <li>Chọn ngân hàng: ${info.bank}</li>
                                <li>Nhập số tiền: ${formatCurrency(amount)}</li>
                                <li>Nhập nội dung: SHARE${shareId}</li>
                                <li>Xác nhận và nhập OTP</li>
                                <li>Sau khi thành công, bấm "Xác nhận thanh toán" bên dưới</li>
                            ` : `
                                <li>Chuẩn bị số tiền: ${formatCurrency(amount)}</li>
                                <li>Liên hệ admin để thanh toán trực tiếp</li>
                                <li>Ghi nhớ mã: SHARE${shareId}</li>
                                <li>Sau khi thanh toán, bấm "Xác nhận thanh toán" bên dưới</li>
                            `}
                        </ol>
                    </div>
                </div>
                
                <div class="modal-footer">
                    <button class="btn btn-secondary" onclick="closeQRModal()">
                        <i class="fas fa-times"></i> Hủy
                    </button>
                    <button class="btn btn-success" onclick="confirmCostSharePayment(${shareId}, '${method}')">
                        <i class="fas fa-check-circle"></i> Xác nhận thanh toán
                    </button>
                </div>
            </div>
        </div>
    `;
    
    // Add modal to page
    document.body.insertAdjacentHTML('beforeend', modalHTML);
}

function showQRCodeModal(paymentId, payment, method) {
    // Get method info
    const methodInfo = {
        'ewallet': { name: 'Ví điện tử', bank: 'MoMo', account: '0123456789', accountName: 'NGUYEN VAN A' },
        'banking': { name: 'Chuyển khoản', bank: 'Vietcombank', account: '0987654321', accountName: 'NGUYEN VAN A' },
        'cash': { name: 'Tiền mặt', bank: 'Tiền mặt', account: 'Thanh toán trực tiếp', accountName: 'Admin' }
    };
    
    const info = methodInfo[method] || methodInfo['banking'];
    const amount = payment.amount || 0;
    
    // Generate QR content (for demo - in production use real QR API)
    const qrContent = `Bank: ${info.bank}\nAccount: ${info.account}\nAmount: ${amount}\nContent: PAY${paymentId}`;
    const qrCodeUrl = `https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=${encodeURIComponent(qrContent)}`;
    
    // Create modal HTML
    const modalHTML = `
        <div class="payment-modal-overlay" id="qr-modal">
            <div class="payment-modal">
                <div class="modal-header">
                    <h2><i class="fas fa-qrcode"></i> Thanh toán ${info.name}</h2>
                    <button class="close-modal" onclick="closeQRModal()">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
                
                <div class="modal-body">
                    <div class="payment-info-box">
                        <div class="info-row">
                            <span class="info-label">Ngân hàng/Ví:</span>
                            <span class="info-value">${info.bank}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Số tài khoản:</span>
                            <span class="info-value">${info.account}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Tên tài khoản:</span>
                            <span class="info-value">${info.accountName}</span>
                        </div>
                        <div class="info-row highlight">
                            <span class="info-label">Số tiền:</span>
                            <span class="info-value amount">${formatCurrency(amount)}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Nội dung:</span>
                            <span class="info-value">PAY${paymentId}</span>
                        </div>
                    </div>
                    
                    ${method !== 'cash' ? `
                        <div class="qr-code-container">
                            <h3>Quét mã QR để thanh toán</h3>
                            <div class="qr-code">
                                <img src="${qrCodeUrl}" alt="QR Code">
                            </div>
                            <p class="qr-note">
                                <i class="fas fa-info-circle"></i>
                                Quét mã QR bằng app ${info.bank} của bạn
                            </p>
                        </div>
                    ` : `
                        <div class="cash-payment-note">
                            <i class="fas fa-hand-holding-usd"></i>
                            <p>Vui lòng thanh toán tiền mặt trực tiếp cho admin</p>
                        </div>
                    `}
                    
                    <div class="payment-instructions">
                        <h4><i class="fas fa-clipboard-list"></i> Hướng dẫn:</h4>
                        <ol>
                            ${method !== 'cash' ? `
                                <li>Mở app ${info.bank} trên điện thoại</li>
                                <li>Quét mã QR hoặc nhập thông tin chuyển khoản</li>
                                <li>Kiểm tra số tiền và nội dung chuyển khoản</li>
                                <li>Xác nhận thanh toán trên app</li>
                                <li>Sau khi chuyển khoản thành công, bấm nút "Xác nhận đã thanh toán" bên dưới</li>
                            ` : `
                                <li>Chuẩn bị số tiền: ${formatCurrency(amount)}</li>
                                <li>Liên hệ admin để thanh toán</li>
                                <li>Sau khi thanh toán, bấm nút "Xác nhận đã thanh toán"</li>
                            `}
                        </ol>
                    </div>
                </div>
                
                <div class="modal-footer">
                    <button class="btn btn-secondary" onclick="closeQRModal()">
                        <i class="fas fa-times"></i> Hủy
                    </button>
                    <button class="btn btn-success" onclick="confirmPayment(${paymentId}, '${method}')">
                        <i class="fas fa-check-circle"></i> Xác nhận đã thanh toán
                    </button>
                </div>
            </div>
        </div>
    `;
    
    // Add modal to page
    document.body.insertAdjacentHTML('beforeend', modalHTML);
}

function closeQRModal() {
    const modal = document.getElementById('qr-modal');
    if (modal) {
        modal.remove();
    }
}

// New function to confirm cost share payment
async function confirmCostSharePayment(shareId, method) {
    // Show loading
    showToast('Đang xác nhận thanh toán...', 'info');
    
    try {
        // Generate transaction code
        const transactionCode = 'TXN' + Date.now() + Math.floor(Math.random() * 1000);
        
        // Call API to confirm payment for cost share
        const response = await fetch(`${API.COST_SHARES}/${shareId}/payment`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                userId: CURRENT_USER_ID,
                paymentMethod: method,
                transactionCode: transactionCode,
                status: 'PAID'
            })
        });
        
        if (response.ok) {
            const result = await response.json();
            
            // Close modal
            closeQRModal();
            
            // Show success message
            showToast(`Thanh toán thành công! Mã GD: ${transactionCode}`, 'success');
            
            // Reload payment lists
            await loadPendingPayments();
            await loadPaymentHistory();
            
            // Reload costs page if it's visible
            const costsPage = document.getElementById('costs');
            if (costsPage && !costsPage.classList.contains('hidden')) {
                await loadCostsPage();
            }
            
            // Update quick stats
            await loadQuickStats();
        } else {
            const errorText = await response.text();
            console.error('Payment error:', errorText);
            showToast('Lỗi khi xác nhận thanh toán', 'error');
        }
    } catch (error) {
        console.error('Error confirming payment:', error);
        showToast('Có lỗi xảy ra. Vui lòng thử lại!', 'error');
    }
}

async function confirmPayment(paymentId, method) {
    // Show loading
    showToast('Đang xác nhận thanh toán...', 'info');
    
    try {
        // Generate transaction code
        const transactionCode = 'TXN' + Date.now() + Math.floor(Math.random() * 1000);
        
        // Call API to confirm payment
        const response = await fetch(`${API.PAYMENTS}/${paymentId}/confirm`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                userId: CURRENT_USER_ID,
                method: method,
                transactionCode: transactionCode
            })
        });
        
        if (response.ok) {
            const result = await response.json();
            
            // Close modal
            closeQRModal();
            
            // Show success message
            showToast(`Thanh toán thành công! Mã GD: ${transactionCode}`, 'success');
            
            // Reload payment lists
            await loadPendingPayments();
            await loadPaymentHistory();
            
            // Update quick stats
            await loadQuickStats();
        } else {
            showToast('Lỗi khi xác nhận thanh toán', 'error');
        }
    } catch (error) {
        console.error('Error confirming payment:', error);
        showToast('Có lỗi xảy ra. Vui lòng thử lại!', 'error');
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

// ============ FUND PAGE ============
async function loadFundPage() {
    try {
        await loadFundGroups();
        await loadFundStats();
        await loadMyPendingRequests();
        await loadPendingVoteRequests(); // Load các yêu cầu cần vote
        await loadRecentTransactions();
        await loadTransactionHistory();
        
        // Bắt đầu auto-refresh mỗi 15 giây khi ở trang Fund
        startFundAutoRefresh();
    } catch (error) {
        console.error('Error loading fund page:', error);
    }
}

// Auto-refresh cho trang Fund (kiểm tra yêu cầu mới mỗi 15 giây)
function startFundAutoRefresh() {
    // Dừng interval cũ nếu có
    if (fundAutoRefreshInterval) {
        clearInterval(fundAutoRefreshInterval);
    }
    
    // Chỉ auto-refresh khi đang ở trang Fund
    fundAutoRefreshInterval = setInterval(() => {
        if (currentPage === 'fund') {
            console.log('🔄 Auto-refreshing fund data...');
            loadPendingVoteRequests(); // Kiểm tra yêu cầu mới cần vote
            loadMyPendingRequests(); // Kiểm tra yêu cầu của mình
            loadFundStats(); // Cập nhật stats
        }
    }, 15000); // 15 giây
}

function stopFundAutoRefresh() {
    if (fundAutoRefreshInterval) {
        clearInterval(fundAutoRefreshInterval);
        fundAutoRefreshInterval = null;
    }
}

function initFundModals() {
    // Deposit form handler
    const depositForm = document.getElementById('depositForm');
    if (depositForm) {
        depositForm.addEventListener('submit', handleDeposit);
    }
    
    // Withdraw vote form handler
    const withdrawVoteForm = document.getElementById('withdrawVoteForm');
    if (withdrawVoteForm) {
        withdrawVoteForm.addEventListener('submit', handleWithdrawVote);
    }
    
    // Withdraw group dropdown - cập nhật số dư khi chọn nhóm
    const withdrawGroupSelect = document.getElementById('withdrawGroup');
    if (withdrawGroupSelect) {
        withdrawGroupSelect.addEventListener('change', function() {
            const selectedGroupId = this.value;
            if (selectedGroupId) {
                loadFundBalanceByGroupId(parseInt(selectedGroupId));
            } else {
                // Reset về 0 nếu không chọn nhóm
                const availableBalanceEl = document.getElementById('availableBalance');
                if (availableBalanceEl) {
                    availableBalanceEl.textContent = formatFundCurrency(0);
                }
            }
        });
    }
    
    // Filter handlers
    const filterStatus = document.getElementById('filterStatus');
    const filterType = document.getElementById('filterType');
    if (filterStatus) {
        filterStatus.addEventListener('change', loadTransactionHistory);
    }
    if (filterType) {
        filterType.addEventListener('change', loadTransactionHistory);
    }
    
    // Close modal when clicking outside
    window.addEventListener('click', function(event) {
        if (event.target.id === 'depositModal') {
            closeDepositModal();
        }
        if (event.target.id === 'withdrawVoteModal') {
            closeWithdrawVoteModal();
        }
    });
}

// Load groups for fund dropdowns (chỉ các nhóm mà user đã tham gia)
async function loadFundGroups() {
    try {
        // Chỉ load các nhóm mà user hiện tại đã tham gia
        const response = await fetch(`/api/groups/user/${CURRENT_USER_ID}`);
        if (!response.ok) throw new Error('Failed to load groups');
        
        const groups = await response.json();
        console.log(`📦 [FUND] Loaded ${groups.length} groups for user ${CURRENT_USER_ID}:`, groups);
        
        // Fetch fundId for each group
        const groupsWithFunds = await Promise.all(
            groups.map(async (group) => {
                try {
                    const fundResponse = await fetch(`${API.FUND}/group/${group.groupId}`);
                    if (fundResponse.ok) {
                        const fund = await fundResponse.json();
                        return {
                            ...group,
                            fundId: fund.fundId
                        };
                    } else if (fundResponse.status === 404) {
                        // Group chưa có fund là bình thường, không cần log warning
                        // console.debug(`Group ${group.groupId} chưa có fund`);
                    }
                } catch (e) {
                    // Ignore 404 errors (group chưa có fund)
                    if (e.message && !e.message.includes('404')) {
                        console.debug(`Error checking fund for group ${group.groupId}:`, e.message);
                    }
                }
                return group;
            })
        );
        
        // Populate deposit dropdown - chỉ các nhóm user đã tham gia
        const depositSelect = document.getElementById('depositGroup');
        if (depositSelect) {
            depositSelect.innerHTML = '<option value="">Chọn nhóm</option>' +
                groupsWithFunds
                    .map(g => `<option value="${g.groupId}" data-fund-id="${g.fundId || ''}">${g.groupName}${g.fundId ? '' : ' (chưa có quỹ)'}</option>`)
                    .join('');
        }
        
        // Populate withdraw dropdown - chỉ nhóm có quỹ và user đã tham gia mới rút được
        const withdrawSelect = document.getElementById('withdrawGroup');
        if (withdrawSelect) {
            const groupsWithFundsOnly = groupsWithFunds.filter(g => g.fundId);
            if (groupsWithFundsOnly.length === 0) {
                withdrawSelect.innerHTML = '<option value="">Bạn chưa tham gia nhóm nào có quỹ</option>';
            } else {
                withdrawSelect.innerHTML = '<option value="">Chọn nhóm</option>' +
                    groupsWithFundsOnly
                        .map(g => `<option value="${g.groupId}" data-fund-id="${g.fundId}">${g.groupName}</option>`)
                        .join('');
            }
        }
        
    } catch (error) {
        console.error('Error loading groups:', error);
        const depositSelect = document.getElementById('depositGroup');
        const withdrawSelect = document.getElementById('withdrawGroup');
        if (depositSelect) depositSelect.innerHTML = '<option value="">Không thể tải nhóm</option>';
        if (withdrawSelect) withdrawSelect.innerHTML = '<option value="">Không thể tải nhóm</option>';
    }
}

async function loadFundStats() {
    try {
        // Gọi API với userId để chỉ lấy số dư của các nhóm mà user tham gia
        const response = await fetch(`${API.FUND}/stats?userId=${CURRENT_USER_ID}`);
        if (!response.ok) throw new Error('Failed to load stats');
        
        const stats = await response.json();
        
        // Update stats cards
        const totalBalanceEl = document.getElementById('totalBalance');
        const myDepositsEl = document.getElementById('myDeposits');
        const myWithdrawsEl = document.getElementById('myWithdraws');
        const myPendingEl = document.getElementById('myPending');
        
        if (totalBalanceEl) totalBalanceEl.textContent = formatFundCurrency(stats.totalBalance);
        if (myDepositsEl) myDepositsEl.textContent = formatFundCurrency(stats.myDeposits || 0);
        if (myWithdrawsEl) myWithdrawsEl.textContent = formatFundCurrency(stats.myWithdraws || 0);
        if (myPendingEl) myPendingEl.textContent = stats.myPendingCount || 0;
        
        // Update summary
        const summaryOpeningEl = document.getElementById('summaryOpening');
        const summaryIncomeEl = document.getElementById('summaryIncome');
        const summaryExpenseEl = document.getElementById('summaryExpense');
        const summaryBalanceEl = document.getElementById('summaryBalance');
        
        if (summaryOpeningEl) summaryOpeningEl.textContent = formatFundCurrency(stats.openingBalance);
        if (summaryIncomeEl) summaryIncomeEl.textContent = formatFundCurrency(stats.totalIncome);
        if (summaryExpenseEl) summaryExpenseEl.textContent = formatFundCurrency(stats.totalExpense);
        if (summaryBalanceEl) summaryBalanceEl.textContent = formatFundCurrency(stats.totalBalance);
        
    } catch (error) {
        console.error('Error loading stats:', error);
    }
}

async function loadMyPendingRequests() {
    try {
        const response = await fetch(`${API.FUND}/transactions?status=Pending&userId=${CURRENT_USER_ID}`);
        if (!response.ok) throw new Error('Failed to load pending requests');
        
        const transactions = await response.json();
        if (!Array.isArray(transactions)) {
            console.warn('⚠️ Expected array but got:', transactions);
            updateMyPendingDisplay([]);
            return;
        }
        
        // Filter only my withdrawal requests (deposits don't need approval)
        const myRequests = transactions.filter(t => {
            const userId = t.userId || t.user_id || t.createdBy || t.created_by;
            const transactionType = t.transactionType || t.transaction_type || t.type;
            return userId === CURRENT_USER_ID && 
                   (transactionType === 'Withdraw' || transactionType === 'WITHDRAW');
        });
        
        updateMyPendingDisplay(myRequests);
        
    } catch (error) {
        console.error('Error loading my pending requests:', error);
        updateMyPendingDisplay([]);
    }
}

function updateMyPendingDisplay(requests) {
    const badge = document.getElementById('myPendingBadge');
    const tbody = document.getElementById('myPendingBody');
    
    if (badge) badge.textContent = requests.length;
    
    if (!tbody) return;
    
    if (requests.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="empty-table">
                    <div class="empty-state">
                        <i class="fas fa-check-circle"></i>
                        <p>Không có phiếu nào đang chờ</p>
                    </div>
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = requests.map(t => `
        <tr>
            <td>${formatFundDate(t.createdAt)}</td>
            <td class="amount negative">
                ${formatFundCurrency(t.amount)}
            </td>
            <td>${t.purpose || '-'}</td>
            <td>
                <span class="badge badge-${getFundStatusClass(t.status)}">
                    ${getFundStatusIcon(t.status)} ${getFundStatusText(t.status)}
                </span>
            </td>
            <td>
                ${t.voteId 
                    ? `<a href="/groups/voting?voteId=${t.voteId}" class="btn btn-sm btn-outline">
                         <i class="fas fa-poll"></i> Xem phiếu vote
                       </a>`
                    : '<span class="text-muted">Chưa có vote</span>'
                }
            </td>
            <td>
                <button class="btn btn-sm btn-outline" onclick="viewTransactionDetail(${t.transactionId})">
                    <i class="fas fa-eye"></i>
                </button>
                ${t.status === 'Pending' 
                    ? `<button class="btn btn-sm btn-danger" onclick="cancelRequest(${t.transactionId})">
                         <i class="fas fa-times"></i>
                       </button>`
                    : ''
                }
            </td>
        </tr>
    `).join('');
}

async function loadRecentTransactions() {
    try {
        const response = await fetch(`${API.FUND}/transactions?status=Completed&userId=${CURRENT_USER_ID}`);
        if (!response.ok) throw new Error('Failed to load transactions');
        
        const transactions = await response.json();
        if (!Array.isArray(transactions)) {
            console.warn('⚠️ Expected array but got:', transactions);
            transactions = [];
        }
        
        // Take only last 5
        const recent = transactions.slice(0, 5);
        
        const container = document.getElementById('recentTransactions');
        if (!container) {
            console.warn('⚠️ Container #recentTransactions not found');
            return;
        }
        
        if (recent.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-receipt"></i>
                    <p>Chưa có giao dịch nào</p>
                </div>
            `;
            return;
        }
        
        container.innerHTML = recent.map(t => {
            const transactionType = t.transactionType || t.transaction_type || t.type;
            const date = t.date || t.createdAt || t.created_at;
            const isWithdraw = transactionType === 'Withdraw' || transactionType === 'WITHDRAW';
            
            return `
            <div class="transaction-item">
                <div class="transaction-icon ${isWithdraw ? 'expense' : 'income'}">
                    <i class="fas fa-${isWithdraw ? 'arrow-down' : 'arrow-up'}"></i>
                </div>
                <div class="transaction-info">
                    <div class="transaction-title">${t.purpose || 'Không có mục đích'}</div>
                    <div class="transaction-date">${formatFundDate(date)}</div>
                </div>
                <div class="transaction-amount ${isWithdraw ? 'negative' : 'positive'}">
                    ${isWithdraw ? '-' : '+'} ${formatFundCurrency(t.amount)}
                </div>
            </div>
            `;
        }).join('');
        
    } catch (error) {
        console.error('Error loading recent transactions:', error);
        const container = document.getElementById('recentTransactions');
        if (container) {
            container.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-exclamation-triangle"></i>
                    <p>Không thể tải giao dịch</p>
                </div>
            `;
        }
    }
}

// Load các yêu cầu rút tiền cần vote (của các thành viên khác trong nhóm)
async function loadPendingVoteRequests() {
    try {
        console.log('🔍 Loading pending vote requests for user:', CURRENT_USER_ID);
        
        // Lấy danh sách các nhóm mà user tham gia
        const groupsResponse = await fetch(`/api/groups/user/${CURRENT_USER_ID}`);
        if (!groupsResponse.ok) {
            console.error('❌ Failed to load user groups');
            updatePendingVoteDisplay([]);
            return;
        }
        
        const groups = await groupsResponse.json();
        console.log('📋 User groups:', groups);
        
        const allPendingRequests = [];
        
        // Với mỗi nhóm, lấy fund và pending requests
        for (const group of groups) {
            try {
                // Lấy fund của nhóm
                const fundResponse = await fetch(`${API.FUND}/group/${group.groupId}`);
                if (!fundResponse.ok) continue;
                
                const fund = await fundResponse.json();
                if (!fund || !fund.fundId) continue;
                
                const fundId = fund.fundId;
                
                // Lấy pending requests của fund này
                const pendingUrl = `/api/funds/${fundId}/pending-requests`;
                console.log(`🔍 Fetching pending requests from: ${pendingUrl}`);
                const requestsResponse = await fetch(pendingUrl);
                if (!requestsResponse.ok) continue;
                
                const requests = await requestsResponse.json();
                if (!Array.isArray(requests)) continue;
                
                console.log(`📋 Found ${requests.length} pending requests for fund ${fundId}`);
                
                // Filter: chỉ các withdrawal requests không phải của user này
                requests.forEach(req => {
                    const transactionType = req.transactionType || req.transaction_type;
                    const status = req.status || req.transaction_status;
                    const userId = req.userId || req.user_id || req.createdBy;
                    
                    const isWithdraw = transactionType === 'Withdraw' || transactionType === 'WITHDRAW';
                    const isPending = status === 'Pending' || status === 'PENDING';
                    const isNotMyRequest = userId !== CURRENT_USER_ID && userId !== parseInt(CURRENT_USER_ID);
                    
                    if (isWithdraw && isPending && isNotMyRequest) {
                        allPendingRequests.push({
                            ...req,
                            groupName: group.groupName || group.group_name || `Nhóm ${group.groupId}`,
                            groupId: group.groupId,
                            fundId: fundId,
                            requesterId: userId
                        });
                    }
                });
            } catch (e) {
                console.warn(`Error loading requests for group ${group.groupId}:`, e);
            }
        }
        
        console.log('✅ Pending vote requests:', allPendingRequests);
        
        // Kiểm tra xem có yêu cầu mới không (so với lần trước)
        // Chỉ hiển thị thông báo nếu:
        // 1. Có yêu cầu mới (số lượng tăng) và đã có yêu cầu trước đó - để tránh thông báo khi lần đầu load trang
        // HOẶC đang ở trang Fund và có yêu cầu (để user biết ngay khi vào trang)
        if (allPendingRequests.length > lastPendingVoteCount) {
            if (lastPendingVoteCount > 0) {
                // Có yêu cầu mới được tạo
                const newCount = allPendingRequests.length - lastPendingVoteCount;
                showToast(`🔔 Có ${newCount} yêu cầu rút tiền mới cần bạn bỏ phiếu!`, 'info');
            } else if (allPendingRequests.length > 0 && currentPage === 'fund') {
                // Lần đầu vào trang Fund và có yêu cầu đang chờ
                showToast(`🔔 Có ${allPendingRequests.length} yêu cầu rút tiền đang chờ bạn bỏ phiếu!`, 'info');
            }
        }
        lastPendingVoteCount = allPendingRequests.length;
        
        updatePendingVoteDisplay(allPendingRequests);
        
    } catch (error) {
        console.error('❌ Error loading pending vote requests:', error);
        updatePendingVoteDisplay([]);
    }
}

/**
 * Hiển thị danh sách các withdrawal requests cần vote
 */
function updatePendingVoteDisplay(requests) {
    const voteSection = document.getElementById('pendingVoteSection');
    const voteBadge = document.getElementById('pendingVoteBadge');
    const voteBody = document.getElementById('pendingVoteBody');
    
    if (!voteSection || !voteBadge || !voteBody) {
        console.warn('⚠️ Pending vote section elements not found');
        return;
    }
    
    // Cập nhật badge
    voteBadge.textContent = requests.length;
    
    // Hiển thị/ẩn section
    if (requests.length === 0) {
        voteSection.style.display = 'none';
        return;
    }
    
    // Hiển thị section
    voteSection.style.display = 'block';
    
    // Render danh sách yêu cầu
    voteBody.innerHTML = requests.map(req => {
        const date = req.date || req.createdAt || req.created_at;
        const transactionId = req.transactionId || req.transaction_id;
        const amount = req.amount || 0;
        const purpose = req.purpose || '-';
        const requesterId = req.requesterId || req.userId || req.user_id;
        const groupName = req.groupName || `Nhóm ${req.groupId}`;
        const fundId = req.fundId;
        
        return `
        <tr>
            <td>
                <strong>User #${requesterId}</strong>
            </td>
            <td>${formatFundDate(date)}</td>
            <td class="amount negative">
                ${formatFundCurrency(amount)}
            </td>
            <td>${purpose}</td>
            <td>${groupName}</td>
            <td>
                <div style="display: flex; gap: 0.5rem;">
                    <button class="btn btn-sm btn-success" onclick="voteOnWithdrawRequest(${transactionId}, ${fundId}, true)" title="Đồng ý">
                        <i class="fas fa-check"></i> Đồng ý
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="voteOnWithdrawRequest(${transactionId}, ${fundId}, false)" title="Từ chối">
                        <i class="fas fa-times"></i> Từ chối
                    </button>
                </div>
            </td>
        </tr>
        `;
    }).join('');
}

/**
 * Vote cho withdrawal request (approve hoặc reject)
 */
async function voteOnWithdrawRequest(transactionId, fundId, approve) {
    if (!confirm(approve 
        ? 'Bạn có chắc chắn muốn đồng ý yêu cầu rút tiền này không?'
        : 'Bạn có chắc chắn muốn từ chối yêu cầu rút tiền này không?')) {
        return;
    }
    
    try {
        const url = `${API.FUND}/transactions/${transactionId}/vote`;
        
        console.log(`🗳️ Voting ${approve ? 'approve' : 'reject'} for transaction ${transactionId}`);
        
        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                transactionId: transactionId,
                userId: CURRENT_USER_ID,
                approve: approve
            })
        });
        
        if (!response.ok) {
            let errorData;
            try {
                errorData = await response.json();
            } catch (e) {
                const errorText = await response.text();
                errorData = { error: errorText };
            }
            throw new Error(errorData.error || 'Failed to vote');
        }
        
        const result = await response.json();
        console.log('✅ Vote result:', result);
        
        showToast(approve 
            ? '✅ Bạn đã đồng ý yêu cầu rút tiền này'
            : '❌ Bạn đã từ chối yêu cầu rút tiền này', 'success');
        
        // Reload data
        loadPendingVoteRequests();
        loadFundStats();
        loadMyPendingRequests();
        loadTransactionHistory();
        
    } catch (error) {
        console.error('Error voting:', error);
        showToast('❌ Lỗi: ' + error.message, 'error');
    }
}

async function loadTransactionHistory() {
    try {
        const statusEl = document.getElementById('filterStatus');
        const typeEl = document.getElementById('filterType');
        const status = statusEl ? statusEl.value : '';
        const type = typeEl ? typeEl.value : '';
        
        let url = `${API.FUND}/transactions?userId=${CURRENT_USER_ID}`;
        if (status) url += `&status=${status}`;
        if (type) url += `&type=${type}`;
        
        const response = await fetch(url);
        if (!response.ok) throw new Error('Failed to load transactions');
        
        const transactions = await response.json();
        
        updateTransactionTable(transactions);
        
    } catch (error) {
        console.error('Error loading transaction history:', error);
    }
}

function updateTransactionTable(transactions) {
    const tbody = document.getElementById('transactionsTableBody');
    if (!tbody) return;
    
    if (transactions.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="empty-table">
                    <div class="empty-state">
                        <i class="fas fa-receipt"></i>
                        <p>Không có giao dịch nào</p>
                    </div>
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = transactions.map(t => `
        <tr>
            <td>${formatFundDate(t.createdAt)}</td>
            <td>
                <span class="badge ${t.type === 'Deposit' ? 'badge-success' : 'badge-warning'}">
                    ${t.type === 'Deposit' ? '📥 Nạp tiền' : '📤 Rút tiền'}
                </span>
            </td>
            <td>${t.purpose || '-'}</td>
            <td class="amount ${t.type === 'Withdraw' ? 'negative' : 'positive'}">
                ${formatFundCurrency(t.amount)}
            </td>
            <td>
                <span class="badge badge-${getFundStatusClass(t.status)}">
                    ${getFundStatusIcon(t.status)} ${getFundStatusText(t.status)}
                </span>
            </td>
            <td>${t.createdByName || 'Unknown'}</td>
        </tr>
    `).join('');
}

// Modal functions
function openDepositModal() {
    const modal = document.getElementById('depositModal');
    if (modal) {
        modal.classList.add('show');
        modal.style.display = 'flex';
        const form = document.getElementById('depositForm');
        if (form) form.reset();
        
        // Reload groups để đảm bảo chỉ hiển thị nhóm user đã tham gia
        loadFundGroups();
    }
}

function closeDepositModal() {
    const modal = document.getElementById('depositModal');
    if (modal) {
        modal.classList.remove('show');
        modal.style.display = 'none';
    }
}

async function handleDeposit(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const groupId = parseInt(formData.get('groupId'));
    
    // Lấy fundId từ data attribute của option đã chọn
    const selectedOption = e.target.querySelector(`option[value="${groupId}"]`);
    let fundId = selectedOption ? selectedOption.getAttribute('data-fund-id') : null;
    
    try {
        // Nếu chưa có fund, tạo fund mới trước
        if (!fundId || fundId === '') {
            const createResponse = await fetch(`${API.FUND}/group/${groupId}/create`, {
                method: 'POST'
            });
            
            if (createResponse.ok) {
                const newFund = await createResponse.json();
                fundId = newFund.fundId;
            } else {
                throw new Error('Không thể tạo quỹ mới');
            }
        }
        
        const data = {
            fundId: parseInt(fundId),
            userId: CURRENT_USER_ID,
            amount: parseFloat(formData.get('amount')),
            purpose: formData.get('purpose')
        };
        
        const response = await fetch(`${API.FUND}/deposit`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        
        const result = await response.json();
        
        if (!response.ok) {
            throw new Error(result.error || result.message || 'Failed to deposit');
        }
        
        if (result.success) {
            showToast('✅ Nạp tiền thành công!', 'success');
            closeDepositModal();
            
            // Reload data
            loadFundGroups();
            loadFundStats();
            loadRecentTransactions();
            loadTransactionHistory();
        } else {
            throw new Error(result.message || 'Unknown error');
        }
        
    } catch (error) {
        console.error('Error depositing:', error);
        showToast('❌ Lỗi: ' + error.message, 'error');
    }
}

function openWithdrawVoteModal() {
    const modal = document.getElementById('withdrawVoteModal');
    if (modal) {
        modal.classList.add('show');
        modal.style.display = 'flex';
        const form = document.getElementById('withdrawVoteForm');
        if (form) form.reset();
        
        // Reset số dư về 0 khi mở modal
        const availableBalanceEl = document.getElementById('availableBalance');
        if (availableBalanceEl) {
            availableBalanceEl.textContent = formatFundCurrency(0);
        }
        
        // Reload groups để đảm bảo chỉ hiển thị nhóm user đã tham gia
        loadFundGroups();
    }
}

function closeWithdrawVoteModal() {
    const modal = document.getElementById('withdrawVoteModal');
    if (modal) {
        modal.classList.remove('show');
        modal.style.display = 'none';
    }
}

async function loadAvailableBalance() {
    try {
        // Gọi API với userId để chỉ lấy số dư của các nhóm mà user tham gia
        const response = await fetch(`${API.FUND}/stats?userId=${CURRENT_USER_ID}`);
        if (!response.ok) throw new Error('Failed to load balance');
        
        const stats = await response.json();
        const availableBalanceEl = document.getElementById('availableBalance');
        if (availableBalanceEl) {
            availableBalanceEl.textContent = formatFundCurrency(stats.totalBalance);
        }
    } catch (error) {
        console.error('Error loading balance:', error);
    }
}

/**
 * Load số dư của một nhóm cụ thể khi chọn nhóm trong dropdown rút tiền
 */
async function loadFundBalanceByGroupId(groupId) {
    try {
        const response = await fetch(`${API.FUND}/group/${groupId}`);
        if (!response.ok) throw new Error('Failed to load fund balance');
        
        const fund = await response.json();
        const availableBalanceEl = document.getElementById('availableBalance');
        if (availableBalanceEl) {
            const currentBalance = fund.currentBalance || 0;
            availableBalanceEl.textContent = formatFundCurrency(currentBalance);
        }
    } catch (error) {
        console.error('Error loading fund balance for groupId:', groupId, error);
        const availableBalanceEl = document.getElementById('availableBalance');
        if (availableBalanceEl) {
            availableBalanceEl.textContent = formatFundCurrency(0);
        }
    }
}

async function handleWithdrawVote(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const groupId = parseInt(formData.get('groupId'));
    
    // Get fundId from selected option
    const selectedOption = e.target.querySelector(`option[value="${groupId}"]`);
    const fundId = selectedOption ? selectedOption.getAttribute('data-fund-id') : null;
    
    if (!fundId) {
        showToast('Nhóm này chưa có quỹ', 'error');
        return;
    }
    
    const data = {
        fundId: parseInt(fundId),
        userId: CURRENT_USER_ID,
        amount: parseFloat(formData.get('amount')),
        purpose: formData.get('purpose'),
        receiptUrl: formData.get('receiptUrl') || null
    };
    
    try {
        const response = await fetch(`${API.FUND}/withdraw/request`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        
        if (!response.ok) throw new Error('Failed to create withdrawal request');
        
        const result = await response.json();
        
        if (result.success) {
            showToast('🗳️ Phiếu bỏ phiếu đã được tạo! Các thành viên sẽ bỏ phiếu trong 3 ngày.', 'success');
            closeWithdrawVoteModal();
            
            // Reload data
            loadFundStats();
            loadMyPendingRequests();
            loadTransactionHistory();
        } else {
            throw new Error(result.message || 'Unknown error');
        }
        
    } catch (error) {
        console.error('Error creating withdrawal request:', error);
        showToast('❌ Lỗi: ' + error.message, 'error');
    }
}

function viewAllTransactions() {
    // Scroll to transaction table
    const table = document.getElementById('transactionsTableBody');
    if (table) {
        table.scrollIntoView({ behavior: 'smooth' });
    }
}

function viewTransactionDetail(transactionId) {
    showToast(`Xem chi tiết giao dịch #${transactionId}`, 'info');
}

async function cancelRequest(transactionId) {
    if (!confirm('Bạn có chắc muốn hủy yêu cầu này?')) return;
    
    try {
        // Đảm bảo userId luôn có giá trị
        const userId = CURRENT_USER_ID || 1;
        const url = `${API.FUND}/transactions/${transactionId}?userId=${userId}`;
        
        console.log('🗑️ Cancelling transaction:', { transactionId, userId, url });
        
        const response = await fetch(url, {
            method: 'DELETE'
        });
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            const errorText = await response.text().catch(() => '');
            console.error('❌ Delete failed:', { status: response.status, errorData, errorText });
            throw new Error(errorData.error || errorData.message || errorText || 'Failed to cancel request');
        }
        
        const result = await response.json();
        console.log('✅ Cancel success:', result);
        showToast('✅ Đã hủy yêu cầu', 'success');
        
        // Reload data
        loadFundStats();
        loadMyPendingRequests();
        loadTransactionHistory();
        
    } catch (error) {
        console.error('Error canceling request:', error);
        showToast('❌ Lỗi: ' + error.message, 'error');
    }
}

// Fund utility functions
function getFundStatusClass(status) {
    const map = {
        'Pending': 'warning',
        'Approved': 'info',
        'Rejected': 'danger',
        'Completed': 'success'
    };
    return map[status] || 'secondary';
}

function getFundStatusText(status) {
    const map = {
        'Pending': 'Chờ duyệt',
        'Approved': 'Đã duyệt',
        'Rejected': 'Từ chối',
        'Completed': 'Hoàn tất'
    };
    return map[status] || status;
}

function getFundStatusIcon(status) {
    const map = {
        'Pending': '⏳',
        'Approved': '✅',
        'Rejected': '❌',
        'Completed': '✔️'
    };
    return map[status] || '';
}

function formatFundCurrency(amount) {
    if (!amount) return '0 VNĐ';
    return new Intl.NumberFormat('vi-VN').format(amount) + ' VNĐ';
}

function formatFundDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', { 
        year: 'numeric', 
        month: '2-digit', 
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// ============ BROWSE GROUPS PAGE ============
let allGroups = [];
let myGroupIds = [];

async function loadBrowseGroupsPage() {
    try {
        // Load all groups and user's groups
        await Promise.all([
            loadAllGroups(),
            loadUserGroups()
        ]);
        
        // Initialize search and filter
        initBrowseGroupsFilters();
        
        // Render groups
        renderBrowseGroups();
        
    } catch (error) {
        console.error('Error loading browse groups page:', error);
        showToast('Lỗi khi tải danh sách nhóm', 'error');
    }
}

async function loadAllGroups() {
    try {
        const response = await fetch(API.GROUPS);
        if (!response.ok) throw new Error('Failed to load groups');
        
        allGroups = await response.json();
        console.log(`📦 Loaded ${allGroups.length} groups:`, allGroups);
        
    } catch (error) {
        console.error('Error loading all groups:', error);
        allGroups = [];
    }
}

async function loadUserGroups() {
    try {
        const response = await fetch(`${API.GROUPS}/user/${CURRENT_USER_ID}`);
        if (!response.ok) throw new Error('Failed to load user groups');
        
        const userGroups = await response.json();
        myGroupIds = userGroups.map(g => g.groupId);
        console.log(`👤 User ${CURRENT_USER_ID} is member of groups:`, myGroupIds);
        
    } catch (error) {
        console.error('Error loading user groups:', error);
        myGroupIds = [];
    }
}

function initBrowseGroupsFilters() {
    const searchInput = document.getElementById('group-search');
    const statusFilter = document.getElementById('group-status-filter');
    
    if (searchInput) {
        searchInput.addEventListener('input', renderBrowseGroups);
    }
    
    if (statusFilter) {
        statusFilter.addEventListener('change', renderBrowseGroups);
    }
}

function renderBrowseGroups() {
    const container = document.getElementById('browse-groups-grid');
    if (!container) return;
    
    const searchTerm = document.getElementById('group-search')?.value.toLowerCase() || '';
    const statusFilter = document.getElementById('group-status-filter')?.value || 'all';
    
    // Filter groups
    let filteredGroups = allGroups.filter(group => {
        // Search filter
        const matchesSearch = !searchTerm || 
            group.groupName.toLowerCase().includes(searchTerm);
        
        // Status filter
        const matchesStatus = statusFilter === 'all' || 
            group.status === statusFilter;
        
        return matchesSearch && matchesStatus;
    });
    
    // Render groups
    if (filteredGroups.length === 0) {
        container.innerHTML = `
            <div class="empty-state" style="grid-column: 1 / -1;">
                <i class="fas fa-search"></i>
                <p>Không tìm thấy nhóm nào</p>
            </div>
        `;
        return;
    }
    
    container.innerHTML = filteredGroups.map(group => {
        const isMember = myGroupIds.includes(group.groupId);
        const statusBadge = group.status === 'Active' 
            ? '<span class="badge badge-success">Đang hoạt động</span>'
            : '<span class="badge badge-warning">Tạm ngưng</span>';
        
        return `
            <div class="group-card">
                <div class="group-card-header">
                    <h3>${escapeHtml(group.groupName)}</h3>
                    ${statusBadge}
                </div>
                <div class="group-card-body">
                    <div class="group-info-item">
                        <i class="fas fa-user-shield"></i>
                        <span>Quản lý bởi: User #${group.adminId}</span>
                    </div>
                    <div class="group-info-item">
                        <i class="fas fa-users"></i>
                        <span>${group.memberCount || 0} thành viên</span>
                    </div>
                    <div class="group-info-item">
                        <i class="fas fa-car"></i>
                        <span>Xe #${group.vehicleId || 'N/A'}</span>
                    </div>
                    <div class="group-info-item">
                        <i class="fas fa-vote-yea"></i>
                        <span>${group.voteCount || 0} phiếu bỏ phiếu</span>
                    </div>
                    ${group.createdAt ? `
                    <div class="group-info-item">
                        <i class="fas fa-calendar"></i>
                        <span>Thành lập: ${formatDate(group.createdAt)}</span>
                    </div>
                    ` : ''}
                </div>
                <div class="group-card-footer">
                    ${isMember ? `
                        <button class="btn btn-success" disabled>
                            <i class="fas fa-check"></i> Đã tham gia
                        </button>
                    ` : `
                        <button class="btn btn-primary join-group-btn" data-group-id="${group.groupId}">
                            <i class="fas fa-user-plus"></i> Tham gia nhóm
                        </button>
                    `}
                </div>
            </div>
        `;
    }).join('');
}

function openJoinGroupModal(groupId) {
    console.log('openJoinGroupModal called with groupId:', groupId, 'type:', typeof groupId);
    console.log('allGroups:', allGroups);
    
    // Convert to number if needed
    const id = typeof groupId === 'string' ? parseInt(groupId) : groupId;
    
    const group = allGroups.find(g => g.groupId === id || g.groupId === groupId);
    if (!group) {
        console.error('Group not found. ID:', id, 'Available groups:', allGroups.map(g => g.groupId));
        showToast('Không tìm thấy thông tin nhóm', 'error');
        return;
    }
    
    console.log('Found group:', group);
    
    // Set hidden fields
    const groupIdInput = document.getElementById('join-group-id');
    const userIdInput = document.getElementById('join-user-id');
    
    if (!groupIdInput || !userIdInput) {
        console.error('Modal inputs not found');
        showToast('Lỗi: Không tìm thấy form', 'error');
        return;
    }
    
    groupIdInput.value = id;
    userIdInput.value = CURRENT_USER_ID;
    
    // Set group info
    const infoDiv = document.getElementById('join-group-info');
    if (infoDiv) {
        infoDiv.innerHTML = `
            <div><strong>Tên nhóm:</strong> ${escapeHtml(group.groupName)}</div>
            <div><strong>Thành viên hiện tại:</strong> ${group.memberCount || 0}</div>
            <div><strong>Trạng thái:</strong> ${group.status === 'Active' ? 'Đang hoạt động' : 'Tạm ngưng'}</div>
        `;
    }
    
    // Reset form
    const ownershipInput = document.getElementById('joinOwnershipPercent');
    if (ownershipInput) {
        ownershipInput.value = '';
    }
    
    // Show modal
    const modal = document.getElementById('joinGroupModal');
    if (modal) {
        modal.classList.add('active');
        console.log('Modal opened');
        
        // Ensure submit button handler is bound
        const submitBtn = document.getElementById('joinGroupSubmitBtn');
        if (submitBtn) {
            console.log('🔵 Binding submit button handler');
            
            // Remove all existing click listeners by replacing the button
            // Create a temporary marker to identify old listeners
            const oldBtn = submitBtn;
            const newBtn = oldBtn.cloneNode(true);
            
            // Replace the button
            oldBtn.parentNode.replaceChild(newBtn, oldBtn);
            
            // Get the new button reference
            const button = document.getElementById('joinGroupSubmitBtn');
            
            // Ensure button is enabled
            button.disabled = false;
            button.style.pointerEvents = 'auto';
            button.style.cursor = 'pointer';
            
            // Bind click handler with detailed logging
            // Handle both button click and icon click
            const clickHandler = function(e) {
                e.preventDefault();
                e.stopPropagation();
                console.log('🔵 Submit button clicked!');
                console.log('🔵 Event details:', {
                    type: e.type,
                    target: e.target,
                    currentTarget: e.currentTarget,
                    buttonId: e.currentTarget.id,
                    clickedElement: e.target.tagName,
                    clickedElementClass: e.target.className
                });
                
                // If clicked on icon, find the button parent
                let targetButton = e.target;
                if (targetButton.tagName === 'I' || targetButton.tagName === 'SPAN') {
                    targetButton = targetButton.closest('button');
                }
                
                if (!targetButton || targetButton.id !== 'joinGroupSubmitBtn') {
                    console.warn('⚠️ Click not on button, ignoring');
                    return;
                }
                
                console.log('🔵 Calling handleJoinGroup...');
                
                // Try to trigger form submit as primary method
                const form = document.getElementById('joinGroupForm');
                if (form) {
                    console.log('🔵 Triggering form submit...');
                    // Create and dispatch submit event
                    const submitEvent = new Event('submit', { bubbles: true, cancelable: true });
                    form.dispatchEvent(submitEvent);
                } else {
                    // Fallback to direct handler call
                    console.log('🔵 Form not found, calling handleJoinGroup directly...');
                    handleJoinGroup(e);
                }
            };
            
            // Set onclick attribute directly as primary method (most reliable)
            button.onclick = function(e) {
                console.log('🔵 onclick attribute triggered!');
                clickHandler(e);
            };
            
            // Add click listener to button as backup
            button.addEventListener('click', clickHandler, { once: false, capture: false });
            
            // Also add mousedown/pointerdown as backup
            button.addEventListener('mousedown', function(e) {
                console.log('🔵 Button mousedown event');
            });
            
            // Add click listener to icon if exists
            const icon = button.querySelector('i');
            if (icon) {
                console.log('🔵 Found icon, adding click handler to icon too');
                icon.addEventListener('click', function(e) {
                    console.log('🔵 Icon clicked!');
                    clickHandler(e);
                }, { once: false, capture: false });
                icon.style.pointerEvents = 'auto';
                icon.style.cursor = 'pointer';
            }
            
            // Also try to trigger via form submit as backup
            const form = document.getElementById('joinGroupForm');
            if (form) {
                console.log('🔵 Also binding form submit handler');
                const formSubmitHandler = function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    console.log('🔵 Form submit triggered');
                    handleJoinGroup(e);
                };
                form.addEventListener('submit', formSubmitHandler, { once: false });
            }
            
            console.log('✅ Submit button handler bound successfully');
            console.log('✅ Button state:', {
                id: button.id,
                disabled: button.disabled,
                type: button.type,
                hasOnclick: !!button.onclick
            });
            
            // Debug: Check button visibility and clickability
            setTimeout(() => {
                const btn = document.getElementById('joinGroupSubmitBtn');
                if (btn) {
                    const styles = window.getComputedStyle(btn);
                    const rect = btn.getBoundingClientRect();
                    console.log('🔍 Button debug info:', {
                        display: styles.display,
                        visibility: styles.visibility,
                        pointerEvents: styles.pointerEvents,
                        opacity: styles.opacity,
                        zIndex: styles.zIndex,
                        position: styles.position,
                        top: rect.top,
                        left: rect.left,
                        width: rect.width,
                        height: rect.height,
                        visible: rect.width > 0 && rect.height > 0
                    });
                    
                    // Check if button is covered by another element
                    const elementAtPoint = document.elementFromPoint(
                        rect.left + rect.width / 2,
                        rect.top + rect.height / 2
                    );
                    console.log('🔍 Element at button center:', {
                        tagName: elementAtPoint?.tagName,
                        id: elementAtPoint?.id,
                        className: elementAtPoint?.className,
                        isButton: elementAtPoint === btn || btn.contains(elementAtPoint)
                    });
                }
            }, 100);
            
            // Test click programmatically after a short delay
            setTimeout(() => {
                const btn = document.getElementById('joinGroupSubmitBtn');
                if (btn) {
                    console.log('🧪 Testing programmatic click...');
                    // Don't actually trigger, just log that we can access it
                    console.log('✅ Button accessible for programmatic click');
                }
            }, 200);
        } else {
            console.warn('⚠️ Submit button not found when opening modal');
        }
    } else {
        console.error('Modal not found');
    }
}

function closeJoinGroupModal() {
    document.getElementById('joinGroupModal').classList.remove('active');
    document.getElementById('joinGroupForm').reset();
}

// Initialize join group form handler
document.addEventListener('DOMContentLoaded', function() {
    console.log('🔵 DOMContentLoaded - Initializing join group form...');
    
    const joinGroupForm = document.getElementById('joinGroupForm');
    console.log('Form found:', joinGroupForm ? 'YES' : 'NO');
    
    if (joinGroupForm) {
        // Bind form submit handler
        joinGroupForm.addEventListener('submit', function(e) {
            e.preventDefault();
            e.stopPropagation();
            console.log('🔵 Form submit event triggered');
            handleJoinGroup(e);
        });
        console.log('✅ Form submit event listener added');
        
        // Also bind directly to submit button as backup
        const submitBtn = document.getElementById('joinGroupSubmitBtn');
        if (submitBtn) {
            console.log('✅ Submit button found');
            submitBtn.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                console.log('🔵 Submit button clicked (backup handler)');
                handleJoinGroup(e);
            });
        } else {
            console.warn('⚠️ Submit button not found');
        }
    } else {
        console.error('❌ joinGroupForm not found in DOM');
    }
    
    // Event delegation for join group buttons (handles dynamically created buttons)
    document.addEventListener('click', function(event) {
        // Skip if clicking on submit button or inside modal footer
        const submitBtn = event.target.closest('#joinGroupSubmitBtn');
        const modalFooter = event.target.closest('.modal-footer');
        if (submitBtn || (modalFooter && event.target.closest('button'))) {
            // Let the button's own handlers handle this
            return;
        }
        
        // Check if clicked element is a join group button or inside one
        const joinBtn = event.target.closest('.join-group-btn');
        if (joinBtn) {
            event.preventDefault();
            const groupId = joinBtn.getAttribute('data-group-id');
            if (groupId) {
                console.log('Join button clicked, groupId:', groupId);
                openJoinGroupModal(parseInt(groupId));
            }
        }
        
        // Close modal when clicking outside
        if (event.target.id === 'joinGroupModal') {
            closeJoinGroupModal();
        }
    });
});

// Flag to prevent duplicate calls
let isJoiningGroup = false;

async function handleJoinGroup(e) {
    e.preventDefault();
    e.stopPropagation();
    
    // Prevent duplicate calls
    if (isJoiningGroup) {
        console.log('⚠️ handleJoinGroup already in progress, ignoring duplicate call');
        return;
    }
    
    isJoiningGroup = true;
    console.log('🔵 handleJoinGroup called');
    
    const groupIdInput = document.getElementById('join-group-id');
    const userIdInput = document.getElementById('join-user-id');
    const ownershipInput = document.getElementById('joinOwnershipPercent');
    
    console.log('Form inputs:', {
        groupIdInput: groupIdInput ? groupIdInput.value : 'NOT FOUND',
        userIdInput: userIdInput ? userIdInput.value : 'NOT FOUND',
        ownershipInput: ownershipInput ? ownershipInput.value : 'NOT FOUND'
    });
    
    const groupId = parseInt(groupIdInput?.value);
    const userId = parseInt(userIdInput?.value);
    const ownershipPercent = parseFloat(ownershipInput?.value);
    
    console.log('Parsed values:', { groupId, userId, ownershipPercent });
    
    // Validation
    if (!groupId || isNaN(groupId) || groupId <= 0) {
        console.error('❌ Validation failed: Invalid groupId', { groupId });
        showToast('Lỗi: Không tìm thấy thông tin nhóm', 'error');
        isJoiningGroup = false;
        return;
    }
    
    if (!userId || isNaN(userId) || userId <= 0) {
        console.error('❌ Validation failed: Invalid userId', { userId });
        showToast('Lỗi: Không tìm thấy thông tin người dùng', 'error');
        isJoiningGroup = false;
        return;
    }
    
    if (!ownershipInput || !ownershipInput.value || isNaN(ownershipPercent)) {
        console.error('❌ Validation failed: Invalid ownershipPercent', { ownershipPercent, inputValue: ownershipInput?.value });
        showToast('Vui lòng nhập tỷ lệ sở hữu (từ 0.01% đến 100%)', 'error');
        isJoiningGroup = false;
        return;
    }
    
    if (ownershipPercent <= 0 || ownershipPercent > 100) {
        console.error('❌ Ownership percent out of range:', ownershipPercent);
        showToast('Tỷ lệ sở hữu phải từ 0.01% đến 100%', 'error');
        isJoiningGroup = false;
        return;
    }
    
    try {
        console.log('📡 Checking current group members...');
        // Check current ownership total
        const membersUrl = `${API.GROUPS}/${groupId}/members`;
        console.log('Fetching:', membersUrl);
        
        const membersResponse = await fetch(membersUrl);
        console.log('Members response status:', membersResponse.status, membersResponse.ok);
        
        if (!membersResponse.ok) {
            const errorText = await membersResponse.text();
            console.error('❌ Failed to load group members:', errorText);
            throw new Error('Failed to load group members');
        }
        
        const currentMembers = await membersResponse.json();
        console.log('Current members:', currentMembers);
        
        // Check if user is already a member
        const existingMember = currentMembers.find(m => m.userId === userId);
        if (existingMember) {
            console.log('⚠️ User is already a member:', existingMember);
            console.log('⚠️ Existing ownership:', existingMember.ownershipPercent);
            console.log('⚠️ New ownership request:', ownershipPercent);
            
            // If user is already a member with same ownership, just show success
            if (existingMember.ownershipPercent === ownershipPercent) {
                console.log('✅ User already has same ownership, treating as success');
                showToast('Bạn đã là thành viên của nhóm này với tỷ lệ sở hữu này rồi', 'info');
                closeJoinGroupModal();
                await loadBrowseGroupsPage();
                isJoiningGroup = false;
                return;
            }
            
            // If user wants to update ownership, allow it (backend will handle)
            console.log('⚠️ User wants to update ownership, proceeding...');
        }
        
        // Calculate total ownership excluding current user (if already a member)
        const currentTotal = currentMembers
            .filter(m => m.userId !== userId) // Exclude current user's existing ownership
            .reduce((sum, m) => sum + (m.ownershipPercent || 0), 0);
        console.log('Current total ownership (excluding current user):', currentTotal);
        console.log('Requested ownership:', ownershipPercent);
        console.log('Total would be:', currentTotal + ownershipPercent);
        
        if (currentTotal + ownershipPercent > 100) {
            console.error('❌ Total ownership exceeds 100%');
            showToast(`Tổng tỷ lệ sở hữu không được vượt quá 100%. Hiện tại: ${currentTotal.toFixed(2)}%`, 'error');
            isJoiningGroup = false; // Reset flag
            return;
        }
        
        // Join group
        const joinData = {
            userId: userId,
            role: 'Member',
            ownershipPercent: ownershipPercent
        };
        
        console.log('📤 Sending join request:', joinData);
        const joinUrl = `${API.GROUPS}/${groupId}/members`;
        console.log('POST URL:', joinUrl);
        
        const response = await fetch(joinUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(joinData)
        });
        
        console.log('Join response status:', response.status, response.ok);
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error('❌ Join failed. Status:', response.status);
            console.error('❌ Response body:', errorText);
            
            let errorMessage = 'Không thể tham gia nhóm';
            
            // Try to parse error response as JSON
            try {
                const errorData = JSON.parse(errorText);
                // Check multiple possible error message fields
                errorMessage = errorData.message || errorData.error || errorData.details || errorMessage;
                console.error('Parsed error data:', errorData);
            } catch (e) {
                // If not JSON, use text directly
                if (errorText && errorText.trim().length > 0) {
                    errorMessage = errorText;
                }
            }
            
            // Map specific status codes to user-friendly messages
            if (response.status === 404) {
                errorMessage = errorMessage || 'Không tìm thấy nhóm';
            } else if (response.status === 400) {
                if (!errorMessage || errorMessage === 'Không thể tham gia nhóm') {
                    errorMessage = 'Bạn đã là thành viên của nhóm này rồi hoặc dữ liệu không hợp lệ';
                }
            } else if (response.status === 500) {
                errorMessage = errorMessage || 'Lỗi server, vui lòng thử lại sau';
            }
            
            throw new Error(errorMessage);
        }
        
        const result = await response.json();
        console.log('✅ Successfully joined group:', result);
        
        showToast('Tham gia nhóm thành công!', 'success');
        closeJoinGroupModal();
        
        // Reload page to update groups list
        await loadBrowseGroupsPage();
        
    } catch (error) {
        console.error('❌ Error joining group:', error);
        console.error('Error stack:', error.stack);
        showToast(error.message || 'Có lỗi xảy ra khi tham gia nhóm', 'error');
    } finally {
        // Reset flag after completion
        isJoiningGroup = false;
    }
}

function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', { 
        year: 'numeric', 
        month: '2-digit', 
        day: '2-digit'
    });
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// ============ GROUP MANAGEMENT FUNCTIONS ============

let currentManagingGroupId = null;

function openManageGroupModal(groupId, groupName) {
    currentManagingGroupId = groupId;
    document.getElementById('manage-group-name').textContent = groupName;
    document.getElementById('manageGroupModal').classList.add('active');
    loadGroupMembers(groupId);
    
    // Reset add member form
    document.getElementById('addMemberForm').reset();
}

function closeManageGroupModal() {
    document.getElementById('manageGroupModal').classList.remove('active');
    currentManagingGroupId = null;
}

async function loadGroupMembers(groupId) {
    const container = document.getElementById('members-list-container');
    container.innerHTML = '<div class="loading-spinner"><i class="fas fa-spinner fa-spin"></i> Đang tải...</div>';
    
    try {
        const response = await fetch(`${API.GROUPS}/${groupId}/members`);
        if (!response.ok) throw new Error('Failed to load members');
        
        const members = await response.json();
        
        if (members.length === 0) {
            container.innerHTML = '<p style="text-align: center; color: var(--text-light);">Chưa có thành viên nào</p>';
            return;
        }
        
        // Calculate total ownership
        const totalOwnership = members.reduce((sum, m) => sum + (m.ownershipPercent || 0), 0);
        
        container.innerHTML = `
            <div class="members-summary">
                <span><strong>Tổng thành viên:</strong> ${members.length}</span>
                <span><strong>Tổng tỷ lệ sở hữu:</strong> ${totalOwnership.toFixed(2)}%</span>
            </div>
            <div class="members-table">
                <table>
                    <thead>
                        <tr>
                            <th>User ID</th>
                            <th>Quyền</th>
                            <th>Tỷ lệ sở hữu</th>
                            <th>Ngày tham gia</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${members.map(member => `
                            <tr>
                                <td>User #${member.userId}</td>
                                <td>
                                    <span class="badge ${member.role === 'Admin' ? 'badge-admin' : 'badge-member'}">
                                        ${member.role === 'Admin' ? '<i class="fas fa-crown"></i> Admin' : '<i class="fas fa-user"></i> Thành viên'}
                                    </span>
                                </td>
                                <td>${(member.ownershipPercent || 0).toFixed(2)}%</td>
                                <td>${member.joinedAt ? formatDate(member.joinedAt) : 'N/A'}</td>
                                <td>
                                    <div class="member-actions">
                                        ${member.userId !== CURRENT_USER_ID ? `
                                            <button class="btn btn-sm btn-primary" onclick="changeMemberRole(${groupId}, ${member.memberId}, '${member.role === 'Admin' ? 'Member' : 'Admin'}')" title="${member.role === 'Admin' ? 'Hạ quyền' : 'Thăng quyền'}">
                                                <i class="fas fa-${member.role === 'Admin' ? 'arrow-down' : 'arrow-up'}"></i>
                                                ${member.role === 'Admin' ? 'Hạ quyền' : 'Thăng Admin'}
                                            </button>
                                            <button class="btn btn-sm btn-danger" onclick="removeMember(${groupId}, ${member.memberId}, ${member.userId})" title="Xóa thành viên">
                                                <i class="fas fa-trash"></i> Xóa
                                            </button>
                                        ` : `
                                            <span class="text-muted">Bạn</span>
                                        `}
                                    </div>
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
        
    } catch (error) {
        console.error('Error loading group members:', error);
        container.innerHTML = '<div class="alert alert-danger"><i class="fas fa-exclamation-circle"></i> Lỗi khi tải danh sách thành viên</div>';
    }
}

// Initialize add member form handler
document.addEventListener('DOMContentLoaded', function() {
    const addMemberForm = document.getElementById('addMemberForm');
    if (addMemberForm) {
        addMemberForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            await addMember();
        });
    }
    
    // Close modal when clicking outside
    const manageGroupModal = document.getElementById('manageGroupModal');
    if (manageGroupModal) {
        manageGroupModal.addEventListener('click', function(e) {
            if (e.target === manageGroupModal) {
                closeManageGroupModal();
            }
        });
    }
});

async function addMember() {
    if (!currentManagingGroupId) return;
    
    const userId = parseInt(document.getElementById('newMemberUserId').value);
    const ownershipPercent = parseFloat(document.getElementById('newMemberOwnership').value);
    const role = document.getElementById('newMemberRole').value;
    
    if (!userId || isNaN(ownershipPercent)) {
        showToast('Vui lòng điền đầy đủ thông tin', 'error');
        return;
    }
    
    try {
        const response = await fetch(`${API.GROUPS}/${currentManagingGroupId}/members`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                currentUserId: CURRENT_USER_ID, // Thêm currentUserId để kiểm tra quyền Admin
                userId: userId,
                ownershipPercent: ownershipPercent,
                role: role
            })
        });
        
        const result = await response.json();
        
        if (!response.ok) {
            throw new Error(result.message || result.error || 'Failed to add member');
        }
        
        showToast(`Đã thêm User #${userId} vào nhóm thành công`, 'success');
        document.getElementById('addMemberForm').reset();
        await loadGroupMembers(currentManagingGroupId);
        
        // Reload groups list to update member count
        await loadMyGroups();
        
    } catch (error) {
        console.error('Error adding member:', error);
        showToast(error.message || 'Lỗi khi thêm thành viên', 'error');
    }
}

async function removeMember(groupId, memberId, userId) {
    if (!confirm(`Bạn có chắc chắn muốn xóa User #${userId} khỏi nhóm này?`)) {
        return;
    }
    
    try {
        // Thêm currentUserId vào query parameter để kiểm tra quyền Admin
        const response = await fetch(`${API.GROUPS}/${groupId}/members/${memberId}?currentUserId=${CURRENT_USER_ID}`, {
            method: 'DELETE'
        });
        
        const result = await response.json();
        
        if (!response.ok) {
            throw new Error(result.message || result.error || 'Failed to remove member');
        }
        
        showToast(`Đã xóa User #${userId} khỏi nhóm`, 'success');
        await loadGroupMembers(groupId);
        
        // Reload groups list to update member count
        await loadMyGroups();
        
    } catch (error) {
        console.error('Error removing member:', error);
        showToast(error.message || 'Lỗi khi xóa thành viên', 'error');
    }
}

async function changeMemberRole(groupId, memberId, newRole) {
    const roleText = newRole === 'Admin' ? 'thăng làm Admin' : 'hạ xuống thành viên';
    if (!confirm(`Bạn có chắc chắn muốn ${roleText}?`)) {
        return;
    }
    
    try {
        // First, get current member data
        const membersResponse = await fetch(`${API.GROUPS}/${groupId}/members`);
        if (!membersResponse.ok) throw new Error('Failed to fetch members');
        
        const members = await membersResponse.json();
        const member = members.find(m => m.memberId === memberId);
        
        if (!member) {
            throw new Error('Member not found');
        }
        
        // Update member with new role
        const response = await fetch(`${API.GROUPS}/${groupId}/members/${memberId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                currentUserId: CURRENT_USER_ID, // Thêm currentUserId để kiểm tra quyền Admin
                userId: member.userId,
                role: newRole,
                ownershipPercent: member.ownershipPercent
            })
        });
        
        const result = await response.json();
        
        if (!response.ok) {
            throw new Error(result.message || result.error || 'Failed to update member role');
        }
        
        showToast(`Đã ${newRole === 'Admin' ? 'thăng' : 'hạ'} quyền thành công`, 'success');
        await loadGroupMembers(groupId);
        
        // Update user role cache if it's current user
        if (member.userId === CURRENT_USER_ID) {
            userGroupRoles[groupId] = newRole;
        }
        
        // Reload groups list to update UI
        await loadMyGroups();
        
    } catch (error) {
        console.error('Error changing member role:', error);
        showToast('Lỗi khi thay đổi quyền', 'error');
    }
}

// ============ CREATE GROUP FUNCTIONS ============

function openCreateGroupModal() {
    // Reset form
    document.getElementById('createGroupForm').reset();
    document.getElementById('createGroupStatus').value = 'Active';
    
    // Show modal
    document.getElementById('createGroupModal').classList.add('active');
}

function closeCreateGroupModal() {
    document.getElementById('createGroupModal').classList.remove('active');
    document.getElementById('createGroupForm').reset();
}

// Initialize create group form submit handler
document.addEventListener('DOMContentLoaded', function() {
    const createGroupSubmitBtn = document.getElementById('createGroupSubmitBtn');
    const createGroupForm = document.getElementById('createGroupForm');
    
    if (createGroupSubmitBtn) {
        createGroupSubmitBtn.addEventListener('click', async function() {
            // Validate form
            if (!createGroupForm.checkValidity()) {
                createGroupForm.reportValidity();
                return;
            }
            
            const groupName = document.getElementById('createGroupName').value.trim();
            const vehicleId = document.getElementById('createGroupVehicleId').value;
            const ownershipPercent = document.getElementById('createGroupOwnershipPercent').value;
            const status = document.getElementById('createGroupStatus').value;
            
            if (!groupName) {
                showToast('Vui lòng nhập tên nhóm', 'error');
                return;
            }
            
            // Validate ownershipPercent nếu có nhập
            if (ownershipPercent) {
                const ownershipValue = parseFloat(ownershipPercent);
                if (isNaN(ownershipValue) || ownershipValue < 0 || ownershipValue > 100) {
                    showToast('Tỷ lệ sở hữu phải là số từ 0 đến 100', 'error');
                    return;
                }
            }
            
            try {
                // Tự động set adminId = CURRENT_USER_ID (người tạo nhóm)
                const groupData = {
                    groupName: groupName,
                    adminId: CURRENT_USER_ID, // ⭐ QUAN TRỌNG: User tạo nhóm tự động trở thành Admin
                    vehicleId: vehicleId ? parseInt(vehicleId) : null,
                    ownershipPercent: ownershipPercent ? parseFloat(ownershipPercent) : null, // Tỷ lệ sở hữu của Admin
                    status: status
                };
                
                const response = await fetch(API.GROUPS, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(groupData)
                });
                
                const result = await response.json();
                
                if (!response.ok) {
                    throw new Error(result.message || result.error || 'Failed to create group');
                }
                
                showToast(`Đã tạo nhóm "${groupName}" thành công! Bạn đã trở thành Admin của nhóm này.`, 'success');
                closeCreateGroupModal();
                
                // Reload groups list để hiển thị nhóm mới
                await loadMyGroups();
                
            } catch (error) {
                console.error('Error creating group:', error);
                showToast(error.message || 'Lỗi khi tạo nhóm', 'error');
            }
        });
    }
    
    // Close modal when clicking outside
    const createGroupModal = document.getElementById('createGroupModal');
    if (createGroupModal) {
        createGroupModal.addEventListener('click', function(e) {
            if (e.target === createGroupModal) {
                closeCreateGroupModal();
            }
        });
    }
});

