package com.example.ui_service.controller;

import com.example.ui_service.dto.ContractCreationDto;
import com.example.ui_service.model.Asset; // POJO (Đã được "làm sạch")
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
public class ContractController {

    private static final Logger logger = LoggerFactory.getLogger(ContractController.class);

    @Autowired
    private RestTemplate restTemplate; // Công cụ gọi API

    @Value("${backend.api.base-url}")
    private String apiBaseUrl;

    /**
     * Sửa lỗi 404: Xử lý GET /contract-management (hoặc /hop-dong)
     */
    @GetMapping({"/contract-management", "/hop-dong"}) // <-- Sửa lỗi 404
    public String showContractManagement(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            // 1. GỌI API LẤY SUMMARY
            String summaryUrl = apiBaseUrl + "/contracts/my-summary";
            // (Lưu ý: Cần cấu hình RestTemplate để gửi xác thực của user)
            // (Tạm thời bỏ qua try/catch gọi API để đảm bảo trang hiển thị)

            // Chỉ trả về tên file HTML để trang load
            return "contract-management";

        } catch (Exception e) {
            logger.error("Lỗi khi gọi API Hợp đồng: ", e);
            model.addAttribute("errorMessage", "Không thể tải dữ liệu hợp đồng từ máy chủ.");
            return "contract-management";
        }
    }

    @GetMapping("/hop-dong/tao-moi")
    public String showCreateContractForm(Model model) {
        try {
            // GỌI API ĐỂ LẤY DANH SÁCH ASSET
            String assetsUrl = apiBaseUrl + "/assets";
            Asset[] assetsArray = restTemplate.getForObject(assetsUrl, Asset[].class);

            model.addAttribute("assets", Arrays.asList(assetsArray));
            model.addAttribute("contractDto", new ContractCreationDto());
        } catch (Exception e) {
            logger.error("Lỗi khi gọi API lấy tài sản: ", e);
            model.addAttribute("errorMessage", "Không thể tải danh sách tài sản.");
        }
        return "create-contract";
    }

    @PostMapping("/hop-dong/tao-moi")
    public String processCreateContract(
            @ModelAttribute("contractDto") ContractCreationDto contractDto,
            RedirectAttributes redirectAttributes,
            Model model) {

        // (Logic gọi API POST /api/contracts của bạn)

        // Sau khi hoàn thành, chuyển hướng:
        return "redirect:/hop-dong";
    }
}