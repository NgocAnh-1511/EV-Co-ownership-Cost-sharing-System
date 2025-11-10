package com.example.ui_service.controller;

import com.example.ui_service.model.ServiceDTO;
import com.example.ui_service.model.VehicleDTO;
import com.example.ui_service.service.ServiceRestClient;
import com.example.ui_service.service.VehicleRestClient;
import com.example.ui_service.service.VehicleServiceRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ServiceRegistrationController {

    @Autowired
    private VehicleRestClient vehicleRestClient;

    @Autowired
    private ServiceRestClient serviceRestClient;

    @Autowired
    private VehicleServiceRestClient vehicleServiceRestClient;

    /**
     * Trang đăng ký dịch vụ xe cho khách hàng - GET
     * Load dữ liệu từ database: 
     * - Danh sách xe từ bảng vehicle
     * - Danh sách loại dịch vụ từ cột service_type trong bảng service
     * - Danh sách dịch vụ từ cột service_name trong bảng service
     */
    @GetMapping("/user/service-registration")
    public String serviceRegistration(Model model) {
        try {
            model.addAttribute("pageTitle", "Đăng Ký Dịch Vụ Xe");
            model.addAttribute("pageDescription", "Đăng ký dịch vụ bảo dưỡng, sửa chữa và các dịch vụ khác");
            
            // Load danh sách xe từ bảng vehicle trong database
            System.out.println("📡 [ServiceRegistrationController] Bắt đầu load danh sách xe...");
            List<VehicleDTO> vehicles = vehicleRestClient.getAllVehicles();
            
            if (vehicles == null) {
                System.err.println("❌ [ServiceRegistrationController] vehicles list là null");
                vehicles = new ArrayList<>();
            }
            
            model.addAttribute("vehicles", vehicles);
            System.out.println("✅ [ServiceRegistrationController] Đã load " + vehicles.size() + " xe từ bảng vehicle");
            
            // Log chi tiết để debug
            if (vehicles.isEmpty()) {
                System.err.println("⚠️ [ServiceRegistrationController] Danh sách xe rỗng! Có thể:");
                System.err.println("   1. Backend service không chạy");
                System.err.println("   2. Database không có dữ liệu");
                System.err.println("   3. API không trả về dữ liệu");
            } else {
                System.out.println("   - Xe đầu tiên: " + vehicles.get(0).getVehicleId() + " - " + vehicles.get(0).getVehicleNumber());
            }
            
            // Load danh sách loại dịch vụ từ cột service_type trong bảng service
            List<String> serviceTypes = serviceRestClient.getServiceTypes();
            model.addAttribute("serviceTypes", serviceTypes);
            System.out.println("✅ Đã load " + serviceTypes.size() + " loại dịch vụ từ cột service_type");
            
            // Load danh sách dịch vụ từ bảng service trong database
            List<ServiceDTO> services = serviceRestClient.getAllServicesAsDTO();
            model.addAttribute("services", services);
            System.out.println("✅ Đã load " + services.size() + " dịch vụ từ cột service_name");
            
            // Log chi tiết để debug
            if (vehicles.size() > 0) {
                System.out.println("Xe đầu tiên: " + vehicles.get(0).getVehicleId() + " - " + vehicles.get(0).getVehicleNumber());
            }
            if (serviceTypes.size() > 0) {
                System.out.println("Loại dịch vụ: " + serviceTypes);
            }
            if (services.size() > 0) {
                System.out.println("Dịch vụ đầu tiên: " + services.get(0).getServiceId() + " - " + services.get(0).getServiceName() + " (" + services.get(0).getServiceType() + ")");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi load dữ liệu cho trang đăng ký dịch vụ: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("vehicles", List.of());
            model.addAttribute("serviceTypes", List.of());
            model.addAttribute("services", List.of());
            model.addAttribute("errorMessage", "Không thể tải dữ liệu từ database. Vui lòng thử lại sau.");
        }
        return "user/service-registration";
    }

    /**
     * Xử lý đăng ký dịch vụ - POST
     */
    @PostMapping("/user/service-registration")
    public String registerService(
            @RequestParam String vehicleId,
            @RequestParam String serviceId,
            @RequestParam String serviceType,
            @RequestParam(required = false) String serviceDescription,
            RedirectAttributes redirectAttributes) {
        try {
            // Tạo request data
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("vehicleId", vehicleId);
            requestData.put("serviceId", serviceId);
            requestData.put("serviceType", serviceType);
            if (serviceDescription != null && !serviceDescription.trim().isEmpty()) {
                requestData.put("serviceDescription", serviceDescription);
            }
            requestData.put("status", "pending");

            // Gọi API để đăng ký dịch vụ
            Map<String, Object> result = vehicleServiceRestClient.registerVehicleService(requestData);
            
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký dịch vụ thành công! Chúng tôi sẽ xử lý yêu cầu của bạn sớm nhất.");
            return "redirect:/user/service-registration?success=true";
        } catch (Exception e) {
            System.err.println("Lỗi khi đăng ký dịch vụ: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi đăng ký dịch vụ: " + e.getMessage());
            return "redirect:/user/service-registration?error=true";
        }
    }
}


