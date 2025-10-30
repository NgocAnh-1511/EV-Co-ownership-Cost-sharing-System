// Auto Split - Tự động chia chi phí
const AUTO_SPLIT_API = '/api/auto-split';
const GROUP_API = '/api/groups';
const COST_API = '/api/costs';

let groupsData = [];
let currentGroupId = null;

// Initialize on DOM load
document.addEventListener('DOMContentLoaded', function() {
    initializeForm();
    loadVehicles();
    loadRecentCosts();
    setActiveNavItem();
});

// Initialize form
function initializeForm() {
    const costForm = document.getElementById('costForm');
    if (costForm) {
        costForm.addEventListener('submit', handleFormSubmit);
    }
}

// Load vehicles (groups)
async function loadVehicles() {
    try {
        const response = await fetch(GROUP_API);
        if (!response.ok) throw new Error('Failed to fetch groups');
        
        groupsData = await response.json();
        const vehicleSelect = document.getElementById('vehicleId');
        
        if (vehicleSelect && groupsData && groupsData.length > 0) {
            vehicleSelect.innerHTML = '<option value="">-- Chọn xe --</option>' +
                groupsData.map(g => 
                    `<option value="${g.vehicleId}" data-group-id="${g.groupId}">${g.groupName}</option>`
                ).join('');
        }
    } catch (error) {
        console.error('Error loading vehicles:', error);
        showNotification('Không thể tải danh sách xe', 'error');
    }
}

