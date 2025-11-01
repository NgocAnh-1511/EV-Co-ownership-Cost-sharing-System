// Global variables
let currentMode = 'checkin';
let qrStream = null;
let signatureCanvas = null;
let signatureCtx = null;
let isDrawing = false;

// API Configuration
const API_URL = 'http://localhost:8082/api/checkinout';

document.addEventListener('DOMContentLoaded', function () {
    initializeSignatureCanvas();
    setupEventListeners();
    updateUIForMode(currentMode);
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

// Setup Event Listeners
function setupEventListeners() {
    // Mode toggle
    document.querySelectorAll('.mode-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            document.querySelector('.mode-btn.active').classList.remove('active');
            this.classList.add('active');
            currentMode = this.dataset.mode;
            updateUIForMode(currentMode);
        });
    });

    // QR Scanner
    document.getElementById('scanQRBtn')?.addEventListener('click', function() {
        if (qrStream) {
            stopQRScanner();
        } else {
            startQRScanner();
        }
    });

    // Vehicle selection
    document.getElementById('vehicle-select')?.addEventListener('change', function() {
        updateVehicleStatus(this.value);
    });

    // Signature Canvas
    if (signatureCanvas) {
        signatureCanvas.addEventListener('mousedown', startDrawing);
        signatureCanvas.addEventListener('mousemove', draw);
        signatureCanvas.addEventListener('mouseup', stopDrawing);
        signatureCanvas.addEventListener('mouseout', stopDrawing);
        
        // Touch events for mobile
        signatureCanvas.addEventListener('touchstart', handleTouch);
        signatureCanvas.addEventListener('touchmove', handleTouch);
        signatureCanvas.addEventListener('touchend', stopDrawing);
    }

    // Clear signature
    document.querySelector('.btn-clear-signature')?.addEventListener('click', clearSignature);

    // Confirm button
    document.getElementById('confirmBtn')?.addEventListener('click', function() {
        handleConfirm();
    });

    // Cancel button
    document.getElementById('cancelBtn')?.addEventListener('click', function() {
        if (confirm('Hủy thao tác này?')) {
            resetForm();
        }
    });

    // Date and time inputs
    document.getElementById('date-input')?.addEventListener('change', updateTimestamp);
    document.getElementById('time-input')?.addEventListener('change', updateTimestamp);
}

// Update UI for mode (Check-in or Check-out)
function updateUIForMode(mode) {
    const confirmBtn = document.getElementById('confirmBtn');
    const qrScannerGroup = document.getElementById('qr-scanner-group');
    const vehicleStatusGroup = document.getElementById('vehicle-status-group');
    const vehicleStatusAfterGroup = document.getElementById('vehicle-status-after-group');
    const signatureGroup = document.getElementById('signature-group');
    const statusDisplay = document.getElementById('vehicle-status-display');
    
    if (mode === 'checkin') {
        confirmBtn.textContent = 'Hoàn Thành Check-in';
        qrScannerGroup.style.display = 'block';
        vehicleStatusGroup.style.display = 'block';
        vehicleStatusAfterGroup.style.display = 'none';
        signatureGroup.style.display = 'none';
        updateStatusDisplay('Vui lòng quét QR và kiểm tra xe trước khi giao', 'info');
    } else {
        confirmBtn.textContent = 'Hoàn Thành Check-out';
        qrScannerGroup.style.display = 'none';
        vehicleStatusGroup.style.display = 'none';
        vehicleStatusAfterGroup.style.display = 'block';
        signatureGroup.style.display = 'block';
        updateStatusDisplay('Vui lòng ký và kiểm tra xe khi trả', 'warning');
    }
}

// QR Scanner Functions
function startQRScanner() {
    const video = document.getElementById('qr-video');
    const canvas = document.getElementById('qr-canvas');
    const scanBtn = document.getElementById('scanQRBtn');
    
    navigator.mediaDevices.getUserMedia({ 
        video: { facingMode: 'environment' } 
    })
    .then(stream => {
        qrStream = stream;
        video.srcObject = stream;
        video.style.display = 'block';
        scanBtn.innerHTML = '<i class="fas fa-stop"></i> Dừng Camera';
        scanQRCode(video, canvas);
    })
    .catch(err => {
        console.error('Camera error:', err);
        alert('Không thể truy cập camera. Vui lòng cho phép quyền truy cập camera.');
    });
}

function stopQRScanner() {
    if (qrStream) {
        qrStream.getTracks().forEach(track => track.stop());
        qrStream = null;
    }
    const video = document.getElementById('qr-video');
    const canvas = document.getElementById('qr-canvas');
    const scanBtn = document.getElementById('scanQRBtn');
    video.style.display = 'none';
    video.srcObject = null;
    scanBtn.innerHTML = '<i class="fas fa-camera"></i> Bật Camera Quét QR';
    document.getElementById('qr-result').style.display = 'none';
}

function scanQRCode(video, canvas) {
    const qrResult = document.getElementById('qr-result');
    
    setInterval(() => {
        if (video.readyState === video.HAVE_ENOUGH_DATA) {
            canvas.width = video.videoWidth;
            canvas.height = video.videoHeight;
            const ctx = canvas.getContext('2d');
            ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
            
            const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
            // Simulate QR code detection
            // In production, use a QR library like jsQR
            const simulatedQR = simulateQRDetection();
            if (simulatedQR) {
                qrResult.textContent = `Đã quét: ${simulatedQR}`;
                qrResult.style.display = 'block';
                document.getElementById('vehicle-select').value = simulatedQR;
                updateVehicleStatus(simulatedQR);
                stopQRScanner();
            }
        }
    }, 100);
}

