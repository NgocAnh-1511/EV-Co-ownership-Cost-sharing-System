package com.example.user_account_service.service;

import com.example.user_account_service.entity.User;
import com.example.user_account_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class StorageService {

    // QUAN TRỌNG: Đường dẫn này phải trỏ đến thư mục 'uploads'
    // (Thư mục 'uploads' này phải nằm ở thư mục gốc dự án)
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    public StorageService() {
        try {
            // Đảm bảo thư mục 'uploads' tồn tại
            if (!Files.exists(fileStorageLocation)) {
                Files.createDirectories(this.fileStorageLocation);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Không thể tạo thư mục lưu trữ: " + fileStorageLocation, ex);
        }
    }

    /**
     * Lưu file và trả về tên file duy nhất (để lưu vào database)
     */
    public String storeFile(MultipartFile file, Long userId) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || file.isEmpty()) {
            throw new RuntimeException("File tải lên không hợp lệ.");
        }

        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uniqueFileName = userId + "_" + UUID.randomUUID().toString() + fileExtension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return uniqueFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Không thể lưu file " + originalFileName, ex);
        }
    }
}