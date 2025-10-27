// Fund Management JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // Initialize the fund management interface
    initializeFundManagement();
    
    // Set up event listeners
    setupEventListeners();
    
    // Load initial data
    loadFundData();
});

// Initialize fund management
function initializeFundManagement() {
    console.log('Initializing Fund Management...');
    
    // Set current date for transaction form
    const today = new Date().toISOString().split('T')[0];
    const dateInput = document.getElementById('transactionDate');
    if (dateInput) {
        dateInput.value = today;
    }
    
    // Add fade-in animation to cards
    const cards = document.querySelectorAll('.card');
    cards.forEach((card, index) => {
        setTimeout(() => {
            card.classList.add('fade-in');
        }, index * 100);
    });
}

// Setup event listeners
function setupEventListeners() {
    // Form submission
    const addTransactionForm = document.getElementById('addTransactionForm');
    if (addTransactionForm) {
        addTransactionForm.addEventListener('submit', handleAddTransaction);
    }
    
    // Filter changes
    const filterGroup = document.getElementById('filterGroup');
    const filterType = document.getElementById('filterType');
    
    if (filterGroup) {
        filterGroup.addEventListener('change', applyFilters);
    }
    
    if (filterType) {
        filterType.addEventListener('change', applyFilters);
    }
    
    // Tab switching
    const tabButtons = document.querySelectorAll('[data-bs-toggle="tab"]');
    tabButtons.forEach(button => {
        button.addEventListener('shown.bs.tab', function(event) {
            const targetTab = event.target.getAttribute('data-bs-target');
            handleTabSwitch(targetTab);
        });
    });
}

// Load fund data
async function loadFundData() {
    try {
        showLoadingState();
        
        // Load fund statistics
        await loadFundStatistics();
        
        // Load recent transactions
        await loadRecentTransactions();
        
        // Load groups for filters
        await loadGroups();
        
        hideLoadingState();
    } catch (error) {
        console.error('Error loading fund data:', error);
        showErrorMessage('Không thể tải dữ liệu quỹ chung');
    }
}

// Load fund statistics
async function loadFundStatistics() {
    try {
        // Mock data - replace with actual API calls
        const mockStats = {
            totalFundBalance: 15000000,
            totalIncome: 2500000,
            totalExpenses: 1800000,
            transactionCount: 12,
            maintenanceFund: 5000000,
            reserveFund: 3000000,
            upgradeFund: 2000000,
            otherFund: 5000000
        };
        
        // Update statistics cards
        updateElement('totalFundBalance', formatCurrency(mockStats.totalFundBalance));
        updateElement('totalIncome', formatCurrency(mockStats.totalIncome));
        updateElement('totalExpenses', formatCurrency(mockStats.totalExpenses));
        updateElement('transactionCount', mockStats.transactionCount);
        
        // Update fund overview
        updateElement('maintenanceFund', formatCurrency(mockStats.maintenanceFund));
        updateElement('reserveFund', formatCurrency(mockStats.reserveFund));
        updateElement('upgradeFund', formatCurrency(mockStats.upgradeFund));
        updateElement('otherFund', formatCurrency(mockStats.otherFund));
        
    } catch (error) {
        console.error('Error loading fund statistics:', error);
    }
}

// Load recent transactions
async function loadRecentTransactions() {
    try {
        // Mock data - replace with actual API calls
        const mockTransactions = [
            {
                id: 1,
                date: '2024-01-15',
                type: 'EXPENSE',
                description: 'Bảo dưỡng định kỳ',
                amount: 500000,
                status: 'COMPLETED',
                category: 'MAINTENANCE'
            },
            {
                id: 2,
                date: '2024-01-14',
                type: 'INCOME',
                description: 'Đóng góp từ thành viên',
                amount: 1000000,
                status: 'COMPLETED',
                category: 'RESERVE'
            },
            {
                id: 3,
                date: '2024-01-13',
                type: 'EXPENSE',
                description: 'Mua phụ tùng',
                amount: 300000,
                status: 'PENDING',
                category: 'MAINTENANCE'
            }
        ];
        
        displayRecentTransactions(mockTransactions);
        
    } catch (error) {
        console.error('Error loading recent transactions:', error);
    }
}

