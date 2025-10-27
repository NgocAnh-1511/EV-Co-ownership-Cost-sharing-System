// Fund Page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initializeFundPage();
});

function initializeFundPage() {
    loadFundStats();
    loadRecentTransactions();
    loadAllTransactions();
    loadGroups();
    
    // Setup event listeners
    setupEventListeners();
}

function setupEventListeners() {
    // Filter change events
    document.getElementById('filterType').addEventListener('change', filterTransactions);
    document.getElementById('filterGroup').addEventListener('change', filterTransactions);
    
    // Form submissions
    document.getElementById('addFundForm').addEventListener('submit', handleAddFundTransaction);
    
    // Modal close events
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('modal')) {
            closeAllModals();
        }
    });
}

// Load fund statistics
async function loadFundStats() {
    try {
        const response = await fetch('/api/fund/stats');
        if (response.ok) {
            const stats = await response.json();
            updateFundStatsDisplay(stats);
        }
    } catch (error) {
        console.error('Error loading fund stats:', error);
    }
}

function updateFundStatsDisplay(stats) {
    document.getElementById('totalBalance').textContent = formatCurrency(stats.totalBalance || 0);
    document.getElementById('totalIncome').textContent = formatCurrency(stats.totalIncome || 0);
    document.getElementById('totalExpense').textContent = formatCurrency(stats.totalExpense || 0);
    document.getElementById('transactionCount').textContent = stats.transactionCount || 0;
}

// Load recent transactions
async function loadRecentTransactions() {
    try {
        const response = await fetch('/api/fund/transactions/recent');
        if (response.ok) {
            const transactions = await response.json();
            displayRecentTransactions(transactions);
        }
    } catch (error) {
        console.error('Error loading recent transactions:', error);
    }
}

function displayRecentTransactions(transactions) {
    const container = document.getElementById('recentTransactions');
    
    if (transactions.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-receipt"></i>
                <p>Chưa có giao dịch nào</p>
            </div>
        `;
        return;
    }
    
    container.innerHTML = transactions.map(transaction => `
        <div class="transaction-item">
            <div class="transaction-info">
                <div class="transaction-description">${transaction.description || 'Không có mô tả'}</div>
                <div class="transaction-meta">
                    ${transaction.groupName} • ${formatDate(transaction.transactionDate)}
                </div>
            </div>
            <div class="transaction-amount ${transaction.type === 'INCOME' ? 'positive' : 'negative'}">
                ${transaction.type === 'INCOME' ? '+' : '-'}${formatCurrency(transaction.amount)}
            </div>
        </div>
    `).join('');
}

// Load all transactions
async function loadAllTransactions() {
    try {
        const response = await fetch('/api/fund/transactions');
        if (response.ok) {
            const transactions = await response.json();
            displayAllTransactions(transactions);
        }
    } catch (error) {
        console.error('Error loading all transactions:', error);
    }
}

function displayAllTransactions(transactions) {
    const tbody = document.getElementById('transactionsTableBody');
    
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
                <span class="transaction-type ${transaction.type.toLowerCase()}">
                    ${transaction.type === 'INCOME' ? 'Thu nhập' : 'Chi phí'}
                </span>
            </td>
            <td>${transaction.description || 'Không có mô tả'}</td>
            <td>${transaction.groupName}</td>
            <td class="${transaction.type === 'INCOME' ? 'positive' : 'negative'}">
                ${transaction.type === 'INCOME' ? '+' : '-'}${formatCurrency(transaction.amount)}
            </td>
            <td>${transaction.createdBy}</td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-sm btn-outline" onclick="viewTransactionDetails(${transaction.id})">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="btn btn-sm btn-outline" onclick="editTransaction(${transaction.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

// Load groups for dropdown
async function loadGroups() {
    try {
        const response = await fetch('/api/groups');
        if (response.ok) {
            const groups = await response.json();
            populateGroupSelects(groups);
        }
    } catch (error) {
        console.error('Error loading groups:', error);
    }
}

function populateGroupSelects(groups) {
    const selects = [
        document.getElementById('fundGroup'),
        document.getElementById('filterGroup')
    ];
    
    const options = groups.map(group => 
        `<option value="${group.id}">${group.name}</option>`
    ).join('');
    
    selects.forEach(select => {
        if (select) {
            const currentValue = select.value;
            select.innerHTML = '<option value="">Chọn nhóm</option>' + options;
            if (currentValue) {
                select.value = currentValue;
            }
        }
    });
}

// Filter transactions
function filterTransactions() {
    const typeFilter = document.getElementById('filterType').value;
    const groupFilter = document.getElementById('filterGroup').value;
    
    // This would typically make an API call with filters
    // For now, we'll just reload all transactions
    loadAllTransactions();
}

// Modal functions
function openAddFundModal() {
    // Set default date to today
    document.getElementById('fundDate').value = new Date().toISOString().split('T')[0];
    document.getElementById('addFundModal').classList.add('show');
}

function closeAddFundModal() {
    document.getElementById('addFundModal').classList.remove('show');
    document.getElementById('addFundForm').reset();
}

function closeAllModals() {
    document.querySelectorAll('.modal').forEach(modal => {
        modal.classList.remove('show');
    });
}

// Handle form submissions
async function handleAddFundTransaction(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const transactionData = {
        type: formData.get('type'),
        groupId: formData.get('groupId'),
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
            closeAddFundModal();
            loadFundStats();
            loadRecentTransactions();
            loadAllTransactions();
            showNotification('Giao dịch đã được thêm thành công!', 'success');
        } else {
            showNotification('Có lỗi xảy ra khi thêm giao dịch', 'error');
        }
    } catch (error) {
        console.error('Error adding fund transaction:', error);
        showNotification('Có lỗi xảy ra khi thêm giao dịch', 'error');
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

function exportFundReport() {
    // Implementation for exporting fund report
    showNotification('Tính năng xuất báo cáo đang được phát triển', 'info');
}

function viewAllTransactions() {
    // Scroll to transactions table
    document.getElementById('transactionsTableBody').scrollIntoView({ 
        behavior: 'smooth' 
    });
}

function viewTransactionDetails(transactionId) {
    // Implementation for viewing transaction details
    showNotification('Tính năng xem chi tiết đang được phát triển', 'info');
}

function editTransaction(transactionId) {
    // Implementation for editing transaction
    showNotification('Tính năng chỉnh sửa đang được phát triển', 'info');
}

function showNotification(message, type = 'info') {
    // Simple notification system
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 1rem 1.5rem;
        border-radius: 0.5rem;
        color: white;
        font-weight: 500;
        z-index: 10000;
        animation: slideIn 0.3s ease-out;
    `;
    
    const colors = {
        success: '#10b981',
        error: '#ef4444',
        info: '#3b82f6',
        warning: '#f59e0b'
    };
    
    notification.style.backgroundColor = colors[type] || colors.info;
    
    document.body.appendChild(notification);
    
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease-in';
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 3000);
}

// Add CSS animations
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
    
    .transaction-type {
        padding: 0.25rem 0.5rem;
        border-radius: 0.25rem;
        font-size: 0.8rem;
        font-weight: 500;
    }
    
    .transaction-type.income {
        background: #d1fae5;
        color: #065f46;
    }
    
    .transaction-type.expense {
        background: #fee2e2;
        color: #991b1b;
    }
    
    .positive {
        color: #38a169;
    }
    
    .negative {
        color: #e53e3e;
    }
    
    .action-buttons {
        display: flex;
        gap: 0.5rem;
    }
`;
document.head.appendChild(style);
