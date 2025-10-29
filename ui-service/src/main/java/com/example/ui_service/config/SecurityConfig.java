package com.example.ui_service.config;

import org.springframework.beans.factory.annotation.Autowired; // Đảm bảo có import này
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler; // Đảm bảo có import này

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // --- Inject handler tùy chỉnh ---
    @Autowired
    private AuthenticationSuccessHandler customAuthenticationSuccessHandler;
    // --- Kết thúc inject ---


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // Các rule phân quyền (giữ nguyên như trước)
                        .requestMatchers("/", "/login", "/register").permitAll() // Trang công khai
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() // Tài nguyên tĩnh
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Chỉ Admin vào /admin/*
                        .requestMatchers("/checkin-checkout").hasAnyRole("USER", "ADMIN") // User hoặc Admin
                        .anyRequest().authenticated() // Mọi request khác cần đăng nhập
                )
                // --- CẬP NHẬT FORM LOGIN ĐỂ DÙNG HANDLER ---
                .formLogin(form -> form
                        .loginPage("/login") // Trang đăng nhập
                        .loginProcessingUrl("/login") // URL xử lý đăng nhập
                        .usernameParameter("username") // Tên input username/email
                        .passwordParameter("password") // Tên input password
                        // .defaultSuccessUrl("/checkin-checkout", true) // <-- Dòng này đã bị xóa/comment
                        .successHandler(customAuthenticationSuccessHandler) // <-- Sử dụng handler tùy chỉnh này
                        .failureUrl("/login?error=true") // URL khi đăng nhập thất bại
                        .permitAll() // Cho phép tất cả truy cập trang login
                )
                // --- KẾT THÚC CẬP NHẬT FORM LOGIN ---
                .logout(logout -> logout // Cấu hình đăng xuất (giữ nguyên)
                        .logoutUrl("/logout") // URL xử lý đăng xuất
                        .logoutSuccessUrl("/login?logout=true") // URL sau khi đăng xuất thành công
                        .permitAll() // Cho phép tất cả thực hiện đăng xuất
                );

        return http.build();
    }
}