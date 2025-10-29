package com.example.ui_service.controller;

import com.example.ui_service.dto.ContractCreationDto;
import com.example.ui_service.dto.ContractListViewDto;
import com.example.ui_service.dto.ContractSummaryDto;
import com.example.ui_service.model.Asset;
import com.example.ui_service.model.User;
import com.example.ui_service.repository.AssetRepository;
import com.example.ui_service.repository.UserRepository;
import com.example.ui_service.service.ContractService;
import jakarta.validation.Valid;
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ContractController {

    // Khai báo logger
    private static final Logger logger = LoggerFactory.getLogger(ContractController.class);

    @Autowired private ContractService contractService;
    @Autowired private UserRepository userRepository;
    @Autowired private AssetRepository assetRepository;

    @GetMapping("/hop-dong")
    public String showContractManagement(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Long currentUserId = getCurrentUserId();
            ContractSummaryDto summary = contractService.getContractSummaryForUser(currentUserId);
            model.addAttribute("summary", summary);
            Pageable pageable = PageRequest.of(page, size);
            Page<ContractListViewDto> contractPage = contractService.getContractsForUser(currentUserId, pageable);
            model.addAttribute("contractPage", contractPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", contractPage.getTotalPages());
        } catch (IllegalStateException | UsernameNotFoundException e) {
            logger.warn("User not authenticated or not found while accessing /hop-dong", e); // Log warning
            model.addAttribute("errorMessage", "Vui lòng đăng nhập để xem hợp đồng.");
        }
        return "contract-management";
    }

    @GetMapping("/hop-dong/tao-moi")
    public String showCreateContractForm(Model model) {
        // --- SỬA Ở ĐÂY ---
        List<Asset> assets = assetRepository.findAllByOrderByAssetNameAsc(); // Đổi tên phương thức
        model.addAttribute("assets", assets);
        model.addAttribute("contractDto", new ContractCreationDto());
        return "create-contract";
    }

    @PostMapping("/hop-dong/tao-moi")
    public String processCreateContract(
            @Valid @ModelAttribute("contractDto") ContractCreationDto contractDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            // --- SỬA Ở ĐÂY ---
            List<Asset> assets = assetRepository.findAllByOrderByAssetNameAsc(); // Đổi tên phương thức
            model.addAttribute("assets", assets);
            return "create-contract";
        }

        try {
            Long currentUserId = getCurrentUserId();
            contractService.createContract(contractDto, currentUserId);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo hợp đồng mới thành công!");
            return "redirect:/hop-dong";
        } catch (IllegalStateException | UsernameNotFoundException e) {
            logger.error("Error creating contract: User not valid.", e); // Log lỗi
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Người dùng không hợp lệ.");
            return "redirect:/login";
        } catch (Exception e) {
            logger.error("Lỗi khi tạo hợp đồng: ", e); // Log lỗi chi tiết
            // --- SỬA Ở ĐÂY ---
            List<Asset> assets = assetRepository.findAllByOrderByAssetNameAsc(); // Đổi tên phương thức
            model.addAttribute("assets", assets);
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi tạo hợp đồng: " + e.getMessage());
            return "create-contract";
        }
    }

    // Helper method để lấy User ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) { // Thêm kiểm tra anonymousUser
            throw new IllegalStateException("Người dùng chưa đăng nhập");
        }
        String userEmail = authentication.getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với email: " + userEmail))
                .getUserId();
    }
}