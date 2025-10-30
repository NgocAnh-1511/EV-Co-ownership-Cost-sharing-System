package com.example.ui_service.config;

import com.example.ui_service.service.UserDetailsServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder; // <-- Vẫn import
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private AuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Autowired
    private UserDetailsServiceImp userDetailsServiceImp; // (Phiên bản gọi API)

    @Autowired
    private PasswordEncoder passwordEncoder; // (Được tiêm từ AppConfig)

    // Cấu hình AuthenticationManager (Để Spring biết cách tìm user)
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsServiceImp)
                .passwordEncoder(passwordEncoder);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // --- ĐÂY LÀ DÒNG SỬA LỖI ---
                        // Cho phép mọi người truy cập login, register và trang chủ
                        .requestMatchers("/", "/login", "/register").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                        // --- KẾT THÚC DÒNG SỬA LỖI ---

                        // Các trang còn lại yêu cầu đăng nhập
                        .requestMatchers("/hop-dong", "/dang-ky-chu-xe", "/ho-so", "/trang-thai-ho-so").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email") // (Đảm bảo đã sửa thành email)
                        .passwordParameter("password")
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureUrl("/login?error=true")
                        .permitAll() // <-- Dòng này cũng quan trọng
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }
}