package com.example.ui_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // <-- Import
import org.springframework.security.crypto.password.PasswordEncoder; // <-- Import
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // 1. THÊM "CÔNG THỨC" PasswordEncoder VÀO ĐÂY
    // (Di chuyển từ SecurityConfig sang)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}