package com.example.user_account_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    // 1. DI CHUYỂN "CÔNG THỨC" TẠO PasswordEncoder SANG ĐÂY
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // (Bạn cũng có thể đặt @Bean RestTemplate ở đây nếu sau này cần)
}