// Display recent transactions
function displayRecentTransactions(transactions) {
    const container = document.getElementById('recentTransactions');
    if (!container) return;
    
    if (transactions.length === 0) {
        container.innerHTML = `
            <div class="text-center text-muted py-4">
                <i class="fas fa-inbox fa-3x mb-3"></i>
                <p>Chưa có giao dịch nào</p>
            </div>
        `;
        return;
    }
    
    const transactionsHtml = transactions.map(transaction => `
        <div class="d-flex align-items-center p-3 border-bottom">
            <div class="flex-shrink-0 me-3">
                <div class="bg-${getTransactionTypeColor(transaction.type)} rounded-circle d-flex align-items-center justify-content-center" style="width: 40px; height: 40px;">
                    <i class="fas fa-${getTransactionIcon(transaction.type)} text-white"></i>
                </div>
            </div>
            <div class="flex-grow-1">
                <h6 class="mb-1">${transaction.description}</h6>
                <small class="text-muted">${formatDate(transaction.date)}</small>
            </div>
            <div class="text-end">
                <div class="fw-bold ${transaction.type === 'INCOME' ? 'text-success' : 'text-danger'}">
                    ${transaction.type === 'INCOME' ? '+' : '-'}${formatCurrency(transaction.amount)}
                </div>
                <span class="badge bg-${getStatusColor(transaction.status)}">${getStatusText(transaction.status)}</span>
            </div>
        </div>
    `).join('');
    
    container.innerHTML = transactionsHtml;
}

// Load groups for filters
async function loadGroups() {
    try {
        // Mock data - replace with actual API calls
        const mockGroups = [
            { id: 1, name: 'Nhóm Tesla Model 3' },
            { id: 2, name: 'Nhóm BMW i3' },
            { id: 3, name: 'Nhóm Nissan Leaf' }
        ];
        
        // Populate group filters
        populateSelect('filterGroup', mockGroups);
        populateSelect('transactionGroup', mockGroups);
        
    } catch (error) {
        console.error('Error loading groups:', error);
    }
}

// Handle add transaction form submission
async function handleAddTransaction(event) {
    event.preventDefault();
    
    const form = event.target;
    const formData = new FormData(form);
    
    try {
        showLoadingState();
        
        // Convert FormData to object
        const transactionData = Object.fromEntries(formData.entries());
        
        // Mock API call - replace with actual API call
        console.log('Adding transaction:', transactionData);
        
        // Simulate API delay
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        // Show success message
        showSuccessMessage('Giao dịch đã được thêm thành công!');
        
        // Close modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('addTransactionModal'));
        if (modal) {
            modal.hide();
        }
        
        // Reset form
        form.reset();
        
        // Reload data
        await loadFundData();
        
    } catch (error) {
        console.error('Error adding transaction:', error);
        showErrorMessage('Không thể thêm giao dịch. Vui lòng thử lại.');
    } finally {
        hideLoadingState();
    }
}

// Handle tab switching
function handleTabSwitch(targetTab) {
    console.log('Switching to tab:', targetTab);
    
    switch (targetTab) {
        case '#balance':
            loadBalanceData();
            break;
        case '#transactions':
            loadTransactionsData();
            break;
        case '#reports':
            // Reports tab is handled by generateFundReport function
            break;
    }
}

// Load balance data
async function loadBalanceData() {
    try {
        // Mock data - replace with actual API calls
        const mockBalanceData = {
            groups: [
                { name: 'Nhóm Tesla Model 3', balance: 5000000 },
                { name: 'Nhóm BMW i3', balance: 3000000 },
                { name: 'Nhóm Nissan Leaf', balance: 2000000 }
            ]
        };
        
        displayBalanceData(mockBalanceData);
        
    } catch (error) {
        console.error('Error loading balance data:', error);
    }
}

// Display balance data
function displayBalanceData(data) {
    const container = document.getElementById('balanceCard');
    if (!container) return;
    
    const balanceHtml = data.groups.map(group => `
        <div class="col-md-4 mb-3">
            <div class="card bg-light">
                <div class="card-body text-center">
                    <h6 class="card-title">${group.name}</h6>
                    <h4 class="text-primary">${formatCurrency(group.balance)}</h4>
                </div>
            </div>
        </div>
    `).join('');
    
    container.innerHTML = balanceHtml;
}

// Load transactions data
async function loadTransactionsData() {
    try {
        // Mock data - replace with actual API calls
        const mockTransactions = [
            {
                id: 1,
                date: '2024-01-15',
                type: 'EXPENSE',
                description: 'Bảo dưỡng định kỳ',
                amount: 500000,
                status: 'COMPLETED',
                category: 'MAINTENANCE'
            },
            {
                id: 2,
                date: '2024-01-14',
                type: 'INCOME',
                description: 'Đóng góp từ thành viên',
                amount: 1000000,
                status: 'COMPLETED',
                category: 'RESERVE'
            },
            {
                id: 3,
                date: '2024-01-13',
                type: 'EXPENSE',
                description: 'Mua phụ tùng',
                amount: 300000,
                status: 'PENDING',
                category: 'MAINTENANCE'
            }
        ];
        
        displayTransactionsTable(mockTransactions);
        
    } catch (error) {
        console.error('Error loading transactions data:', error);
    }
}

