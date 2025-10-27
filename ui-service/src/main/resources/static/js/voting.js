// Voting Page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initializeVotingPage();
});

function initializeVotingPage() {
    loadVotingStats();
    loadActiveVotings();
    loadRecentResults();
    loadAllVotings();
    loadGroups();
    
    // Setup event listeners
    setupEventListeners();
}

function setupEventListeners() {
    // Filter change events
    document.getElementById('filterGroup').addEventListener('change', filterVotings);
    document.getElementById('filterStatus').addEventListener('change', filterVotings);
    
    // Form submissions
    document.getElementById('createVotingForm').addEventListener('submit', handleCreateVoting);
    document.getElementById('votingForm').addEventListener('submit', handleVoteSubmission);
    
    // Modal close events
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('modal')) {
            closeAllModals();
        }
    });
}

// Load voting statistics
async function loadVotingStats() {
    try {
        const response = await fetch('/api/votings/stats');
        if (response.ok) {
            const stats = await response.json();
            updateStatsDisplay(stats);
        }
    } catch (error) {
        console.error('Error loading voting stats:', error);
    }
}

function updateStatsDisplay(stats) {
    document.getElementById('totalVotings').textContent = stats.totalVotings || 0;
    document.getElementById('completedVotings').textContent = stats.completedVotings || 0;
    document.getElementById('activeVotings').textContent = stats.activeVotings || 0;
    document.getElementById('totalParticipants').textContent = stats.totalParticipants || 0;
}

// Load active votings
async function loadActiveVotings() {
    try {
        const response = await fetch('/api/votings/active');
        if (response.ok) {
            const votings = await response.json();
            displayActiveVotings(votings);
        }
    } catch (error) {
        console.error('Error loading active votings:', error);
    }
}

function displayActiveVotings(votings) {
    const container = document.getElementById('activeVotingsList');
    
    if (votings.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-vote-yea"></i>
                <p>Chưa có cuộc bỏ phiếu nào đang diễn ra</p>
            </div>
        `;
        return;
    }
    
    container.innerHTML = votings.map(voting => `
        <div class="voting-item">
            <div class="voting-header">
                <h4>${voting.topic}</h4>
                <span class="status-badge active">Đang diễn ra</span>
            </div>
            <p class="voting-description">${voting.description || 'Không có mô tả'}</p>
            <div class="voting-meta">
                <span><i class="fas fa-users"></i> ${voting.groupName}</span>
                <span><i class="fas fa-clock"></i> Còn lại: ${formatTimeRemaining(voting.deadline)}</span>
            </div>
            <div class="voting-actions">
                <button class="btn btn-primary" onclick="openVotingModal(${voting.id})">
                    <i class="fas fa-vote-yea"></i>
                    Bỏ phiếu
                </button>
            </div>
        </div>
    `).join('');
}

// Load recent results
async function loadRecentResults() {
    try {
        const response = await fetch('/api/votings/results/recent');
        if (response.ok) {
            const results = await response.json();
            displayRecentResults(results);
        }
    } catch (error) {
        console.error('Error loading recent results:', error);
    }
}

function displayRecentResults(results) {
    const container = document.getElementById('recentResultsList');
    
    if (results.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-chart-bar"></i>
                <p>Chưa có kết quả nào</p>
            </div>
        `;
        return;
    }
    
    container.innerHTML = results.map(result => `
        <div class="result-item">
            <div class="result-header">
                <h4>${result.topic}</h4>
                <span class="status-badge completed">Đã hoàn thành</span>
            </div>
            <div class="result-summary">
                <div class="result-option">
                    <span class="option-label">${result.optionA}:</span>
                    <span class="option-count">${result.votesA} phiếu</span>
                </div>
                <div class="result-option">
                    <span class="option-label">${result.optionB}:</span>
                    <span class="option-count">${result.votesB} phiếu</span>
                </div>
            </div>
            <div class="result-meta">
                <span><i class="fas fa-users"></i> ${result.groupName}</span>
                <span><i class="fas fa-calendar"></i> ${formatDate(result.createdAt)}</span>
            </div>
        </div>
    `).join('');
}

// Load all votings
async function loadAllVotings() {
    try {
        const response = await fetch('/api/votings');
        if (response.ok) {
            const votings = await response.json();
            displayAllVotings(votings);
        }
    } catch (error) {
        console.error('Error loading all votings:', error);
    }
}

