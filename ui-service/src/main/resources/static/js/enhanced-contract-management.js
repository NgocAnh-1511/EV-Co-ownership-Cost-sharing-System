// API Configuration
const CONTRACT_API_URL = 'http://localhost:8082/api/legalcontracts';
const GROUP_API_URL = 'http://localhost:8083/api/vehiclegroups';
let currentFilter = 'all';
let contracts = [];
let groups = [];
let selectedContractId = null;

// Signature Canvas
let signatureCanvas = null;
let signatureCtx = null;
let isDrawing = false;

document.addEventListener('DOMContentLoaded', function () {
    initializeSignatureCanvas();
    initializeListeners();
    loadGroups();
    loadContracts();
});

// Initialize Signature Canvas
function initializeSignatureCanvas() {
    signatureCanvas = document.getElementById('signature-canvas');
    if (signatureCanvas) {
        signatureCtx = signatureCanvas.getContext('2d');
        signatureCanvas.width = signatureCanvas.offsetWidth;
        signatureCanvas.height = signatureCanvas.offsetHeight;
        signatureCtx.strokeStyle = '#000';
        signatureCtx.lineWidth = 2;
        signatureCtx.lineCap = 'round';
        signatureCtx.lineJoin = 'round';
    }
}

// Initialize Event Listeners
function initializeListeners() {
    // Filter tabs
    document.querySelectorAll('.filter-tab').forEach(tab => {
        tab.addEventListener('click', function () {
            document.querySelector('.filter-tab.active').classList.remove('active');
            this.classList.add('active');
            currentFilter = this.dataset.filter;
            renderContractsList();
        });
    });

    // Create button
    document.getElementById('createContractBtn').addEventListener('click', () => {
        selectedContractId = null;
        document.getElementById('contract-form-panel').scrollIntoView({ behavior: 'smooth' });
        document.getElementById('contract-code').value = 'CONTRACT-' + Date.now();
    });

    // Confirm button
    document.getElementById('confirmBtn').addEventListener('click', handleConfirm);

    // Sign button
    document.getElementById('signBtn').addEventListener('click', handleSign);

    // Cancel button
    document.getElementById('cancelBtn').addEventListener('click', resetForm);

    // Add party button
    document.getElementById('addPartyBtn').addEventListener('click', addParty);

    // Signature canvas
    if (signatureCanvas) {
        signatureCanvas.addEventListener('mousedown', startDrawing);
        signatureCanvas.addEventListener('mousemove', draw);
        signatureCanvas.addEventListener('mouseup', stopDrawing);
        signatureCanvas.addEventListener('mouseout', stopDrawing);
    }

    document.querySelector('.btn-clear-signature')?.addEventListener('click', clearSignature);

    // Auto-generate code
    document.getElementById('contract-type').addEventListener('change', function() {
        if (!selectedContractId) {
            const code = 'CONTRACT-' + this.value.substring(0, 3).toUpperCase() + '-' + Date.now();
            document.getElementById('contract-code').value = code;
        }
    });

    // Show/hide signature
    document.getElementById('contract-status').addEventListener('change', function() {
        if (this.value === 'pending' || this.value === 'signed') {
            document.getElementById('signature-group').style.display = 'block';
            document.getElementById('signBtn').style.display = 'block';
        } else {
            document.getElementById('signature-group').style.display = 'none';
            document.getElementById('signBtn').style.display = 'none';
        }
    });
}

// Load Groups
function loadGroups() {
    fetch(`${GROUP_API_URL}/all`)
        .then(res => res.json())
        .then(data => {
            groups = data;
            const select = document.getElementById('vehicle-group');
            select.innerHTML = '<option value="">Chọn nhóm</option>';
            data.forEach(group => {
                const option = document.createElement('option');
                option.value = group.id;
                option.textContent = group.groupName;
                select.appendChild(option);
            });
        })
        .catch(err => {
            console.error('Error loading groups:', err);
        });
}

// Load Contracts
function loadContracts() {
    fetch(`${CONTRACT_API_URL}/all`)
        .then(res => res.json())
        .then(data => {
            contracts = data;
            updateStats();
            renderContractsList();
        })
        .catch(err => {
            console.error('Error loading contracts:', err);
        });
}

// Render Contracts List
function renderContractsList() {
    const contractsList = document.getElementById('contracts-list');
    contractsList.innerHTML = '';

    let filteredContracts = contracts;

    if (currentFilter !== 'all') {
        filteredContracts = contracts.filter(c => c.contractStatus === currentFilter);
    }

    if (filteredContracts.length === 0) {
        contractsList.innerHTML = '<div class="empty-state">Không có hợp đồng nào</div>';
        return;
    }

    filteredContracts.sort((a, b) => {
        const dateA = new Date(a.creationDate);
        const dateB = new Date(b.creationDate);
        return dateB - dateA;
    });

    filteredContracts.forEach(contract => {
        const item = createContractItem(contract);
        contractsList.appendChild(item);
    });
}

// Create Contract Item
function createContractItem(contract) {
    const template = document.getElementById('contract-item-template');
    const item = template.content.cloneNode(true);

    item.querySelector('.service-name').textContent = contract.contractCode || 'Chưa có mã';
    item.querySelector('.service-vehicle').textContent = 'Hợp đồng đồng sở hữu';
    
    const creationDate = new Date(contract.creationDate);
    item.querySelector('.service-date').textContent = `Ngày: ${creationDate.toLocaleDateString('vi-VN')}`;

    const statusBadge = item.querySelector('.status-badge');
    statusBadge.textContent = getStatusText(contract.contractStatus);
    statusBadge.className = `status-badge status-${contract.contractStatus}`;

    // Add handlers
    item.querySelector('.btn-edit-contract').addEventListener('click', () => editContract(contract));
    item.querySelector('.btn-sign-contract').addEventListener('click', () => signContract(contract.id));
    item.querySelector('.btn-delete-contract').addEventListener('click', () => deleteContract(contract.id));

    return item;
}

