package com.example.user_account_service.service;

import com.example.user_account_service.dto.CoOwnerRegistrationDto;
import com.example.user_account_service.dto.RegistrationDto;
import com.example.user_account_service.model.User;
import com.example.user_account_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
// (Import thêm dịch vụ lưu file của bạn nếu cần)

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // (Inject thêm dịch vụ lưu file của bạn, ví dụ: FileStorageService)
    // @Autowired
    // private FileStorageService fileStorageService;

    @Override
    public User register(RegistrationDto registrationDto) throws Exception {
        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new Exception("Email đã tồn tại");
        }

        User user = new User();
        user.setEmail(registrationDto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registrationDto.getPassword()));

        // (Lưu ý: Bạn đang thiếu fullName trong DTO đăng ký)
        // user.setFullName(registrationDto.getFullName());

        user.setRole("ROLE_USER");
        user.setVerified(false);

        // Trả về User đã lưu
        return userRepository.save(user);
    }

    // --- PHƯƠNG THỨC BỊ THIẾU CỦA BẠN (GÂY LỖI) ---
    @Override
    public User updateCoOwnerInfo(CoOwnerRegistrationDto coOwnerDto,
                                  MultipartFile idCardFront, MultipartFile idCardBack,
                                  MultipartFile licenseImage, MultipartFile portraitImage) throws Exception {

        User currentUser = getCurrentAuthenticatedUser();

        // (Logic nghiệp vụ của bạn để lưu file vào /uploads/
        // và lấy đường dẫn URL trả về, ví dụ:)
        // String idCardFrontUrl = fileStorageService.store(idCardFront);
        // ...

        // Cập nhật thông tin từ DTO
        currentUser.setFullName(coOwnerDto.getFullName());
        currentUser.setPhoneNumber(coOwnerDto.getPhoneNumber());
        currentUser.setDateOfBirth(coOwnerDto.getDateOfBirth());
        currentUser.setIdCardNumber(coOwnerDto.getIdCardNumber());
        currentUser.setIdCardIssueDate(coOwnerDto.getIdCardIssueDate());
        currentUser.setIdCardIssuePlace(coOwnerDto.getIdCardIssuePlace());
        currentUser.setLicenseNumber(coOwnerDto.getLicenseNumber());
        currentUser.setLicenseClass(coOwnerDto.getLicenseClass());
        currentUser.setLicenseIssueDate(coOwnerDto.getLicenseIssueDate());
        currentUser.setLicenseExpiryDate(coOwnerDto.getLicenseExpiryDate());

        // Cập nhật URL file (ví dụ)
        // currentUser.setIdCardFrontUrl(idCardFrontUrl);
        // ...

        // Hồ sơ cần được duyệt lại
        currentUser.setVerified(false);

        // Trả về đối tượng User sau khi lưu
        return userRepository.save(currentUser);
    }

    @Override
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("Người dùng chưa đăng nhập");
        }
        String userEmail = authentication.getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với email: " + userEmail));
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void promoteUserToAdmin(Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("Không tìm thấy User ID: " + userId));
        user.setRole("ROLE_ADMIN");
        userRepository.save(user);
    }

    @Override
    public List<User> findUnverifiedUsers() {
        return userRepository.findByIsVerifiedFalse();
    }

    // --- PHƯƠNG THỨC BỊ THIẾU (GÂY LỖI) ---
    @Override
    public void verifyUser(Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("Không tìm thấy User ID: " + userId));
        user.setVerified(true);
        userRepository.save(user);
    }
}