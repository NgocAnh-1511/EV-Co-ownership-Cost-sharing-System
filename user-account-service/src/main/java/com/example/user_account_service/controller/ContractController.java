package com.example.user_account_service.controller; // <-- Đổi package

import com.example.user_account_service.dto.ContractCreationDto;
import com.example.user_account_service.dto.ContractListViewDto;
import com.example.user_account_service.dto.ContractSummaryDto;
import com.example.user_account_service.model.Contract; // <-- Import model Contract
import com.example.user_account_service.repository.UserRepository;
import com.example.user_account_service.service.ContractService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus; // <-- Import mới
import org.springframework.http.ResponseEntity; // <-- Import mới
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult; // <-- Import mới
import org.springframework.web.bind.annotation.*;

// 1. Nâng cấp lên @RestController
@RestController
// 2. Thêm tiền tố API chung
@RequestMapping("/api/contracts")
public class ContractController {

    private static final Logger logger = LoggerFactory.getLogger(ContractController.class);

    @Autowired private ContractService contractService;
    @Autowired private UserRepository userRepository;

    // 3. XÓA BỎ các hàm GET trả về HTML (ví dụ: showCreateContractForm)

    /**
     * API Lấy TÓM TẮT hợp đồng (cho card summary)
     */
    @GetMapping("/my-summary")
    public ResponseEntity<?> getMyContractSummary() {
        try {
            Long currentUserId = getCurrentUserId();
            ContractSummaryDto summary = contractService.getContractSummaryForUser(currentUserId);
            return ResponseEntity.ok(summary);
        } catch (IllegalStateException | UsernameNotFoundException e) {
            // 4. Trả về 401 Unauthorized (Chưa đăng nhập)
            return new ResponseEntity<>("Vui lòng đăng nhập để xem tóm tắt.", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * API Lấy DANH SÁCH hợp đồng (đã phân trang)
     */
    @GetMapping("/my-list")
    public ResponseEntity<?> getMyContractList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Long currentUserId = getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size);
            Page<ContractListViewDto> contractPage = contractService.getContractsForUser(currentUserId, pageable);
            return ResponseEntity.ok(contractPage); // <-- Trả về Page JSON
        } catch (IllegalStateException | UsernameNotFoundException e) {
            return new ResponseEntity<>("Vui lòng đăng nhập để xem danh sách.", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * API Tạo một hợp đồng mới
     */
    @PostMapping
    public ResponseEntity<?> processCreateContract(
            @Valid @RequestBody ContractCreationDto contractDto, // <-- 5. Đổi sang @RequestBody (nhận JSON)
            BindingResult bindingResult) { // <-- Bỏ RedirectAttributes, Model

        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }

        try {
            Long currentUserId = getCurrentUserId();
            // (Giả sử bạn sửa service để trả về Contract đã tạo)
            Contract newContract = contractService.createContract(contractDto, currentUserId);

            // 6. Trả về 201 CREATED (Tạo thành công) với đối tượng vừa tạo
            return new ResponseEntity<>(newContract, HttpStatus.CREATED);
        } catch (IllegalStateException | UsernameNotFoundException e) {
            logger.error("Error creating contract: User not valid.", e);
            return new ResponseEntity<>("Lỗi: Người dùng không hợp lệ.", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            logger.error("Lỗi khi tạo hợp đồng: ", e);
            return new ResponseEntity<>("Đã xảy ra lỗi khi tạo hợp đồng: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // (Giữ nguyên helper method)
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("Người dùng chưa đăng nhập");
        }
        String userEmail = authentication.getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với email: " + userEmail))
                .getUserId();
    }
}