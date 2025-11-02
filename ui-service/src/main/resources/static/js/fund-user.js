// ========================================
// FUND USER JS - Giao diện User
// ========================================

const API_BASE_URL = '/api/fund';
const CURRENT_USER_ID = 1; // TODO: Get from session

// ========================================
// INITIALIZATION
// ========================================

document.addEventListener('DOMContentLoaded', function() {
    loadGroups();
    loadFundStats();
    loadMyPendingRequests();
    loadRecentTransactions();
    loadTransactionHistory();
    
    // Event listeners
    document.getElementById('depositForm').addEventListener('submit', handleDeposit);
    document.getElementById('withdrawVoteForm').addEventListener('submit', handleWithdrawVote);
    document.getElementById('filterStatus').addEventListener('change', loadTransactionHistory);
    document.getElementById('filterType').addEventListener('change', loadTransactionHistory);
    
    // Auto refresh every 30s
    setInterval(() => {
        loadGroups(); // Refresh group list to update fund status
        loadFundStats();
        loadMyPendingRequests();
        loadRecentTransactions();
    }, 30000);
});

// ========================================
// LOAD DATA
// ========================================

// Load groups for dropdowns
async function loadGroups() {
    try {
        const response = await fetch('/groups/api/all');
        if (!response.ok) throw new Error('Failed to load groups');
        
        const groups = await response.json();
        console.log('📦 [USER] Loaded groups:', groups);
        
        // Fetch fundId for each group
        const groupsWithFunds = await Promise.all(
            groups.map(async (group) => {
                try {
                    const fundResponse = await fetch(`/api/fund/group/${group.groupId}`);
                    if (fundResponse.ok) {
                        const fund = await fundResponse.json();
                        return {
                            ...group,
                            fundId: fund.fundId
                        };
                    }
                } catch (e) {
                    console.warn(`⚠️ No fund found for group ${group.groupId}`);
                }
                return group;
            })
        );
        
        console.log('💰 [USER] Groups with fund info:', groupsWithFunds);
        
        // Populate deposit dropdown - hiển thị tất cả nhóm (dùng groupId)
        const depositSelect = document.getElementById('depositGroup');
        if (depositSelect) {
            depositSelect.innerHTML = '<option value="">Chọn nhóm</option>' +
                groupsWithFunds
                    .map(g => `<option value="${g.groupId}" data-fund-id="${g.fundId || ''}">${g.groupName}${g.fundId ? '' : ' (chưa có quỹ)'}</option>`)
                    .join('');
            console.log('✅ Populated depositGroup dropdown');
        }
        
        // Populate withdraw dropdown - chỉ nhóm có quỹ mới rút được
        const withdrawSelect = document.getElementById('withdrawGroup');
        if (withdrawSelect) {
            withdrawSelect.innerHTML = '<option value="">Chọn nhóm</option>' +
                groupsWithFunds
                    .filter(g => g.fundId)
                    .map(g => `<option value="${g.groupId}" data-fund-id="${g.fundId}">${g.groupName}</option>`)
                    .join('');
            console.log('✅ Populated withdrawGroup dropdown');
        }
        
    } catch (error) {
        console.error('❌ Error loading groups:', error);
        
        // Restore empty state on error
        const depositSelect = document.getElementById('depositGroup');
        const withdrawSelect = document.getElementById('withdrawGroup');
        if (depositSelect) depositSelect.innerHTML = '<option value="">Không thể tải nhóm</option>';
        if (withdrawSelect) withdrawSelect.innerHTML = '<option value="">Không thể tải nhóm</option>';
    }
}

