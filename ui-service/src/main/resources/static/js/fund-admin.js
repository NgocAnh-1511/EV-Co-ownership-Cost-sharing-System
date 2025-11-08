// ========================================
// FUND ADMIN JS - Giao diện Admin
// ========================================

const API_BASE_URL = '/api/fund';
const CURRENT_USER_ID = 1; // TODO: Get from session

// ========================================
// INITIALIZATION
// ========================================

document.addEventListener('DOMContentLoaded', function() {
    loadGroups();
    loadFundStats();
    loadPendingApprovals();
    loadRecentTransactions();
    loadTransactionHistory();
    
    // Event listeners
    document.getElementById('addFundForm').addEventListener('submit', handleAddTransaction);
    document.getElementById('transactionType').addEventListener('change', handleTypeChange);
    document.getElementById('filterStatus').addEventListener('change', loadTransactionHistory);
    document.getElementById('filterType').addEventListener('change', loadTransactionHistory);
    document.getElementById('filterGroup').addEventListener('change', loadTransactionHistory);
    
    // Auto refresh every 30s
    setInterval(() => {
        loadGroups(); // Refresh group list to update fund status
        loadFundStats();
        loadPendingApprovals();
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
        console.log('📦 [ADMIN] Loaded groups:', groups);
        
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
        
        console.log('💰 [ADMIN] Groups with fund info:', groupsWithFunds);
        
        // Populate transaction modal dropdown (fundGroup) - hiển thị tất cả nhóm
        const fundGroupSelect = document.getElementById('fundGroup');
        if (fundGroupSelect) {
            fundGroupSelect.innerHTML = '<option value="">Chọn nhóm</option>' +
                groupsWithFunds
                    .map(g => `<option value="${g.groupId}" data-fund-id="${g.fundId || ''}">${g.groupName}${g.fundId ? '' : ' (chưa có quỹ)'}</option>`)
                    .join('');
            console.log('✅ Populated fundGroup dropdown');
        }
        
        // Populate filter dropdown - chỉ nhóm có quỹ
        const filterGroupSelect = document.getElementById('filterGroup');
        if (filterGroupSelect) {
            filterGroupSelect.innerHTML = '<option value="">Tất cả nhóm</option>' +
                groupsWithFunds
                    .filter(g => g.fundId)
                    .map(g => `<option value="${g.fundId}">${g.groupName}</option>`)
                    .join('');
            console.log('✅ Populated filterGroup dropdown');
        }
        
    } catch (error) {
        console.error('❌ Error loading groups:', error);
        
        // Restore empty state on error
        const fundGroupSelect = document.getElementById('fundGroup');
        const filterGroupSelect = document.getElementById('filterGroup');
        if (fundGroupSelect) fundGroupSelect.innerHTML = '<option value="">Không thể tải nhóm</option>';
        if (filterGroupSelect) filterGroupSelect.innerHTML = '<option value="">Tất cả nhóm</option>';
    }
}

async function loadFundStats() {
    try {
        const response = await fetch(`${API_BASE_URL}/stats`);
        if (!response.ok) throw new Error('Failed to load stats');
        
        const stats = await response.json();
        
        // Update stats cards
        document.getElementById('totalBalance').textContent = formatCurrency(stats.totalBalance);
        document.getElementById('totalIncome').textContent = formatCurrency(stats.totalIncome);
        document.getElementById('totalExpense').textContent = formatCurrency(stats.totalExpense);
        document.getElementById('pendingCount').textContent = stats.pendingCount || 0;
        
        // Update summary
        document.getElementById('summaryOpening').textContent = formatCurrency(stats.openingBalance);
        document.getElementById('summaryIncome').textContent = formatCurrency(stats.totalIncome);
        document.getElementById('summaryExpense').textContent = formatCurrency(stats.totalExpense);
        document.getElementById('summaryBalance').textContent = formatCurrency(stats.totalBalance);
        
    } catch (error) {
        console.error('Error loading stats:', error);
    }
}

async function loadPendingApprovals() {
    try {
        const response = await fetch(`${API_BASE_URL}/transactions?status=Pending`);
        if (!response.ok) throw new Error('Failed to load pending approvals');
        
        const transactions = await response.json();
        
        updatePendingApprovalsDisplay(transactions);
        
    } catch (error) {
        console.error('Error loading pending approvals:', error);
    }
}

function updatePendingApprovalsDisplay(transactions) {
    const badge = document.getElementById('pendingBadge');
    const tbody = document.getElementById('pendingApprovalsBody');
    
    badge.textContent = transactions.length;
    
    if (transactions.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="empty-table">
                    <div class="empty-state">
                        <i class="fas fa-check-circle"></i>
                        <p>Không có yêu cầu chờ duyệt</p>
                    </div>
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = transactions.map(t => `
        <tr>
            <td>${formatDate(t.createdAt)}</td>
            <td>${t.createdByName || 'User #' + t.createdBy}</td>
            <td>
                <span class="badge ${t.type === 'Deposit' ? 'badge-success' : 'badge-warning'}">
                    ${t.type === 'Deposit' ? '📥 Nạp tiền' : '📤 Rút tiền'}
                </span>
            </td>
            <td class="amount ${t.type === 'Withdraw' ? 'negative' : 'positive'}">
                ${formatCurrency(t.amount)}
            </td>
            <td>${t.purpose || '-'}</td>
            <td>
                ${t.receiptUrl 
                    ? `<a href="${t.receiptUrl}" target="_blank" class="receipt-link">
                        <i class="fas fa-file-invoice"></i> Xem hóa đơn
                       </a>` 
                    : '<span style="color: #999;">Không có</span>'}
            </td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-sm btn-success" onclick="approveTransaction(${t.transactionId})" title="Phê duyệt">
                        <i class="fas fa-check"></i>
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="rejectTransaction(${t.transactionId})" title="Từ chối">
                        <i class="fas fa-times"></i>
                    </button>
                    <button class="btn btn-sm btn-info" onclick="viewTransactionDetails(${t.transactionId})" title="Chi tiết">
                        <i class="fas fa-eye"></i>
                    </button>
                </div>
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
        const groupId = document.getElementById('filterGroup').value;
        
        let url = `${API_BASE_URL}/transactions?`;
        if (status) url += `status=${status}&`;
        if (type) url += `type=${type}&`;
        if (groupId) url += `groupId=${groupId}`;
        
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
                <td colspan="7" class="empty-table">
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
            <td>
                <div class="action-buttons">
                    ${t.status === 'Pending' ? `
                        <button class="btn btn-sm btn-success" onclick="approveTransaction(${t.transactionId})" title="Phê duyệt">
                            <i class="fas fa-check"></i>
                        </button>
                        <button class="btn btn-sm btn-danger" onclick="rejectTransaction(${t.transactionId})" title="Từ chối">
                            <i class="fas fa-times"></i>
                        </button>
                    ` : ''}
                    <button class="btn btn-sm btn-info" onclick="viewTransactionDetails(${t.transactionId})" title="Chi tiết">
                        <i class="fas fa-eye"></i>
                    </button>
                    ${t.status === 'Pending' || t.status === 'Rejected' ? `
                        <button class="btn btn-sm btn-secondary" onclick="deleteTransaction(${t.transactionId})" title="Xóa">
                            <i class="fas fa-trash"></i>
                        </button>
                    ` : ''}
                </div>
            </td>
        </tr>
    `).join('');
}

// ========================================
// APPROVAL ACTIONS
// ========================================

async function approveTransaction(transactionId) {
    if (!confirm('Bạn có chắc muốn PHÊ DUYỆT yêu cầu này?')) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/transactions/${transactionId}/approve`, {
            method: 'POST'
        });
        
        if (!response.ok) throw new Error('Failed to approve transaction');
        
        const result = await response.json();
        
        if (result.success) {
            showNotification('success', '✅ Đã phê duyệt yêu cầu!');
            
            // Reload data
            loadFundStats();
            loadPendingApprovals();
            loadRecentTransactions();
            loadTransactionHistory();
        } else {
            throw new Error(result.error || 'Unknown error');
        }
        
    } catch (error) {
        console.error('Error approving transaction:', error);
        showNotification('error', '❌ Lỗi: ' + error.message);
    }
}

async function rejectTransaction(transactionId) {
    const reason = prompt('Lý do từ chối:');
    if (!reason) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/transactions/${transactionId}/reject`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ reason })
        });
        
        if (!response.ok) throw new Error('Failed to reject transaction');
        
        const result = await response.json();
        
        if (result.success) {
            showNotification('success', '❌ Đã từ chối yêu cầu!');
            
            // Reload data
            loadFundStats();
            loadPendingApprovals();
            loadTransactionHistory();
        } else {
            throw new Error(result.error || 'Unknown error');
        }
        
    } catch (error) {
        console.error('Error rejecting transaction:', error);
        showNotification('error', '❌ Lỗi: ' + error.message);
    }
}

