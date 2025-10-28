// Fund Management JavaScript

// Global variables
let fundStats = {};
let transactions = [];
let groups = [];

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    initializePage();
    loadFundStats();
    loadGroups();
    loadTransactions();
    setupEventListeners();
});

// Initialize page elements
function initializePage() {
    // Set current date for transaction form
    const today = new Date().toISOString().split('T')[0];
    const fundDateInput = document.getElementById('fundDate');
    if (fundDateInput) {
        fundDateInput.value = today;
    }
}

// Load fund statistics
async function loadFundStats() {
    try {
        const response = await fetch('/api/fund/stats');
        if (response.ok) {
            fundStats = await response.json();
            updateFundStatsDisplay();
        }
    } catch (error) {
        console.error('Error loading fund stats:', error);
        showNotification('Lỗi khi tải thống kê quỹ', 'error');
    }
}

// Update fund statistics display
function updateFundStatsDisplay() {
    // Update stat cards
    const statCards = document.querySelectorAll('.stat-card .stat-value');
    if (statCards.length >= 4) {
        statCards[0].textContent = formatCurrency(fundStats.totalBalance || 0);
        statCards[1].textContent = formatCurrency(fundStats.totalIncome || 0);
        statCards[2].textContent = formatCurrency(fundStats.totalExpense || 0);
        statCards[3].textContent = (fundStats.transactionCount || 0).toString();
    }
    
    // Update fund summary
    const summaryItems = document.querySelectorAll('.fund-summary .value');
    if (summaryItems.length >= 4) {
        summaryItems[0].textContent = formatCurrency(fundStats.openingBalance || 0);
        summaryItems[1].textContent = formatCurrency(fundStats.totalIncome || 0);
        summaryItems[2].textContent = formatCurrency(fundStats.totalExpense || 0);
        summaryItems[3].textContent = formatCurrency(fundStats.totalBalance || 0);
    }
}

// Load groups
async function loadGroups() {
    try {
        const response = await fetch('/api/groups');
        if (response.ok) {
            groups = await response.json();
            updateGroupSelects();
        }
    } catch (error) {
        console.error('Error loading groups:', error);
    }
}

// Update group select elements
function updateGroupSelects() {
    const groupSelects = document.querySelectorAll('#filterGroup, #fundGroup');
    groupSelects.forEach(select => {
        // Clear existing options except the first one
        while (select.children.length > 1) {
            select.removeChild(select.lastChild);
        }
        
        // Add group options
        groups.forEach(group => {
            const option = document.createElement('option');
            option.value = group.id;
            option.textContent = group.name;
            select.appendChild(option);
        });
    });
}

// Load transactions
async function loadTransactions() {
    try {
        const response = await fetch('/api/fund/transactions');
        if (response.ok) {
            transactions = await response.json();
            updateTransactionsDisplay();
            updateRecentTransactions();
        }
    } catch (error) {
        console.error('Error loading transactions:', error);
        showNotification('Lỗi khi tải giao dịch', 'error');
    }
}

