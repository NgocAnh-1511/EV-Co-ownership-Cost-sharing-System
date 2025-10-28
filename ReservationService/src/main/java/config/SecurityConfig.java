package com.example.reservationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 🔸 Tắt CSRF để cho phép POST từ UI-Service
                .csrf(csrf -> csrf.disable())

                // 🔸 Cấu hình quyền truy cập
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll()  // Cho phép toàn bộ API public
                        .anyRequest().permitAll()                // Cho phép mọi request khác (hoặc có thể đổi thành authenticated() nếu cần)
                )

                // 🔸 Tắt các hình thức login mặc định
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