async function loadFundStats() {
    try {
        const response = await fetch(`${API_BASE_URL}/stats`);
        if (!response.ok) throw new Error('Failed to load stats');
        
        const stats = await response.json();
        
        // Update stats cards
        document.getElementById('totalBalance').textContent = formatCurrency(stats.totalBalance);
        document.getElementById('myDeposits').textContent = formatCurrency(stats.myDeposits || 0);
        document.getElementById('myWithdraws').textContent = formatCurrency(stats.myWithdraws || 0);
        document.getElementById('myPending').textContent = stats.myPendingCount || 0;
        
        // Update summary
        document.getElementById('summaryOpening').textContent = formatCurrency(stats.openingBalance);
        document.getElementById('summaryIncome').textContent = formatCurrency(stats.totalIncome);
        document.getElementById('summaryExpense').textContent = formatCurrency(stats.totalExpense);
        document.getElementById('summaryBalance').textContent = formatCurrency(stats.totalBalance);
        
    } catch (error) {
        console.error('Error loading stats:', error);
    }
}

async function loadMyPendingRequests() {
    try {
        const response = await fetch(`${API_BASE_URL}/transactions?status=Pending`);
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
    
    badge.textContent = requests.length;
    
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
            <td>${formatDate(t.createdAt)}</td>
            <td class="amount negative">
                ${formatCurrency(t.amount)}
            </td>
            <td>${t.purpose || '-'}</td>
            <td>
                <span class="badge badge-${getStatusClass(t.status)}">
                    ${getStatusIcon(t.status)} ${getStatusText(t.status)}
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
        const response = await fetch(`${API_BASE_URL}/transactions?status=Completed`);
        if (!response.ok) throw new Error('Failed to load transactions');
        
        const transactions = await response.json();
        
        // Take only last 5
        const recent = transactions.slice(0, 5);
        
        const container = document.getElementById('recentTransactions');
        
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
                    <div class="transaction-date">${formatDate(t.createdAt)}</div>
                </div>
                <div class="transaction-amount ${t.type === 'Withdraw' ? 'negative' : 'positive'}">
                    ${t.type === 'Withdraw' ? '-' : '+'} ${formatCurrency(t.amount)}
                </div>
            </div>
        `).join('');
        
    } catch (error) {
        console.error('Error loading recent transactions:', error);
    }
}

async function loadTransactionHistory() {
    try {
        const status = document.getElementById('filterStatus').value;
        const type = document.getElementById('filterType').value;
        
        let url = `${API_BASE_URL}/transactions?`;
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
            <td>${formatDate(t.createdAt)}</td>
            <td>
                <span class="badge ${t.type === 'Deposit' ? 'badge-success' : 'badge-warning'}">
                    ${t.type === 'Deposit' ? '📥 Nạp tiền' : '📤 Rút tiền'}
                </span>
            </td>
            <td>${t.purpose || '-'}</td>
            <td class="amount ${t.type === 'Withdraw' ? 'negative' : 'positive'}">
                ${formatCurrency(t.amount)}
            </td>
            <td>
                <span class="badge badge-${getStatusClass(t.status)}">
                    ${getStatusIcon(t.status)} ${getStatusText(t.status)}
                </span>
            </td>
            <td>${t.createdByName || 'Unknown'}</td>
        </tr>
    `).join('');
}

// ========================================
// MODAL HANDLERS
// ========================================

// Deposit Modal
function openDepositModal() {
    document.getElementById('depositModal').classList.add('show');
    document.getElementById('depositForm').reset();
}