// Display transactions table
function displayTransactionsTable(transactions) {
    const tbody = document.getElementById('transactionsTableBody');
    if (!tbody) return;
    
    if (transactions.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center text-muted py-4">
                    <i class="fas fa-inbox fa-2x mb-2"></i>
                    <p class="mb-0">Chưa có giao dịch nào</p>
                </td>
            </tr>
        `;
        return;
    }
    
    const transactionsHtml = transactions.map(transaction => `
        <tr>
            <td>${formatDate(transaction.date)}</td>
            <td>
                <span class="badge bg-${getTransactionTypeColor(transaction.type)}">
                    ${getTransactionTypeText(transaction.type)}
                </span>
            </td>
            <td>${transaction.description}</td>
            <td class="${transaction.type === 'INCOME' ? 'text-success' : 'text-danger'}">
                ${transaction.type === 'INCOME' ? '+' : '-'}${formatCurrency(transaction.amount)}
            </td>
            <td>
                <span class="badge bg-${getStatusColor(transaction.status)}">
                    ${getStatusText(transaction.status)}
                </span>
            </td>
            <td>
                <div class="btn-group btn-group-sm">
                    <button class="btn btn-outline-primary" onclick="viewTransaction(${transaction.id})">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="btn btn-outline-secondary" onclick="editTransaction(${transaction.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-outline-danger" onclick="deleteTransaction(${transaction.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
    
    tbody.innerHTML = transactionsHtml;
}

// Apply filters
function applyFilters() {
    const groupFilter = document.getElementById('filterGroup')?.value;
    const typeFilter = document.getElementById('filterType')?.value;
    
    console.log('Applying filters:', { groupFilter, typeFilter });
    
    // Reload data with filters
    loadFundData();
}

// Generate fund report
function generateFundReport() {
    const period = document.getElementById('reportPeriod')?.value;
    const resultsContainer = document.getElementById('fundReportResults');
    
    if (!resultsContainer) return;
    
    console.log('Generating report for period:', period);
    
    // Show loading state
    resultsContainer.innerHTML = `
        <div class="text-center py-4">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Đang tạo báo cáo...</span>
            </div>
            <p class="mt-2">Đang tạo báo cáo...</p>
        </div>
    `;
    
    // Simulate report generation
    setTimeout(() => {
        const mockReport = generateMockReport(period);
        displayReportResults(mockReport);
    }, 2000);
}

// Generate mock report
function generateMockReport(period) {
    return {
        period: period,
        totalIncome: 2500000,
        totalExpenses: 1800000,
        netAmount: 700000,
        categories: [
            { name: 'Bảo dưỡng', amount: 800000, percentage: 44.4 },
            { name: 'Dự phòng', amount: 500000, percentage: 27.8 },
            { name: 'Nâng cấp', amount: 300000, percentage: 16.7 },
            { name: 'Khác', amount: 200000, percentage: 11.1 }
        ]
    };
}

// Display report results
function displayReportResults(report) {
    const container = document.getElementById('fundReportResults');
    if (!container) return;
    
    const reportHtml = `
        <div class="row">
            <div class="col-md-6">
                <h6>Tổng quan</h6>
                <div class="row g-2">
                    <div class="col-6">
                        <div class="bg-success text-white p-2 rounded text-center">
                            <small>Thu nhập</small>
                            <div class="fw-bold">${formatCurrency(report.totalIncome)}</div>
                        </div>
                    </div>
                    <div class="col-6">
                        <div class="bg-danger text-white p-2 rounded text-center">
                            <small>Chi tiêu</small>
                            <div class="fw-bold">${formatCurrency(report.totalExpenses)}</div>
                        </div>
                    </div>
                </div>
                <div class="mt-3">
                    <div class="bg-primary text-white p-3 rounded text-center">
                        <small>Số dư</small>
                        <div class="h4 mb-0">${formatCurrency(report.netAmount)}</div>
                    </div>
                </div>
            </div>
            <div class="col-md-6">
                <h6>Chi tiêu theo danh mục</h6>
                ${report.categories.map(category => `
                    <div class="mb-2">
                        <div class="d-flex justify-content-between">
                            <span>${category.name}</span>
                            <span>${formatCurrency(category.amount)}</span>
                        </div>
                        <div class="progress" style="height: 6px;">
                            <div class="progress-bar" style="width: ${category.percentage}%"></div>
                        </div>
                    </div>
                `).join('')}
            </div>
        </div>
    `;
    
    container.innerHTML = reportHtml;
}

// Export fund report
function exportFundReport() {
    console.log('Exporting fund report...');
    
    // Mock export functionality
    showSuccessMessage('Báo cáo đã được xuất thành công!');
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

function getTransactionTypeColor(type) {
    switch (type) {
        case 'INCOME': return 'success';
        case 'EXPENSE': return 'danger';
        case 'TRANSFER': return 'info';
        default: return 'secondary';
    }
}

function getTransactionTypeText(type) {
    switch (type) {
        case 'INCOME': return 'Thu nhập';
        case 'EXPENSE': return 'Chi tiêu';
        case 'TRANSFER': return 'Chuyển khoản';
        default: return 'Khác';
    }
}

function getTransactionIcon(type) {
    switch (type) {
        case 'INCOME': return 'arrow-up';
        case 'EXPENSE': return 'arrow-down';
        case 'TRANSFER': return 'exchange-alt';
        default: return 'question';
    }
}

function getStatusColor(status) {
    switch (status) {
        case 'COMPLETED': return 'success';
        case 'PENDING': return 'warning';
        case 'CANCELLED': return 'danger';
        default: return 'secondary';
    }
}

function getStatusText(status) {
    switch (status) {
        case 'COMPLETED': return 'Hoàn thành';
        case 'PENDING': return 'Chờ xử lý';
        case 'CANCELLED': return 'Đã hủy';
        default: return 'Không xác định';
    }
}

function populateSelect(selectId, options) {
    const select = document.getElementById(selectId);
    if (!select) return;
    
    // Clear existing options except the first one
    const firstOption = select.firstElementChild;
    select.innerHTML = '';
    select.appendChild(firstOption);
    
    // Add new options
    options.forEach(option => {
        const optionElement = document.createElement('option');
        optionElement.value = option.id;
        optionElement.textContent = option.name;
        select.appendChild(optionElement);
    });
}

function updateElement(elementId, value) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = value;
    }
}

