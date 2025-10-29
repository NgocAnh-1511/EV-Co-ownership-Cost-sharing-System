package com.example.ui_service.service;

import com.example.ui_service.dto.RegistrationDto;
import com.example.ui_service.model.User;
import com.example.ui_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List; // Import List
import java.util.Optional; // Import Optional

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Xử lý đăng ký người dùng mới.
     * Mật khẩu sẽ được mã hóa và vai trò mặc định là ROLE_USER.
     * @param dto Dữ liệu đăng ký từ form.
     * @throws Exception Nếu mật khẩu không khớp hoặc email đã tồn tại.
     */
    @Override
    public void register(RegistrationDto dto) throws Exception {
        // 1. Kiểm tra mật khẩu xác nhận
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new Exception("Mật khẩu xác nhận không khớp!");
        }

        // 2. Kiểm tra email đã được sử dụng chưa
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new Exception("Địa chỉ email này đã được sử dụng!");
        }

        // 3. Tạo đối tượng User mới
        User newUser = new User();
        newUser.setFullName(dto.getUsername()); // Lấy "Tên đăng nhập" từ form gán vào fullName
        newUser.setEmail(dto.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(dto.getPassword())); // Mã hóa mật khẩu
        newUser.setVerified(false); // Mặc định chưa xác thực
        newUser.setRole("ROLE_USER"); // Gán vai trò mặc định là USER

        // 4. Lưu người dùng mới vào cơ sở dữ liệu
        userRepository.save(newUser);
    }

    /**
     * Lấy danh sách tất cả người dùng trong hệ thống.
     * @return Danh sách các đối tượng User.
     */
    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll(); // Sử dụng phương thức có sẵn của JpaRepository
    }

    /**
     * Nâng cấp vai trò của một người dùng thành ROLE_ADMIN dựa trên ID.
     * @param userId ID của người dùng cần nâng cấp.
     * @throws Exception Nếu không tìm thấy người dùng với ID cung cấp.
     */
    @Override
    public void promoteUserToAdmin(Long userId) throws Exception {
        // Tìm người dùng trong CSDL bằng ID
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Kiểm tra xem họ đã là Admin chưa để tránh cập nhật không cần thiết
            if (!"ROLE_ADMIN".equals(user.getRole())) {
                user.setRole("ROLE_ADMIN"); // Đặt vai trò mới là ADMIN
                userRepository.save(user); // Lưu lại thay đổi vào CSDL
            }
            // Nếu đã là Admin thì không làm gì cả
        } else {
            // Nếu không tìm thấy người dùng, báo lỗi
            throw new Exception("Không tìm thấy người dùng với ID: " + userId);
        }
    }
}