function closeDepositModal() {
    document.getElementById('depositModal').classList.remove('show');
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
            console.log(`🆕 Creating new fund for group ${groupId}...`);
            const createResponse = await fetch(`/api/fund/group/${groupId}/create`, {
                method: 'POST'
            });
            
            if (createResponse.ok) {
                const newFund = await createResponse.json();
                fundId = newFund.fundId;
                console.log(`✅ Created fund ${fundId} for group ${groupId}`);
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
        
        const response = await fetch(`${API_BASE_URL}/deposit`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        
        const result = await response.json();
        
        if (!response.ok) {
            throw new Error(result.error || result.message || 'Failed to deposit');
        }
        
        if (result.success) {
            showNotification('success', '✅ Nạp tiền thành công!');
            closeDepositModal();
            
            // Reload data
            loadGroups(); // Reload để cập nhật fundId mới
            loadFundStats();
            loadRecentTransactions();
            loadTransactionHistory();
        } else {
            throw new Error(result.message || 'Unknown error');
        }
        
    } catch (error) {
        console.error('Error depositing:', error);
        showNotification('error', '❌ Lỗi: ' + error.message);
    }
}

// Withdraw Vote Modal
function openWithdrawVoteModal() {
    document.getElementById('withdrawVoteModal').classList.add('show');
    document.getElementById('withdrawVoteForm').reset();
    
    // Load current balance
    loadAvailableBalance();
}

function closeWithdrawVoteModal() {
    document.getElementById('withdrawVoteModal').classList.remove('show');
}

async function loadAvailableBalance() {
    try {
        const response = await fetch(`${API_BASE_URL}/stats`);
        if (!response.ok) throw new Error('Failed to load balance');
        
        const stats = await response.json();
        document.getElementById('availableBalance').textContent = formatCurrency(stats.totalBalance);
    } catch (error) {
        console.error('Error loading balance:', error);
    }
}

async function handleWithdrawVote(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const data = {
        fundId: parseInt(formData.get('groupId')),
        userId: CURRENT_USER_ID,
        amount: parseFloat(formData.get('amount')),
        purpose: formData.get('purpose'),
        receiptUrl: formData.get('receiptUrl') || null
    };
    
    // TODO: Tích hợp với voting system để tạo phiếu vote
    // Hiện tại chỉ tạo withdrawal request với status Pending
    
    try {
        const response = await fetch(`${API_BASE_URL}/withdraw/request`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        
        if (!response.ok) throw new Error('Failed to create withdrawal request');
        
        const result = await response.json();
        
        if (result.success) {
            showNotification('success', '🗳️ Phiếu bỏ phiếu đã được tạo! Các thành viên sẽ bỏ phiếu trong 3 ngày.');
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
        showNotification('error', '❌ Lỗi: ' + error.message);
    }
}

// ========================================
// UTILITY FUNCTIONS
// ========================================

function getStatusClass(status) {
    const map = {
        'Pending': 'warning',
        'Approved': 'info',
        'Rejected': 'danger',
        'Completed': 'success'
    };
    return map[status] || 'secondary';
}

function getStatusText(status) {
    const map = {
        'Pending': 'Chờ duyệt',
        'Approved': 'Đã duyệt',
        'Rejected': 'Từ chối',
        'Completed': 'Hoàn tất'
    };
    return map[status] || status;
}

function getStatusIcon(status) {
    const map = {
        'Pending': '⏳',
        'Approved': '✅',
        'Rejected': '❌',
        'Completed': '✔️'
    };
    return map[status] || '';
}

function formatCurrency(amount) {
    if (!amount) return '0 VNĐ';
    return new Intl.NumberFormat('vi-VN').format(amount) + ' VNĐ';
}

function formatDate(dateString) {
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

function showNotification(type, message) {
    // Simple alert for now
    alert(message);
}

function viewAllTransactions() {
    // Scroll to transaction table
    document.getElementById('transactionsTableBody').scrollIntoView({ behavior: 'smooth' });
}

function exportFundReport() {
    alert('📥 Chức năng xuất báo cáo đang được phát triển...');
}

function viewTransactionDetail(transactionId) {
    // TODO: Show modal with transaction details
    alert(`Xem chi tiết giao dịch #${transactionId}`);
}

async function cancelRequest(transactionId) {
    if (!confirm('Bạn có chắc muốn hủy yêu cầu này?')) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/transactions/${transactionId}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) throw new Error('Failed to cancel request');
        
        showNotification('success', '✅ Đã hủy yêu cầu');
        
        // Reload data
        loadFundStats();
        loadMyPendingRequests();
        loadTransactionHistory();
        
    } catch (error) {
        console.error('Error canceling request:', error);
        showNotification('error', '❌ Lỗi: ' + error.message);
    }
}

// Close modal when clicking outside
window.onclick = function(event) {
    if (event.target.id === 'depositModal') {
        closeDepositModal();
    }
    if (event.target.id === 'withdrawVoteModal') {
        closeWithdrawVoteModal();
    }
}

