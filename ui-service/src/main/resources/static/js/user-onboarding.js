// user-onboarding.js

document.addEventListener('DOMContentLoaded', function() {
    // Lưu ý: Cần đảm bảo file auth-utils.js đã được load để sử dụng hàm logout()

    const form = document.getElementById('onboardingForm');
    const statusMessage = document.getElementById('status-message');
    const token = localStorage.getItem('jwtToken');

    // URLs API Backend
    const PROFILE_API_URL = "http://localhost:8081/api/users/profile";
    const UPLOAD_API_URL = "http://localhost:8081/api/users/profile/upload";

    // --- 1. KIỂM TRA XÁC THỰC KHI TẢI TRANG ---
    if (!token) {
        if (typeof logout === 'function') {
            logout(); // Gọi hàm logout từ auth-utils.js
        } else {
            // Dự phòng nếu auth-utils chưa tải kịp
            localStorage.removeItem('jwtToken');
            localStorage.removeItem('userName');
            localStorage.removeItem('userRole');
            window.location.href = '/login';
        }
        return;
    }

    // --- HÀM MỚI: Dùng để hiển thị ảnh đã tải lên ---
    function displayUploadedImage(elementId, url) {
        // elementId là ID của input file (ví dụ: 'file-cmnd-front')
        const fileInput = document.getElementById(elementId);
        if (!fileInput) return;

        // Tìm đến .drop-zone (là cha của input, hoặc container)
        const dropZone = fileInput.closest('.drop-zone');
        if (!dropZone) return;

        if (url) {
            // Nếu có URL ảnh, xóa nội dung cũ và chèn ảnh
            // Giả định URL trả về từ server là /uploads/filename.jpg
            // và server có cấu hình để phục vụ file tĩnh từ /uploads
            dropZone.innerHTML = `<img src="${url}" alt="Ảnh đã tải lên">`;
            // Thêm class để CSS biết là đã có ảnh
            dropZone.classList.add('has-image');
        }
        // Nếu không có URL, giữ nguyên nội dung HTML gốc (Kéo thả...)
    }


    /**
     * TẢI DỮ LIỆU: (Đã nâng cấp)
     */
    async function loadUserProfile() {
        try {
            const response = await fetch(PROFILE_API_URL, {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                const user = await response.json();

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

                // 2. Hiển thị tên
                const userNameDisplay = document.getElementById('userNameDisplay');
                if(userNameDisplay) userNameDisplay.textContent = user.fullName || user.email;

                // 3. NÂNG CẤP: Hiển thị ảnh đã tải lên
                displayUploadedImage('file-cmnd-front', user.idCardFrontUrl);
                displayUploadedImage('file-cmnd-back', user.idCardBackUrl);
                displayUploadedImage('file-license', user.licenseImageUrl);
                displayUploadedImage('file-portrait', user.portraitImageUrl);

            } else if (response.status === 401 && typeof logout === 'function') {
                 logout();
            }
        } catch (error) {
            console.error("Lỗi tải hồ sơ:", error);
            statusMessage.classList.add('error');
            statusMessage.textContent = 'Không thể tải hồ sơ. Vui lòng thử lại.';
            statusMessage.style.display = 'block';
        }
    }

    // Gọi hàm tải dữ liệu
    loadUserProfile();

    /**
     * HÀM UPLOAD THỰC TẾ
     */
    async function uploadImage(fileId) {
        const fileInput = document.getElementById(fileId);
        if (fileInput.files.length === 0) {
            // Nếu không chọn file mới, trả về null (Backend sẽ không cập nhật)
            return null;
        }

        const file = fileInput.files[0];
        const formData = new FormData();
        formData.append("file", file);

        try {
            const response = await fetch(UPLOAD_API_URL, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${token}` },
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
        const phoneRegex = /^0[0-9]{9}$/; // 10 số, bắt đầu bằng 0
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

            // 1. Xử lý tải ảnh (Thực tế)
            // (Lưu ý: Nếu người dùng không chọn file mới, hàm uploadImage sẽ trả về null)
            const [idCardFrontUrl, idCardBackUrl, licenseImageUrl, portraitImageUrl] = await Promise.all([
                uploadImage('file-cmnd-front'),
                uploadImage('file-cmnd-back'),
                uploadImage('file-license'),
                uploadImage('file-portrait')
            ]);

            // 2. Chuẩn bị dữ liệu cập nhật
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

                // Gửi URL mới (nếu có) hoặc null (nếu không chọn file mới)
                idCardFrontUrl: idCardFrontUrl,
                idCardBackUrl: idCardBackUrl,
                licenseImageUrl: licenseImageUrl,
                portraitImageUrl: portraitImageUrl,
            };

            // Lọc ra các giá trị null (để Backend không ghi đè null vào ảnh cũ)
            const filteredUpdateData = Object.fromEntries(
                Object.entries(updateData).filter(([_, v]) => v != null)
            );

            // 3. Gọi API Backend (PUT request)
            const response = await fetch(PROFILE_API_URL, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(filteredUpdateData) // Chỉ gửi các trường có giá trị
            });

            const result = await response.json();

            if (response.ok) {
                statusMessage.classList.add('success');
                statusMessage.textContent = 'Đăng ký thông tin thành công! Đang chuyển hướng...';
                statusMessage.style.display = 'block';
                localStorage.setItem('userName', updateData.fullName);

                setTimeout(() => {
                    window.location.href = '/user/profile-status';
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