async function deleteTransaction(transactionId) {
    if (!confirm('Bạn có chắc muốn XÓA giao dịch này?')) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/transactions/${transactionId}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) throw new Error('Failed to delete transaction');
        
        const result = await response.json();
        
        if (result.success) {
            showNotification('success', '🗑️ Đã xóa giao dịch!');
            
            // Reload data
            loadFundStats();
            loadPendingApprovals();
            loadTransactionHistory();
        } else {
            throw new Error(result.error || 'Unknown error');
        }
        
    } catch (error) {
        console.error('Error deleting transaction:', error);
        showNotification('error', '❌ Lỗi: ' + error.message);
    }
}

function viewTransactionDetails(transactionId) {
    alert(`🔍 Xem chi tiết giao dịch #${transactionId}\n\nChức năng đang được phát triển...`);
}

// ========================================
// MODAL HANDLERS
// ========================================

function openAddFundModal() {
    document.getElementById('addFundModal').classList.add('show');
    document.getElementById('addFundForm').reset();
    document.getElementById('receiptGroup').style.display = 'none';
}

function closeAddFundModal() {
    document.getElementById('addFundModal').classList.remove('show');
}

function handleTypeChange() {
    const type = document.getElementById('transactionType').value;
    const receiptGroup = document.getElementById('receiptGroup');
    
    if (type === 'Withdraw' || type === 'WithdrawDirect') {
        receiptGroup.style.display = 'block';
    } else {
        receiptGroup.style.display = 'none';
    }
}

