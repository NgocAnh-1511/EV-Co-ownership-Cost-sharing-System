package com.example.user_account_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class CoOwnerRegistrationDto {

    // Thông tin cá nhân
    @NotEmpty(message = "Họ và tên không được để trống")
    private String fullName;

    @NotEmpty(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @NotEmpty(message = "Email không được để trống") // Giả sử user đã có email, có thể bỏ nếu lấy từ user đăng nhập
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotNull(message = "Ngày sinh không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd") // Hoặc dd/MM/yyyy tùy input date
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dateOfBirth;

    // Giấy tờ tùy thân
    @NotEmpty(message = "Số CMND/CCCD không được để trống")
    @Size(min = 9, max = 12, message = "Số CMND/CCCD phải từ 9 đến 12 ký tự")
    private String idCardNumber;

    @NotNull(message = "Ngày cấp CMND/CCCD không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @PastOrPresent(message = "Ngày cấp phải là ngày hiện tại hoặc quá khứ")
    private LocalDate idCardIssueDate;

    @NotEmpty(message = "Nơi cấp không được để trống")
    private String idCardIssuePlace;

    // Giấy phép lái xe
    @NotEmpty(message = "Số GPLX không được để trống")
    private String licenseNumber;

    @NotEmpty(message = "Vui lòng chọn hạng GPLX")
    private String licenseClass; // B1, B2, C, etc.

    @NotNull(message = "Ngày cấp GPLX không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @PastOrPresent(message = "Ngày cấp phải là ngày hiện tại hoặc quá khứ")
    private LocalDate licenseIssueDate;

    @NotNull(message = "Ngày hết hạn GPLX không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "Ngày hết hạn phải là ngày hiện tại hoặc tương lai") // Hoặc @Future tùy yêu cầu
    private LocalDate licenseExpiryDate;

    // Files (Tạm thời comment, sẽ xử lý sau)
    // private MultipartFile idCardFront;
    // private MultipartFile idCardBack;
    // private MultipartFile licenseImage;
    // private MultipartFile portraitImage;

    @AssertTrue(message = "Bạn phải đồng ý với các điều khoản")
    private boolean agreeToTerms;
}