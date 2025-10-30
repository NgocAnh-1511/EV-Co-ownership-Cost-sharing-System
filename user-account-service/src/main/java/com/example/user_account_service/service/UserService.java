package com.example.user_account_service.service;

import com.example.user_account_service.dto.CoOwnerRegistrationDto;
import com.example.user_account_service.dto.RegistrationDto;
import com.example.user_account_service.model.User;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface UserService {

    /**
     * Đăng ký người dùng mới.
     * Trả về User đã được lưu (để sửa lỗi 'incompatible types').
     */
    User register(RegistrationDto registrationDto) throws Exception;

    /**
     * Cập nhật thông tin hồ sơ đồng sở hữu.
     * Trả về User đã được cập nhật (để sửa lỗi 'incompatible types').
     * Đây là phương thức bạn đang thiếu (gây ra lỗi ở).
     */
    User updateCoOwnerInfo(CoOwnerRegistrationDto coOwnerDto,
                           MultipartFile idCardFront,
                           MultipartFile idCardBack,
                           MultipartFile licenseImage,
                           MultipartFile portraitImage) throws Exception;

    /**
     * Lấy thông tin người dùng đang đăng nhập.
     */
    User getCurrentAuthenticatedUser();

    /**
     * Lấy tất cả người dùng (cho Admin).
     */
    List<User> findAllUsers();

    /**
     * Nâng cấp vai trò người dùng thành Admin.
     */
    void promoteUserToAdmin(Long userId) throws Exception;

    /**
     * Tìm các hồ sơ đang chờ duyệt.
     */
    List<User> findUnverifiedUsers();

    /**
     * Duyệt (xác thực) hồ sơ người dùng.
     * Đây là phương thức bạn đã thiếu ở.
     */
    void verifyUser(Long userId) throws Exception;
}