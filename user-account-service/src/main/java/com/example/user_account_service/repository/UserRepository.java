package com.example.user_account_service.repository;

import com.example.user_account_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByFullName(String fullName);
    List<User> findByIsVerifiedFalse();
    Optional<User> findByIdCardNumber(String idCardNumber);
    boolean existsByIdCardNumberAndUserIdNot(String idCardNumber, Long userId);

    // --- PHƯƠƠNG THỨC MỚI ---
    /**
     * Kiểm tra xem số GPLX có tồn tại cho người dùng khác hay không.
     * @param licenseNumber Số GPLX cần kiểm tra.
     * @param userId ID của người dùng hiện tại (để loại trừ chính họ).
     * @return true nếu số này tồn tại cho người dùng khác, false nếu không.
     */
    boolean existsByLicenseNumberAndUserIdNot(String licenseNumber, Long userId);
    // --- KẾT THÚC ---

    // (Tùy chọn) Thêm existsByPhoneNumberAndUserIdNot nếu cần
}