function displayAllVotings(votings) {
    const tbody = document.getElementById('votingsTableBody');
    
    if (votings.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="empty-table">
                    <div class="empty-state">
                        <i class="fas fa-vote-yea"></i>
                        <p>Chưa có cuộc bỏ phiếu nào</p>
                    </div>
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = votings.map(voting => `
        <tr>
            <td>
                <div class="voting-topic">
                    <strong>${voting.topic}</strong>
                    <small>${voting.description || ''}</small>
                </div>
            </td>
            <td>${voting.groupName}</td>
            <td>${voting.createdBy}</td>
            <td>
                <span class="status-badge ${voting.status.toLowerCase()}">
                    ${getStatusText(voting.status)}
                </span>
            </td>
            <td>
                ${voting.status === 'COMPLETED' ? 
                    `${voting.optionA}: ${voting.votesA} | ${voting.optionB}: ${voting.votesB}` : 
                    'Chưa có kết quả'
                }
            </td>
            <td>${formatDate(voting.createdAt)}</td>
            <td>
                <div class="action-buttons">
                    ${voting.status === 'ACTIVE' ? 
                        `<button class="btn btn-sm btn-primary" onclick="openVotingModal(${voting.id})">
                            <i class="fas fa-vote-yea"></i>
                        </button>` : 
                        `<button class="btn btn-sm btn-outline" onclick="viewVotingDetails(${voting.id})">
                            <i class="fas fa-eye"></i>
                        </button>`
                    }
                    <button class="btn btn-sm btn-outline" onclick="viewVotingDetails(${voting.id})">
                        <i class="fas fa-info-circle"></i>
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
            populateGroupSelect(groups);
        }
    } catch (error) {
        console.error('Error loading groups:', error);
    }
}

function populateGroupSelect(groups) {
    const select = document.getElementById('votingGroup');
    const filterSelect = document.getElementById('filterGroup');
    
    const options = groups.map(group => 
        `<option value="${group.id}">${group.name}</option>`
    ).join('');
    
    select.innerHTML = '<option value="">Chọn nhóm</option>' + options;
    filterSelect.innerHTML = '<option value="">Tất cả nhóm</option>' + options;
}

// Filter votings
function filterVotings() {
    const groupFilter = document.getElementById('filterGroup').value;
    const statusFilter = document.getElementById('filterStatus').value;
    
    // This would typically make an API call with filters
    // For now, we'll just reload all votings
    loadAllVotings();
}

// Modal functions
function openCreateVotingModal() {
    document.getElementById('createVotingModal').classList.add('show');
}

function closeCreateVotingModal() {
    document.getElementById('createVotingModal').classList.remove('show');
    document.getElementById('createVotingForm').reset();
}

function openVotingModal(votingId) {
    loadVotingDetails(votingId);
    document.getElementById('votingModal').classList.add('show');
}

function closeVotingModal() {
    document.getElementById('votingModal').classList.remove('show');
    document.getElementById('votingForm').reset();
}

function closeAllModals() {
    document.querySelectorAll('.modal').forEach(modal => {
        modal.classList.remove('show');
    });
}

// Load voting details for voting modal
async function loadVotingDetails(votingId) {
    try {
        const response = await fetch(`/api/votings/${votingId}`);
        if (response.ok) {
            const voting = await response.json();
            displayVotingDetails(voting);
        }
    } catch (error) {
        console.error('Error loading voting details:', error);
    }
}

function displayVotingDetails(voting) {
    const container = document.getElementById('votingDetails');
    
    container.innerHTML = `
        <h4>${voting.topic}</h4>
        <p>${voting.description || 'Không có mô tả'}</p>
        <div class="voting-info">
            <span><i class="fas fa-users"></i> Nhóm: ${voting.groupName}</span>
            <span><i class="fas fa-clock"></i> Hạn: ${formatDateTime(voting.deadline)}</span>
        </div>
    `;
    
    document.getElementById('optionALabel').textContent = voting.optionA;
    document.getElementById('optionBLabel').textContent = voting.optionB;
    
    // Store voting ID for form submission
    document.getElementById('votingForm').dataset.votingId = voting.id;
}

// Handle form submissions
async function handleCreateVoting(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const votingData = {
        groupId: formData.get('groupId'),
        topic: formData.get('topic'),
        optionA: formData.get('optionA'),
        optionB: formData.get('optionB'),
        description: formData.get('description'),
        deadline: formData.get('deadline')
    };
    
    try {
        const response = await fetch('/api/votings', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(votingData)
        });
        
        if (response.ok) {
            closeCreateVotingModal();
            loadVotingStats();
            loadAllVotings();
            showNotification('Cuộc bỏ phiếu đã được tạo thành công!', 'success');
        } else {
            showNotification('Có lỗi xảy ra khi tạo cuộc bỏ phiếu', 'error');
        }
    } catch (error) {
        console.error('Error creating voting:', error);
        showNotification('Có lỗi xảy ra khi tạo cuộc bỏ phiếu', 'error');
    }
}

async function handleVoteSubmission(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const votingId = e.target.dataset.votingId;
    const voteData = {
        choice: formData.get('choice'),
        comment: formData.get('comment')
    };
    
    try {
        const response = await fetch(`/api/votings/${votingId}/vote`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(voteData)
        });
        
        if (response.ok) {
            closeVotingModal();
            loadVotingStats();
            loadActiveVotings();
            loadAllVotings();
            showNotification('Phiếu bầu của bạn đã được gửi thành công!', 'success');
        } else {
            showNotification('Có lỗi xảy ra khi gửi phiếu bầu', 'error');
        }
    } catch (error) {
        console.error('Error submitting vote:', error);
        showNotification('Có lỗi xảy ra khi gửi phiếu bầu', 'error');
    }
}

// Utility functions
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
}

function formatDateTime(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString('vi-VN');
}

function formatTimeRemaining(deadline) {
    const now = new Date();
    const deadlineDate = new Date(deadline);
    const diff = deadlineDate - now;
    
    if (diff <= 0) return 'Đã hết hạn';
    
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    
    if (days > 0) {
        return `${days} ngày ${hours} giờ`;
    } else if (hours > 0) {
        return `${hours} giờ`;
    } else {
        const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        return `${minutes} phút`;
    }
}

function getStatusText(status) {
    const statusMap = {
        'ACTIVE': 'Đang diễn ra',
        'COMPLETED': 'Đã hoàn thành',
        'CANCELLED': 'Đã hủy'
    };
    return statusMap[status] || status;
}

function exportVotingResults() {
    // Implementation for exporting voting results
    showNotification('Tính năng xuất kết quả đang được phát triển', 'info');
}

function viewVotingDetails(votingId) {
    // Implementation for viewing voting details
    showNotification('Tính năng xem chi tiết đang được phát triển', 'info');
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
`;
document.head.appendChild(style);
