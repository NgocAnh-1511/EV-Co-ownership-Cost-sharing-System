package com.example.ui_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class AuthMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Lấy đường dẫn tuyệt đối đến thư mục 'uploads' ở gốc dự án
        Path uploadDir = Paths.get("uploads").toAbsolutePath();
        String uploadPath = uploadDir.toUri().toString();

        // Cấu hình Resource Handler
        // Khi trình duyệt gọi /uploads/**
        registry.addResourceHandler("/uploads/**")
                // Spring sẽ tìm file trong thư mục uploads/ bên ngoài
                .addResourceLocations(uploadPath);
    }
}