// Get Status Text
function getStatusText(status) {
    const statusMap = {
        'draft': 'Dự thảo',
        'pending': 'Chờ ký',
        'signed': 'Đã ký',
        'archived': 'Đã lưu trữ'
    };
    return statusMap[status] || status;
}

// Update Stats
function updateStats() {
    document.getElementById('total-contracts').textContent = contracts.length;
    document.getElementById('pending-contracts').textContent = contracts.filter(c => c.contractStatus === 'pending').length;
    document.getElementById('signed-contracts').textContent = contracts.filter(c => c.contractStatus === 'signed').length;
    document.getElementById('archived-contracts').textContent = contracts.filter(c => c.contractStatus === 'archived').length;
}

// Edit Contract
function editContract(contract) {
    selectedContractId = contract.id;

    document.getElementById('contract-code').value = contract.contractCode;
    document.getElementById('contract-status').value = contract.contractStatus;
    document.getElementById('contract-description').value = contract.description || '';

    if (contract.creationDate) {
        document.getElementById('creation-date').value = new Date(contract.creationDate).toISOString().split('T')[0];
    }

    if (contract.signedDate) {
        document.getElementById('signed-date').value = new Date(contract.signedDate).toISOString().split('T')[0];
    }
}

// Sign Contract
function signContract(id) {
    if (!confirm('Bạn có chắc muốn ký hợp đồng này?')) {
        return;
    }

    fetch(`${CONTRACT_API_URL}/sign/${id}`, {
        method: 'PUT'
    })
        .then(res => res.json())
        .then(contract => {
            console.log('Contract signed:', contract);
            alert('✅ Đã ký hợp đồng thành công!');
            loadContracts();
        })
        .catch(err => {
            console.error('Error signing contract:', err);
            alert('❌ Không thể ký hợp đồng');
        });
}

// Delete Contract
function deleteContract(id) {
    if (!confirm('Bạn có chắc muốn xóa hợp đồng này?')) {
        return;
    }

    fetch(`${CONTRACT_API_URL}/${id}`, {
        method: 'DELETE'
    })
        .then(() => {
            loadContracts();
            alert('✅ Đã xóa hợp đồng thành công');
        })
        .catch(err => {
            console.error('Error deleting contract:', err);
            alert('❌ Không thể xóa hợp đồng');
        });
}

// Handle Confirm
function handleConfirm() {
    const contractCode = document.getElementById('contract-code').value;
    const contractStatus = document.getElementById('contract-status').value;
    const description = document.getElementById('contract-description').value;

    if (!contractCode) {
        alert('⚠️ Vui lòng nhập mã hợp đồng!');
        return;
    }

    const data = {
        contractCode: contractCode,
        contractStatus: contractStatus,
        description: description
    };

    if (selectedContractId) {
        updateContract(selectedContractId, data);
    } else {
        createContract(data);
    }
}

// Create Contract
function createContract(data) {
    fetch(`${CONTRACT_API_URL}/create`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(res => res.json())
        .then(contract => {
            console.log('Contract created:', contract);
            alert('✅ Tạo hợp đồng thành công!');
            resetForm();
            loadContracts();
        })
        .catch(err => {
            console.error('Error creating contract:', err);
            alert('❌ Không thể tạo hợp đồng');
        });
}

// Update Contract
function updateContract(id, data) {
    fetch(`${CONTRACT_API_URL}/update/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(res => res.json())
        .then(contract => {
            console.log('Contract updated:', contract);
            alert('✅ Cập nhật hợp đồng thành công!');
            resetForm();
            loadContracts();
        })
        .catch(err => {
            console.error('Error updating contract:', err);
            alert('❌ Không thể cập nhật hợp đồng');
        });
}

// Handle Sign
function handleSign() {
    if (!selectedContractId) {
        alert('Vui lòng chọn hợp đồng để ký');
        return;
    }

    signContract(selectedContractId);
}

// Add Party
function addParty() {
    const partiesList = document.getElementById('parties-list');
    const partyItem = document.createElement('div');
    partyItem.className = 'party-item';
    partyItem.innerHTML = `
        <input type="text" placeholder="Nhập tên bên">
        <button class="btn-remove-party"><i class="fas fa-times"></i></button>
    `;
    partiesList.appendChild(partyItem);

    partyItem.querySelector('.btn-remove-party').addEventListener('click', () => {
        partyItem.remove();
    });
}

// Signature Functions
function startDrawing(e) {
    isDrawing = true;
    const rect = signatureCanvas.getBoundingClientRect();
    signatureCtx.beginPath();
    signatureCtx.moveTo(e.clientX - rect.left, e.clientY - rect.top);
}

function draw(e) {
    if (!isDrawing) return;
    const rect = signatureCanvas.getBoundingClientRect();
    signatureCtx.lineTo(e.clientX - rect.left, e.clientY - rect.top);
    signatureCtx.stroke();
}

function stopDrawing() {
    if (isDrawing) {
        isDrawing = false;
        signatureCtx.beginPath();
        saveSignature();
    }
}

function clearSignature() {
    signatureCtx.clearRect(0, 0, signatureCanvas.width, signatureCanvas.height);
    document.getElementById('signature-image').value = '';
}

function saveSignature() {
    const signatureData = signatureCanvas.toDataURL('image/png');
    document.getElementById('signature-image').value = signatureData;
}

// Reset Form
function resetForm() {
    document.getElementById('contract-code').value = '';
    document.getElementById('contract-status').value = 'draft';
    document.getElementById('contract-description').value = '';
    selectedContractId = null;
}