// Update transactions table display
function updateTransactionsDisplay() {
    const tbody = document.getElementById('transactionsTableBody');
    if (!tbody) return;
    
    if (transactions.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="empty-table">
                    <div class="empty-state">
                        <i class="fas fa-receipt"></i>
                        <p>Chưa có giao dịch nào</p>
                    </div>
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = transactions.map(transaction => `
        <tr>
            <td>${formatDate(transaction.transactionDate)}</td>
            <td>
                <span class="type-badge ${transaction.type.toLowerCase()}">
                    ${transaction.type === 'INCOME' ? 'Thu nhập' : 'Chi phí'}
                </span>
            </td>
            <td>${transaction.description || '-'}</td>
            <td>${getGroupName(transaction.groupId)}</td>
            <td class="${transaction.type === 'INCOME' ? 'positive' : 'negative'}">
                ${transaction.type === 'INCOME' ? '+' : '-'}${formatCurrency(transaction.amount)}
            </td>
            <td>${transaction.createdBy || '-'}</td>
            <td>
                <div class="cost-actions">
                    <button class="btn btn-sm btn-outline" onclick="editTransaction(${transaction.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-outline" onclick="deleteTransaction(${transaction.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

// Update recent transactions display
function updateRecentTransactions() {
    const recentContainer = document.getElementById('recentTransactions');
    if (!recentContainer) return;
    
    const recentTransactions = transactions.slice(0, 5);
    
    if (recentTransactions.length === 0) {
        recentContainer.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-receipt"></i>
                <p>Chưa có giao dịch nào</p>
            </div>
        `;
        return;
    }
    
    recentContainer.innerHTML = recentTransactions.map(transaction => `
        <div class="transaction-item">
            <div class="transaction-info">
                <div class="transaction-type ${transaction.type.toLowerCase()}">
                    <i class="fas fa-${transaction.type === 'INCOME' ? 'arrow-up' : 'arrow-down'}"></i>
                    ${transaction.type === 'INCOME' ? 'Thu nhập' : 'Chi phí'}
                </div>
                <div class="transaction-details">
                    <div class="transaction-description">${transaction.description || 'Không có mô tả'}</div>
                    <div class="transaction-meta">
                        ${getGroupName(transaction.groupId)} • ${formatDate(transaction.transactionDate)}
                    </div>
                </div>
            </div>
            <div class="transaction-amount ${transaction.type === 'INCOME' ? 'positive' : 'negative'}">
                ${transaction.type === 'INCOME' ? '+' : '-'}${formatCurrency(transaction.amount)}
            </div>
        </div>
    `).join('');
}

// Setup event listeners
function setupEventListeners() {
    // Filter change events
    const filterType = document.getElementById('filterType');
    const filterGroup = document.getElementById('filterGroup');
    
    if (filterType) {
        filterType.addEventListener('change', filterTransactions);
    }
    
    if (filterGroup) {
        filterGroup.addEventListener('change', filterTransactions);
    }
    
    // Form submission
    const addFundForm = document.getElementById('addFundForm');
    if (addFundForm) {
        addFundForm.addEventListener('submit', handleAddFundTransaction);
    }
}

// Filter transactions
function filterTransactions() {
    const typeFilter = document.getElementById('filterType')?.value;
    const groupFilter = document.getElementById('filterGroup')?.value;
    
    let filteredTransactions = transactions;
    
    if (typeFilter) {
        filteredTransactions = filteredTransactions.filter(t => t.type === typeFilter);
    }
    
    if (groupFilter) {
        filteredTransactions = filteredTransactions.filter(t => t.groupId == groupFilter);
    }
    
    // Update display with filtered transactions
    updateTransactionsDisplayWithData(filteredTransactions);
}

// Update transactions display with specific data
function updateTransactionsDisplayWithData(transactionData) {
    const tbody = document.getElementById('transactionsTableBody');
    if (!tbody) return;
    
    if (transactionData.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="empty-table">
                    <div class="empty-state">
                        <i class="fas fa-search"></i>
                        <p>Không tìm thấy giao dịch phù hợp</p>
                    </div>
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = transactionData.map(transaction => `
        <tr>
            <td>${formatDate(transaction.transactionDate)}</td>
            <td>
                <span class="type-badge ${transaction.type.toLowerCase()}">
                    ${transaction.type === 'INCOME' ? 'Thu nhập' : 'Chi phí'}
                </span>
            </td>
            <td>${transaction.description || '-'}</td>
            <td>${getGroupName(transaction.groupId)}</td>
            <td class="${transaction.type === 'INCOME' ? 'positive' : 'negative'}">
                ${transaction.type === 'INCOME' ? '+' : '-'}${formatCurrency(transaction.amount)}
            </td>
            <td>${transaction.createdBy || '-'}</td>
            <td>
                <div class="cost-actions">
                    <button class="btn btn-sm btn-outline" onclick="editTransaction(${transaction.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-outline" onclick="deleteTransaction(${transaction.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

// Modal functions
function openAddFundModal() {
    const modal = document.getElementById('addFundModal');
    if (modal) {
        modal.classList.add('show');
        modal.style.display = 'flex';
    }
}

function closeAddFundModal() {
    const modal = document.getElementById('addFundModal');
    if (modal) {
        modal.classList.remove('show');
        modal.style.display = 'none';
        // Reset form
        const form = document.getElementById('addFundForm');
        if (form) {
            form.reset();
        }
    }
}

// Handle add fund transaction
async function handleAddFundTransaction(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const transactionData = {
        type: formData.get('type'),
        groupId: parseInt(formData.get('groupId')),
        amount: parseFloat(formData.get('amount')),
        description: formData.get('description'),
        transactionDate: formData.get('transactionDate')
    };
    
    try {
        const response = await fetch('/api/fund/transactions', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(transactionData)
        });
        
        if (response.ok) {
            showNotification('Thêm giao dịch thành công!', 'success');
            closeAddFundModal();
            loadFundStats();
            loadTransactions();
        } else {
            const error = await response.json();
            showNotification(error.message || 'Lỗi khi thêm giao dịch', 'error');
        }
    } catch (error) {
        console.error('Error adding transaction:', error);
        showNotification('Lỗi khi thêm giao dịch', 'error');
    }
}

// Export fund report
function exportFundReport() {
    // Create CSV content
    const csvContent = createFundReportCSV();
    
    // Download CSV
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `fund_report_${new Date().toISOString().split('T')[0]}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

// Create fund report CSV
function createFundReportCSV() {
    const headers = ['Ngày', 'Loại', 'Mô tả', 'Nhóm', 'Số tiền', 'Người thực hiện'];
    const rows = transactions.map(t => [
        formatDate(t.transactionDate),
        t.type === 'INCOME' ? 'Thu nhập' : 'Chi phí',
        t.description || '',
        getGroupName(t.groupId),
        t.amount,
        t.createdBy || ''
    ]);
    
    const csvContent = [headers, ...rows]
        .map(row => row.map(field => `"${field}"`).join(','))
        .join('\n');
    
    return '\uFEFF' + csvContent; // Add BOM for UTF-8
}

// View all transactions
function viewAllTransactions() {
    // Scroll to transactions table
    const table = document.querySelector('.content-card:last-child');
    if (table) {
        table.scrollIntoView({ behavior: 'smooth' });
    }
}

// Edit transaction
function editTransaction(transactionId) {
    const transaction = transactions.find(t => t.id === transactionId);
    if (!transaction) return;
    
    // Fill form with transaction data
    document.getElementById('transactionType').value = transaction.type;
    document.getElementById('fundGroup').value = transaction.groupId;
    document.getElementById('fundAmount').value = transaction.amount;
    document.getElementById('fundDescription').value = transaction.description || '';
    document.getElementById('fundDate').value = transaction.transactionDate;
    
    // Open modal
    openAddFundModal();
    
    // Change form title and submit behavior
    const modalTitle = document.querySelector('#addFundModal .modal-header h3');
    if (modalTitle) {
        modalTitle.textContent = 'Chỉnh sửa giao dịch';
    }
    
    // Store transaction ID for update
    const form = document.getElementById('addFundForm');
    form.dataset.transactionId = transactionId;
}

// Delete transaction
async function deleteTransaction(transactionId) {
    if (!confirm('Bạn có chắc chắn muốn xóa giao dịch này?')) {
        return;
    }
    
    try {
        const response = await fetch(`/api/fund/transactions/${transactionId}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            showNotification('Xóa giao dịch thành công!', 'success');
            loadFundStats();
            loadTransactions();
        } else {
            showNotification('Lỗi khi xóa giao dịch', 'error');
        }
    } catch (error) {
        console.error('Error deleting transaction:', error);
        showNotification('Lỗi khi xóa giao dịch', 'error');
    }
}

// Utility functions
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
}

function getGroupName(groupId) {
    const group = groups.find(g => g.id === groupId);
    return group ? group.name : 'Không xác định';
}

function showNotification(message, type = 'info') {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 1rem 1.5rem;
        border-radius: 0.5rem;
        color: white;
        font-weight: 500;
        z-index: 3000;
        animation: slideIn 0.3s ease;
        max-width: 400px;
    `;
    
    // Set background color based on type
    const colors = {
        success: '#10b981',
        error: '#ef4444',
        warning: '#f59e0b',
        info: '#3b82f6'
    };
    notification.style.backgroundColor = colors[type] || colors.info;
    
    notification.textContent = message;
    
    // Add to page
    document.body.appendChild(notification);
    
    // Remove after 3 seconds
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, 3000);
}

// Add CSS for notifications
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
    
    .transaction-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1rem;
        border-bottom: 1px solid #f1f5f9;
    }
    
    .transaction-item:last-child {
        border-bottom: none;
    }
    
    .transaction-info {
        display: flex;
        align-items: center;
        gap: 1rem;
    }
    
    .transaction-type {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        padding: 0.25rem 0.75rem;
        border-radius: 1rem;
        font-size: 0.8rem;
        font-weight: 500;
    }
    
    .transaction-type.income {
        background: #dcfce7;
        color: #166534;
    }
    
    .transaction-type.expense {
        background: #fef2f2;
        color: #991b1b;
    }
    
    .transaction-details {
        flex: 1;
    }
    
    .transaction-description {
        font-weight: 500;
        color: #1e293b;
        margin-bottom: 0.25rem;
    }
    
    .transaction-meta {
        font-size: 0.8rem;
        color: #64748b;
    }
    
    .transaction-amount {
        font-weight: 600;
        font-size: 1.1rem;
    }
    
    .transaction-amount.positive {
        color: #059669;
    }
    
    .transaction-amount.negative {
        color: #dc2626;
    }
    
    .type-badge {
        display: inline-flex;
        align-items: center;
        gap: 0.25rem;
        padding: 0.25rem 0.75rem;
        border-radius: 1rem;
        font-size: 0.8rem;
        font-weight: 500;
    }
    
    .type-badge.income {
        background: #dcfce7;
        color: #166534;
    }
    
    .type-badge.expense {
        background: #fef2f2;
        color: #991b1b;
    }
`;
document.head.appendChild(style);
