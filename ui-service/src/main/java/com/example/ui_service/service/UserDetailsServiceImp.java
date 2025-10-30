package com.example.ui_service.service;

import com.example.ui_service.model.User; // <-- Model POJO (đã "làm sạch")
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class UserDetailsServiceImp implements UserDetailsService {

    @Autowired
    private RestTemplate restTemplate; // (Đảm bảo bạn đã @Bean nó trong AppConfig)

    @Value("${backend.api.base-url}") // (Lấy từ application.properties)
    private String apiBaseUrl;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Spring Security gọi 'loadUserByUsername' nhưng chúng ta dùng 'email'

        try {
            // 1. GỌI API BACKEND ĐỂ LẤY USER (dưới dạng POJO)
            String url = apiBaseUrl + "/users/by-email?email=" + email;

            // (Hãy chắc chắn backend (user-account-service)
            // có API "/api/users/by-email" để trả về User)
            User user = restTemplate.getForObject(url, User.class);

            if (user == null) {
                throw new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email);
            }

            // 2. TẠO QUYỀN (ROLE) TỪ DỮ LIỆU POJO
            List<GrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority(user.getRole())
            );

            // 3. TRẢ VỀ SPRING SECURITY USER
            // Nó sẽ tự so sánh passwordHash này với mật khẩu user nhập
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPasswordHash(), // (Cần POJO User có getPasswordHash())
                    authorities
            );

        } catch (HttpClientErrorException.NotFound e) {
            // Nếu API trả về 404 Not Found
            throw new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email, e);
        } catch (Exception e) {
            // Các lỗi khác (như không kết nối được backend)
            throw new RuntimeException("Lỗi khi gọi API xác thực: " + e.getMessage(), e);
        }
    }
}