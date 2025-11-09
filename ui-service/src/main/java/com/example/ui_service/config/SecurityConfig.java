package com.example.ui_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Cho phép mọi request truy cập
                )
                .csrf(csrf -> csrf.disable()) // Tắt CSRF (cho form đơn giản)
                .formLogin(login -> login.disable()) // Tắt login mặc định
                .httpBasic(basic -> basic.disable()) // Tắt HTTP Basic authentication
                .logout(logout -> logout.disable()) // Tắt logout mặc định
                .sessionManagement(session -> session.disable()); // Tắt session management

        return http.build();
    }
}