// Load recent costs
async function loadRecentCosts() {
    try {
        const response = await fetch(COST_API);
        if (!response.ok) return;
        
        const costs = await response.json();
        const tbody = document.getElementById('recentCostsBody');
        
        if (!tbody) return;
        
        // Filter costs with splitMethod
        const autoSplitCosts = costs.filter(c => c.splitMethod).slice(0, 10);
        
        if (autoSplitCosts.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center">
                        <div class="empty-state">
                            <i class="fas fa-inbox"></i>
                            <p>Chưa có chi phí tự động chia nào</p>
                        </div>
                    </td>
                </tr>
            `;
            return;
        }
        
        tbody.innerHTML = autoSplitCosts.map(cost => `
            <tr>
                <td><span class="badge badge-info">${getCostTypeIcon(cost.costType)} ${getCostTypeLabel(cost.costType)}</span></td>
                <td>${cost.description || '-'}</td>
                <td><strong>${formatMoney(cost.amount)}</strong></td>
                <td><span class="badge badge-success">${getSplitMethodLabel(cost.splitMethod)}</span></td>
                <td>${formatDate(cost.createdAt)}</td>
                <td>
                    <button class="btn btn-sm btn-outline" onclick="viewCostDetail(${cost.costId})">
                        <i class="fas fa-eye"></i>
                    </button>
                </td>
            </tr>
        `).join('');
        
    } catch (error) {
        console.error('Error loading recent costs:', error);
    }
}

// Suggest split method based on cost type
function suggestSplitMethod() {
    const costType = document.getElementById('costType').value;
    const splitMethodSelect = document.getElementById('splitMethod');
    const hint = document.getElementById('splitMethodHint');

    const suggestions = {
        'ElectricCharge': { method: 'BY_USAGE', text: '💡 Khuyến nghị: Chia theo km đã chạy' },
        'Maintenance': { method: 'BY_OWNERSHIP', text: '💡 Khuyến nghị: Chia theo sở hữu' },
        'Insurance': { method: 'BY_OWNERSHIP', text: '💡 Khuyến nghị: Chia theo sở hữu' },
        'Inspection': { method: 'BY_OWNERSHIP', text: '💡 Khuyến nghị: Chia theo sở hữu' },
        'Cleaning': { method: 'EQUAL', text: '💡 Khuyến nghị: Chia đều' },
        'Other': { method: 'EQUAL', text: '💡 Khuyến nghị: Chia đều' }
    };

    if (suggestions[costType]) {
        splitMethodSelect.value = suggestions[costType].method;
        hint.textContent = suggestions[costType].text;
        hint.style.color = '#10b981';
        updatePreview();
    }
}

// Update preview
async function updatePreview() {
    const vehicleSelect = document.getElementById('vehicleId');
    const amount = parseFloat(document.getElementById('amount').value) || 0;
    const splitMethod = document.getElementById('splitMethod').value;

    if (!vehicleSelect.value || !amount || !splitMethod) {
        document.getElementById('previewSection').style.display = 'none';
        return;
    }

    // Get groupId from selected vehicle
    const selectedOption = vehicleSelect.options[vehicleSelect.selectedIndex];
    const groupId = selectedOption.dataset.groupId;
    currentGroupId = groupId;

    try {
        showLoading();
        
        // Get preview from backend
        const response = await fetch(`${AUTO_SPLIT_API}/preview`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                groupId: parseInt(groupId),
                amount: amount,
                splitMethod: splitMethod,
                month: new Date().getMonth() + 1,
                year: new Date().getFullYear()
            })
        });

        if (!response.ok) throw new Error('Failed to get preview');

        const preview = await response.json();
        displayPreview(preview, amount, splitMethod);

    } catch (error) {
        console.error('Error getting preview:', error);
        // Fallback to simple preview
        displaySimplePreview(amount, splitMethod);
    } finally {
        hideLoading();
    }
}

// Display preview
function displayPreview(preview, amount, splitMethod) {
    document.getElementById('previewSection').style.display = 'block';
    document.getElementById('previewTotal').textContent = formatMoney(amount);
    document.getElementById('previewMembers').textContent = preview.shares.length;
    document.getElementById('previewMethod').textContent = getSplitMethodLabel(splitMethod);

    const tbody = document.getElementById('previewTableBody');
    tbody.innerHTML = preview.shares.map(share => `
        <tr>
            <td><strong>User ${share.userId}</strong></td>
            <td><span class="badge badge-primary">${share.percent.toFixed(2)}%</span></td>
            <td><strong>${formatMoney(share.amountShare)}</strong></td>
        </tr>
    `).join('');
}

// Display simple preview (fallback)
function displaySimplePreview(amount, splitMethod) {
    document.getElementById('previewSection').style.display = 'block';
    document.getElementById('previewTotal').textContent = formatMoney(amount);
    document.getElementById('previewMembers').textContent = '?';
    document.getElementById('previewMethod').textContent = getSplitMethodLabel(splitMethod);

    const tbody = document.getElementById('previewTableBody');
    tbody.innerHTML = `
        <tr>
            <td colspan="3" class="text-center">
                <i class="fas fa-spinner fa-spin"></i> Đang tải preview...
            </td>
        </tr>
    `;
}

// Handle form submit
async function handleFormSubmit(e) {
    e.preventDefault();

    const vehicleSelect = document.getElementById('vehicleId');
    const selectedOption = vehicleSelect.options[vehicleSelect.selectedIndex];
    const groupId = selectedOption.dataset.groupId;
    
    const formData = {
        vehicleId: parseInt(vehicleSelect.value),
        costType: document.getElementById('costType').value,
        amount: parseFloat(document.getElementById('amount').value),
        description: document.getElementById('description').value,
        splitMethod: document.getElementById('splitMethod').value,
        groupId: parseInt(groupId),
        month: new Date().getMonth() + 1,
        year: new Date().getFullYear()
    };

    try {
        showLoading();
        
        const response = await fetch(`${AUTO_SPLIT_API}/create-and-split`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        if (!response.ok) throw new Error('Failed to create cost');

        const result = await response.json();
        
        showNotification(
            `✅ Thành công!\n\nĐã tạo chi phí ${formatMoney(result.cost.amount)}\nĐã chia cho ${result.shares.length} thành viên`,
            'success'
        );

        // Reset form and reload
        resetForm();
        loadRecentCosts();
        
        // Redirect to cost list after 2 seconds
        setTimeout(() => {
            window.location.href = '/costs';
        }, 2000);

    } catch (error) {
        console.error('Error creating cost:', error);
        showNotification('Có lỗi xảy ra khi tạo chi phí. Vui lòng thử lại.', 'error');
    } finally {
        hideLoading();
    }
}

// Reset form
function resetForm() {
    document.getElementById('costForm').reset();
    document.getElementById('previewSection').style.display = 'none';
    document.getElementById('splitMethodHint').textContent = '';
}

// View cost detail
function viewCostDetail(costId) {
    window.location.href = `/costs/${costId}/edit`;
}

// Helper functions
function getCostTypeIcon(costType) {
    const icons = {
        'ElectricCharge': '⚡',
        'Maintenance': '🔧',
        'Insurance': '🛡️',
        'Inspection': '📋',
        'Cleaning': '🧽',
        'Other': '📦'
    };
    return icons[costType] || '📦';
}

function getCostTypeLabel(costType) {
    const labels = {
        'ElectricCharge': 'Sạc điện',
        'Maintenance': 'Bảo dưỡng',
        'Insurance': 'Bảo hiểm',
        'Inspection': 'Đăng kiểm',
        'Cleaning': 'Vệ sinh',
        'Other': 'Khác'
    };
    return labels[costType] || costType;
}

function getSplitMethodLabel(method) {
    const labels = {
        'BY_OWNERSHIP': '📊 Theo sở hữu',
        'BY_USAGE': '🛣️ Theo km',
        'EQUAL': '➗ Chia đều',
        'CUSTOM': '✏️ Tùy chỉnh'
    };
    return labels[method] || method;
}

function formatMoney(value) {
    return new Intl.NumberFormat('vi-VN').format(value) + ' VNĐ';
}

function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
}

function showNotification(message, type = 'info') {
    alert(message);
}

function showLoading() {
    document.body.style.cursor = 'wait';
}

function hideLoading() {
    document.body.style.cursor = 'default';
}