function simulateQRDetection() {
    // Simulate QR code scanning after 2 seconds
    setTimeout(() => {
        const vehicles = ['VEH001', 'VEH002', 'VEH003', 'VEH004'];
        return vehicles[Math.floor(Math.random() * vehicles.length)];
    }, 2000);
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

function handleTouch(e) {
    e.preventDefault();
    const touch = e.touches[0];
    const rect = signatureCanvas.getBoundingClientRect();
    const mouseEvent = {
        clientX: touch.clientX,
        clientY: touch.clientY
    };
    
    if (e.type === 'touchstart') {
        startDrawing(mouseEvent);
    } else if (e.type === 'touchmove') {
        draw(mouseEvent);
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

// Update Vehicle Status
function updateVehicleStatus(vehicleId) {
    if (!vehicleId) return;
    
    fetch(`${API_URL}/by-vehicle/${vehicleId}`)
        .then(res => res.json())
        .then(data => {
            if (data && data.length > 0) {
                const lastLog = data[data.length - 1];
                if (lastLog.status === 'checkin') {
                    updateStatusDisplay('Xe đã được nhận. Sẵn sàng trả xe.', 'warning');
                } else {
                    updateStatusDisplay('Xe có thể giao ngay. Tình trạng tốt.', 'success');
                }
            } else {
                updateStatusDisplay('Xe mới. Chưa có lịch sử.', 'info');
            }
        })
        .catch(err => {
            console.error('Error fetching vehicle status:', err);
            updateStatusDisplay('Không thể tải thông tin xe.', 'error');
        });
}

function updateStatusDisplay(message, type) {
    const statusText = document.getElementById('status-text');
    const statusDisplay = document.getElementById('vehicle-status-display');
    const statusBadge = statusDisplay.querySelector('.status-badge');
    
    statusText.textContent = message;
    statusBadge.className = 'status-badge';
    if (type === 'success') statusBadge.classList.add('success');
    if (type === 'warning') statusBadge.classList.add('warning');
    if (type === 'error') statusBadge.classList.add('error');
}

// Update Timestamp
function updateTimestamp() {
    const date = document.getElementById('date-input').value;
    const time = document.getElementById('time-input').value;
    // Auto-fill timestamp logic if needed
}

// Handle Confirm
function handleConfirm() {
    const vehicleId = document.getElementById('vehicle-select').value;
    const customerId = document.getElementById('customer-select').value;
    const notes = document.getElementById('notes').value;
    const performedBy = document.getElementById('performed-by').value;
    
    if (!vehicleId) {
        alert('⚠️ Vui lòng chọn xe!');
        return;
    }
    
    if (!customerId) {
        alert('⚠️ Vui lòng chọn khách hàng!');
        return;
    }
    
    // Validate checkboxes
    const checkboxes = currentMode === 'checkin' 
        ? document.querySelectorAll('#vehicle-status-group .checkbox-item input')
        : document.querySelectorAll('#vehicle-status-after-group .checkbox-item input');
    
    const unchecked = Array.from(checkboxes).filter(cb => !cb.checked);
    if (unchecked.length > 0) {
        alert(`⚠️ Vui lòng hoàn thành ${unchecked.length} mục checklist!`);
        return;
    }
    
    // Check signature for checkout
    if (currentMode === 'checkout') {
        const signature = document.getElementById('signature-image').value;
        if (!signature) {
            alert('⚠️ Vui lòng ký xác nhận khi trả xe!');
            return;
        }
    }
    
    // Prepare data
    const data = {
        vehicleId: vehicleId,
        status: currentMode,
        notes: notes,
        performedBy: performedBy,
        vehicleConditionBefore: currentMode === 'checkin' 
            ? document.getElementById('vehicle-condition-before').value 
            : null,
        vehicleConditionAfter: currentMode === 'checkout' 
            ? document.getElementById('vehicle-condition-after').value 
            : null,
        signatureImageUrl: currentMode === 'checkout' 
            ? document.getElementById('signature-image').value 
            : null,
        qrCodeData: vehicleId
    };
    
    // Send to API
    const endpoint = currentMode === 'checkin' ? '/checkin' : '/checkout';
    
    fetch(`${API_URL}${endpoint}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data)
    })
    .then(response => response.json())
    .then(data => {
        console.log('Success:', data);
        alert(`✅ Hoàn thành ${currentMode === 'checkin' ? 'check-in' : 'check-out'} thành công!`);
        resetForm();
    })
    .catch(error => {
        console.error('Error:', error);
        alert('❌ Có lỗi xảy ra. Vui lòng thử lại.');
    });
}

// Reset Form
function resetForm() {
    document.getElementById('vehicle-select').value = '';
    document.getElementById('customer-select').value = '';
    document.getElementById('notes').value = '';
    document.getElementById('vehicle-condition-before').value = '';
    document.getElementById('vehicle-condition-after').value = '';
    document.getElementById('qr-result').style.display = 'none';
    clearSignature();
    stopQRScanner();
    
    document.querySelectorAll('input[type="checkbox"]').forEach(cb => {
        cb.checked = false;
    });
    
    updateStatusDisplay('Vui lòng chọn xe và thực hiện quy trình', 'info');
}
