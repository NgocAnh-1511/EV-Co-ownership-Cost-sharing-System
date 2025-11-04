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
const CURRENT_USER_ID = parseInt(urlParams.get('userId')) || 1;

// Global State
let currentPage = 'home';

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
        await loadRecentTransactions();
        await loadTransactionHistory();
    } catch (error) {
        console.error('Error loading fund page:', error);
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
                    }
                } catch (e) {
                    console.warn(`No fund found for group ${group.groupId}`);
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
        const response = await fetch(`${API.FUND}/transactions?status=Pending`);
        if (!response.ok) throw new Error('Failed to load pending requests');
        
        const transactions = await response.json();
        
        // Filter only my requests
        const myRequests = transactions.filter(t => t.createdBy === CURRENT_USER_ID);
        
        updateMyPendingDisplay(myRequests);
        
    } catch (error) {
        console.error('Error loading my pending requests:', error);
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
        const response = await fetch(`${API.FUND}/transactions?status=Completed`);
        if (!response.ok) throw new Error('Failed to load transactions');
        
        const transactions = await response.json();
        
        // Take only last 5
        const recent = transactions.slice(0, 5);
        
        const container = document.getElementById('recentTransactions');
        if (!container) return;
        
        if (recent.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-receipt"></i>
                    <p>Chưa có giao dịch nào</p>
                </div>
            `;
            return;
        }
        
        container.innerHTML = recent.map(t => `
            <div class="transaction-item">
                <div class="transaction-icon ${t.type === 'Withdraw' ? 'expense' : 'income'}">
                    <i class="fas fa-${t.type === 'Withdraw' ? 'arrow-down' : 'arrow-up'}"></i>
                </div>
                <div class="transaction-info">
                    <div class="transaction-title">${t.purpose || 'Không có mục đích'}</div>
                    <div class="transaction-date">${formatFundDate(t.createdAt)}</div>
                </div>
                <div class="transaction-amount ${t.type === 'Withdraw' ? 'negative' : 'positive'}">
                    ${t.type === 'Withdraw' ? '-' : '+'} ${formatFundCurrency(t.amount)}
                </div>
            </div>
        `).join('');
        
    } catch (error) {
        console.error('Error loading recent transactions:', error);
    }
}

async function loadTransactionHistory() {
    try {
        const statusEl = document.getElementById('filterStatus');
        const typeEl = document.getElementById('filterType');
        const status = statusEl ? statusEl.value : '';
        const type = typeEl ? typeEl.value : '';
        
        let url = `${API.FUND}/transactions?`;
        if (status) url += `status=${status}&`;
        if (type) url += `type=${type}`;
        
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
        const response = await fetch(`${API.FUND}/transactions/${transactionId}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) throw new Error('Failed to cancel request');
        
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