async function handleAddTransaction(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    let type = formData.get('type');
    
    // Admin có thể rút tiền trực tiếp
    const isDirectWithdraw = (type === 'WithdrawDirect');
    if (isDirectWithdraw) {
        type = 'Withdraw';
    }
    
    const data = {
        type: type,
        fundId: parseInt(formData.get('groupId')),
        amount: parseFloat(formData.get('amount')),
        purpose: formData.get('purpose'),
        createdBy: CURRENT_USER_ID,
        receiptUrl: formData.get('receiptUrl') || null,
        isAdminDirect: isDirectWithdraw
    };
    
    try {
        const response = await fetch(`${API_BASE_URL}/transactions`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        
        if (!response.ok) throw new Error('Failed to create transaction');
        
        const result = await response.json();
        
        if (result.success || result.transaction) {
            showNotification('success', 
                type === 'Deposit' 
                    ? '✅ Nạp tiền thành công!' 
                    : isDirectWithdraw
                        ? '✅ Rút tiền trực tiếp thành công!'
                        : '⏳ Yêu cầu rút tiền đã được gửi.');
            closeAddFundModal();
            
            // Reload data
            loadFundStats();
            loadPendingApprovals();
            loadRecentTransactions();
            loadTransactionHistory();
        } else {
            throw new Error(result.error || 'Unknown error');
        }
        
    } catch (error) {
        console.error('Error adding transaction:', error);
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

// Close modal when clicking outside
window.onclick = function(event) {
    const modal = document.getElementById('addFundModal');
    if (event.target === modal) {
        closeAddFundModal();
    }
}

