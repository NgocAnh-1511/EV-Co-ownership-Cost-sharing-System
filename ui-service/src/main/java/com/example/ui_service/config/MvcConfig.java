package com.example.ui_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    // Lấy đường dẫn thư mục upload từ application.properties
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        String uploadResourcePath = uploadPath.toUri().toString();

        // Map URL path "/uploads/**" đến thư mục vật lý trên ổ đĩa
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadResourcePath); // "file:/E:/path/to/your/project/uploads/"

        // (Tùy chọn) Đảm bảo các resource tĩnh khác vẫn hoạt động
        registry.addResourceHandler("/static/**") // Hoặc /css/**, /js/** tùy cấu trúc
                .addResourceLocations("classpath:/static/");
    }
}