function showLoadingState() {
    const buttons = document.querySelectorAll('button[type="submit"]');
    buttons.forEach(button => {
        button.disabled = true;
        button.classList.add('loading');
    });
}

function hideLoadingState() {
    const buttons = document.querySelectorAll('button[type="submit"]');
    buttons.forEach(button => {
        button.disabled = false;
        button.classList.remove('loading');
    });
}

function showSuccessMessage(message) {
    // Create and show Bootstrap toast
    const toastHtml = `
        <div class="toast align-items-center text-white bg-success border-0" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex">
                <div class="toast-body">
                    <i class="fas fa-check-circle me-2"></i>${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        </div>
    `;
    
    showToast(toastHtml);
}

function showErrorMessage(message) {
    // Create and show Bootstrap toast
    const toastHtml = `
        <div class="toast align-items-center text-white bg-danger border-0" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex">
                <div class="toast-body">
                    <i class="fas fa-exclamation-circle me-2"></i>${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        </div>
    `;
    
    showToast(toastHtml);
}

function showToast(toastHtml) {
    // Create toast container if it doesn't exist
    let toastContainer = document.getElementById('toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toast-container';
        toastContainer.className = 'toast-container position-fixed top-0 end-0 p-3';
        toastContainer.style.zIndex = '1055';
        document.body.appendChild(toastContainer);
    }
    
    // Add toast to container
    toastContainer.insertAdjacentHTML('beforeend', toastHtml);
    
    // Show the last toast
    const toasts = toastContainer.querySelectorAll('.toast');
    const lastToast = toasts[toasts.length - 1];
    
    const bsToast = new bootstrap.Toast(lastToast);
    bsToast.show();
    
    // Remove toast from DOM after it's hidden
    lastToast.addEventListener('hidden.bs.toast', () => {
        lastToast.remove();
    });
}

// Transaction actions
function viewTransaction(id) {
    console.log('Viewing transaction:', id);
    // Implement view transaction functionality
}

function editTransaction(id) {
    console.log('Editing transaction:', id);
    // Implement edit transaction functionality
}

function deleteTransaction(id) {
    if (confirm('Bạn có chắc chắn muốn xóa giao dịch này?')) {
        console.log('Deleting transaction:', id);
        // Implement delete transaction functionality
        showSuccessMessage('Giao dịch đã được xóa thành công!');
    }
}
