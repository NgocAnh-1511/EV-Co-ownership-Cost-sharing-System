package com.example.ui_service.dto;

import jakarta.validation.constraints.FutureOrPresent; // Cho ngày bắt đầu
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat; // Để parse ngày tháng

import java.time.LocalDate;

@Data
public class ContractCreationDto {

    @NotEmpty(message = "Tiêu đề không được để trống")
    private String title;

    @NotNull(message = "Vui lòng chọn tài sản")
    private Long assetId; // ID của Asset được chọn

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd") // Định dạng ngày tháng từ input HTML
    @FutureOrPresent(message = "Ngày bắt đầu phải là ngày hiện tại hoặc tương lai")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate; // Ngày kết thúc có thể để trống ban đầu

    // Status sẽ được đặt mặc định trong Service (ví dụ: 'pending')
}