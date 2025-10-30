package com.example.user_account_service.config;

import com.example.user_account_service.service.UserDetailsServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder; // <-- Vẫn import
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImp userDetailsServiceImp;

    // 2. Tiêm PasswordEncoder (được tạo từ AppConfig)
    @Autowired
    private PasswordEncoder passwordEncoder;

    // 3. XÓA BỎ @Bean PasswordEncoder KHỎI ĐÂY
    // @Bean
    // public PasswordEncoder passwordEncoder() { ... } // <-- ĐÃ XÓA

    // 4. Cấu hình AuthenticationManager (Giữ nguyên)
    // Spring sẽ tiêm userDetailsServiceImp và passwordEncoder (từ AppConfig)
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsServiceImp)
                .passwordEncoder(passwordEncoder); // <-- Dùng bean đã được tiêm
    }

    // 3. Cấu hình bảo mật cho API (Giữ nguyên)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/**").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}