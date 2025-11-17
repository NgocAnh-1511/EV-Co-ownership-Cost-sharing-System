// user-onboarding.js
// (File user-guard.js đã được chèn vào <head> để bảo vệ)

document.addEventListener('DOMContentLoaded', function() {

    const form = document.getElementById('onboardingForm');
    const statusMessage = document.getElementById('status-message');

    const API_BASE_URL = typeof window.getApiBaseUrl === 'function'
        ? window.getApiBaseUrl()
        : 'http://localhost:8084';
    const PROFILE_API_URL = `${API_BASE_URL}/api/auth/users/profile`;
    const UPLOAD_API_URL = `${API_BASE_URL}/api/auth/users/profile/upload`;

    // --- (Lệnh gọi checkAuthAndLoadUser() đã được XÓA khỏi đây) ---
    // (File auth-utils.js sẽ tự động chạy)

    /**
     * TẢI DỮ LIỆU: Gọi API GET /profile để điền vào form.
     */
    async function loadUserProfile() {
        try {
            const response = await authenticatedFetch(PROFILE_API_URL, {
                method: 'GET'
            });

            if (response.ok) {
                const user = await response.json();
                if (typeof window.updateStoredProfileStatus === 'function') {
                    window.updateStoredProfileStatus(user.profileStatus || 'PENDING');
                }

                // 1. Điền dữ liệu text
                document.getElementById('fullName').value = user.fullName || '';
                document.getElementById('email').value = user.email || '';
                document.getElementById('phoneNumber').value = user.phoneNumber || '';
                document.getElementById('dateOfBirth').value = user.dateOfBirth || '';
                document.getElementById('idCardNumber').value = user.idCardNumber || '';
                document.getElementById('idCardIssueDate').value = user.idCardIssueDate || '';
                document.getElementById('idCardIssuePlace').value = user.idCardIssuePlace || '';
                document.getElementById('licenseNumber').value = user.licenseNumber || '';
                if(user.licenseClass) document.getElementById('licenseClass').value = user.licenseClass;
                document.getElementById('licenseIssueDate').value = user.licenseIssueDate || '';
                document.getElementById('licenseExpiryDate').value = user.licenseExpiryDate || '';

                // 2. Cập nhật tên User trên Header (Nếu auth-utils chưa kịp chạy)
                const userNameDisplay = document.getElementById('userNameDisplay');
                if(userNameDisplay) userNameDisplay.textContent = user.fullName || user.email;

                // 3. Hiển thị ảnh đã tải lên
                displayUploadedImage('file-cmnd-front', user.idCardFrontUrl);
                displayUploadedImage('file-cmnd-back', user.idCardBackUrl);
                displayUploadedImage('file-license', user.licenseImageUrl);
                displayUploadedImage('file-portrait', user.portraitImageUrl);

            } else if (response.status === 401 && typeof logout === 'function') {
                 logout({ skipRemote: true });
            }
        } catch (error) {
            console.error("Lỗi tải hồ sơ:", error);
            statusMessage.classList.add('error');
            statusMessage.textContent = 'Không thể tải hồ sơ. Vui lòng thử lại.';
            statusMessage.style.display = 'block';
        }
    }

    // Hàm hiển thị ảnh đã tải lên
    function displayUploadedImage(elementId, url) {
        const fileInput = document.getElementById(elementId);
        if (!fileInput) return;
        const dropZone = fileInput.closest('.drop-zone');
        if (!dropZone) return;

        if (url) {
            dropZone.innerHTML = `<img src="${url}" alt="Ảnh đã tải lên">`;
            dropZone.classList.add('has-image');
        }
    }

    // Gọi hàm tải dữ liệu
    loadUserProfile();

    /**
     * HÀM UPLOAD THỰC TẾ
     */
    async function uploadImage(fileId) {
        const fileInput = document.getElementById(fileId);
        if (fileInput.files.length === 0) return null;

        const file = fileInput.files[0];
        const formData = new FormData();
        formData.append("file", file);

        try {
            const response = await authenticatedFetch(UPLOAD_API_URL, {
                method: 'POST',
                body: formData
            });

            if (response.ok) {
                const result = await response.json();
                return result.fileUrl;
            } else {
                throw new Error(await response.text());
            }
        } catch (error) {
            console.error(`Lỗi tải file ${file.name}:`, error);
            throw error;
        }
    }

    /**
     * HÀM XỬ LÝ FORM SUBMIT (CẬP NHẬT)
     */
    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        statusMessage.className = 'status-message';
        statusMessage.style.display = 'none';

        // Validation Số điện thoại
        const phoneNumber = document.getElementById('phoneNumber').value;
        const phoneRegex = /^0[0-9]{9}$/;
        if (!phoneRegex.test(phoneNumber)) {
            statusMessage.classList.add('error');
            statusMessage.textContent = 'Số điện thoại không hợp lệ. Vui lòng nhập 10 chữ số bắt đầu bằng 0.';
            statusMessage.style.display = 'block';
            document.getElementById('phoneNumber').focus();
            return;
        }

        try {
            statusMessage.textContent = 'Đang tải lên ảnh và cập nhật hồ sơ...';
            statusMessage.style.display = 'block';

            const [idCardFrontUrl, idCardBackUrl, licenseImageUrl, portraitImageUrl] = await Promise.all([
                uploadImage('file-cmnd-front'),
                uploadImage('file-cmnd-back'),
                uploadImage('file-license'),
                uploadImage('file-portrait')
            ]);

            const updateData = {
                fullName: document.getElementById('fullName').value,
                phoneNumber: phoneNumber,
                dateOfBirth: document.getElementById('dateOfBirth').value,
                idCardNumber: document.getElementById('idCardNumber').value,
                idCardIssueDate: document.getElementById('idCardIssueDate').value,
                idCardIssuePlace: document.getElementById('idCardIssuePlace').value,
                licenseNumber: document.getElementById('licenseNumber').value,
                licenseClass: document.getElementById('licenseClass').value,
                licenseIssueDate: document.getElementById('licenseIssueDate').value,
                licenseExpiryDate: document.getElementById('licenseExpiryDate').value,
                idCardFrontUrl: idCardFrontUrl,
                idCardBackUrl: idCardBackUrl,
                licenseImageUrl: licenseImageUrl,
                portraitImageUrl: portraitImageUrl,
            };

            const filteredUpdateData = Object.fromEntries(
                Object.entries(updateData).filter(([_, v]) => v != null)
            );

            const response = await authenticatedFetch(PROFILE_API_URL, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(filteredUpdateData)
            });

            const result = await response.json();

            if (response.ok) {
                statusMessage.classList.add('success');
                statusMessage.textContent = 'Đăng ký thông tin thành công! Đang chuyển hướng...';
                statusMessage.style.display = 'block';
                localStorage.setItem('userName', updateData.fullName);
                if (typeof window.updateStoredProfileStatus === 'function') {
                    window.updateStoredProfileStatus('PENDING');
                }

                setTimeout(() => {
                    window.location.href = '/user/auth-profile-status';
                }, 2000);
            } else {
                statusMessage.classList.add('error');
                statusMessage.textContent = 'Đăng ký thất bại: ' + (result.message || JSON.stringify(result));
                statusMessage.style.display = 'block';
            }
        } catch (error) {
            statusMessage.classList.add('error');
            statusMessage.textContent = 'Lỗi kết nối hoặc xử lý: ' + error.message;
            statusMessage.style.display = 'block';
        }
    });
});