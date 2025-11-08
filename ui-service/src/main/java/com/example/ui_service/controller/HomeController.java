package com.example.ui_service.controller;

import com.example.ui_service.model.ServiceDTO;
import com.example.ui_service.model.VehicleDTO;
import com.example.ui_service.model.VehiclegroupDTO;
import com.example.ui_service.service.VehicleGroupRestClient;
import com.example.ui_service.service.VehicleRestClient;
import com.example.ui_service.service.ServiceRestClient;
import com.example.ui_service.service.VehicleServiceRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private VehicleRestClient vehicleRestClient;

    @Autowired
    private ServiceRestClient serviceRestClient;

    @Autowired
    private VehicleServiceRestClient vehicleServiceRestClient;

    /**
     * Trang quản lý dịch vụ xe - GET
     * Hiển thị danh sách xe với các chức năng:
     * - Tìm kiếm xe
     * - Lọc theo trạng thái
     * - Phân trang
     * - Thống kê (tổng số xe, sẵn sàng, bảo dưỡng, sửa chữa)
     */
    @GetMapping("/admin/vehicle-manager")
    public String vehicleManager(
            Model model,
            @RequestParam(value = "searchQuery", required = false, defaultValue = "") String searchQuery,
            @RequestParam(value = "statusFilter", required = false, defaultValue = "Tất cả trạng thái") String statusFilter,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        
        try {
        model.addAttribute("pageTitle", "Quản Lý Các Dịch Vụ Xe");
        model.addAttribute("pageDescription", "Quản Lý Danh Sách xe theo trạng thái");
            
            // Lấy danh sách tất cả xe từ API
            List<VehicleDTO> allVehicles = vehicleRestClient.getAllVehicles();
            System.out.println("✅ Đã lấy " + allVehicles.size() + " xe từ API");
            
            // Lọc xe theo search query và status filter
            List<VehicleDTO> filteredVehicles = allVehicles.stream()
                    .filter(vehicle -> {
                        // Lọc theo search query (tìm trong vehicleNumber, vehicleType, vehicleId)
                        boolean matchesSearch = searchQuery.isEmpty() ||
                                (vehicle.getVehicleNumber() != null && vehicle.getVehicleNumber().toLowerCase().contains(searchQuery.toLowerCase())) ||
                                (vehicle.getType() != null && vehicle.getType().toLowerCase().contains(searchQuery.toLowerCase())) ||
                                (vehicle.getVehicleId() != null && vehicle.getVehicleId().toLowerCase().contains(searchQuery.toLowerCase()));
                        
                        // Lọc theo status
                        boolean matchesStatus = "Tất cả trạng thái".equals(statusFilter) ||
                                matchesStatusFilter(vehicle.getStatus(), statusFilter);
                        
                        return matchesSearch && matchesStatus;
                    })
                    .collect(Collectors.toList());
            
            // Tính toán thống kê
            long totalVehicles = allVehicles.size();
            long availableVehicles = allVehicles.stream()
                    .filter(v -> isAvailableStatus(v.getStatus()))
                    .count();
            long maintenanceVehicles = allVehicles.stream()
                    .filter(v -> isMaintenanceStatus(v.getStatus()))
                    .count();
            long brokenVehicles = allVehicles.stream()
                    .filter(v -> isBrokenStatus(v.getStatus()))
                    .count();
            
            // Phân trang
            int totalPages = (int) Math.ceil((double) filteredVehicles.size() / size);
            int startIndex = (page - 1) * size + 1;
            int endIndex = Math.min(page * size, filteredVehicles.size());
            
            // Lấy danh sách xe cho trang hiện tại
            List<VehicleDTO> pagedVehicles = filteredVehicles.stream()
                    .skip((page - 1) * size)
                    .limit(size)
                    .collect(Collectors.toList());
            
            // Map VehicleDTO sang format cho view
            List<Map<String, Object>> vehicleViewData = pagedVehicles.stream()
                    .map(this::mapVehicleToViewData)
                    .collect(Collectors.toList());
            
            // Thêm dữ liệu vào model
            model.addAttribute("vehicles", vehicleViewData);
            model.addAttribute("totalVehicles", totalVehicles);
            model.addAttribute("availableVehicles", availableVehicles);
            model.addAttribute("maintenanceVehicles", maintenanceVehicles);
            model.addAttribute("brokenVehicles", brokenVehicles);
            model.addAttribute("searchQuery", searchQuery);
            model.addAttribute("statusFilter", statusFilter);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("startIndex", startIndex);
            model.addAttribute("endIndex", endIndex);
            
            System.out.println("✅ Đã xử lý " + filteredVehicles.size() + " xe sau khi lọc");
            System.out.println("   - Trang " + page + "/" + totalPages);
            System.out.println("   - Hiển thị " + startIndex + "-" + endIndex + " trong tổng số " + filteredVehicles.size() + " xe");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi load dữ liệu cho trang quản lý xe: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("vehicles", List.of());
            model.addAttribute("totalVehicles", 0);
            model.addAttribute("availableVehicles", 0);
            model.addAttribute("maintenanceVehicles", 0);
            model.addAttribute("brokenVehicles", 0);
            model.addAttribute("errorMessage", "Không thể tải dữ liệu từ database. Vui lòng thử lại sau.");
        }
        
        return "admin/vehicle-manager";
    }
    
    /**
     * Kiểm tra status có khớp với filter không
     */
    private boolean matchesStatusFilter(String vehicleStatus, String statusFilter) {
        if (vehicleStatus == null) return false;
        
        String statusLower = vehicleStatus.toLowerCase();
        String filterLower = statusFilter.toLowerCase();
        
        if ("Sẵn sàng".equals(statusFilter) || "available".equals(filterLower)) {
            return isAvailableStatus(statusLower);
        } else if ("Bảo dưỡng".equals(statusFilter) || "maintenance".equals(filterLower)) {
            return isMaintenanceStatus(statusLower);
        } else if ("Sửa chữa".equals(statusFilter) || "broken".equals(filterLower) || "repair".equals(filterLower)) {
            return isBrokenStatus(statusLower);
        }
        
        return false;
    }
    
    /**
     * Kiểm tra status là "Sẵn sàng"
     */
    private boolean isAvailableStatus(String status) {
        if (status == null) return false;
        String s = status.toLowerCase();
        return s.contains("available") || s.contains("sẵn sàng") || s.contains("ready");
    }
    
    /**
     * Kiểm tra status là "Bảo dưỡng"
     */
    private boolean isMaintenanceStatus(String status) {
        if (status == null) return false;
        String s = status.toLowerCase();
        return s.contains("maintenance") || s.contains("bảo dưỡng") || s.contains("servicing");
    }
    
    /**
     * Kiểm tra status là "Sửa chữa"
     */
    private boolean isBrokenStatus(String status) {
        if (status == null) return false;
        String s = status.toLowerCase();
        return s.contains("broken") || s.contains("sửa chữa") || s.contains("repair") || 
               s.contains("in repair") || s.contains("damaged") || s.contains("hư hỏng");
    }
    
    /**
     * Map VehicleDTO sang format cho view
     */
    private Map<String, Object> mapVehicleToViewData(VehicleDTO vehicle) {
        Map<String, Object> viewData = new HashMap<>();
        
        // Tên xe (sử dụng vehicleType nếu có, nếu không dùng vehicleId)
        String name = vehicle.getType() != null && !vehicle.getType().isEmpty() 
                ? vehicle.getType() 
                : (vehicle.getVehicleId() != null ? vehicle.getVehicleId() : "Không có tên");
        viewData.put("name", name);
        
        // Biển số
        viewData.put("plateNumber", vehicle.getVehicleNumber() != null ? vehicle.getVehicleNumber() : "N/A");
        
        // Loại xe (category)
        viewData.put("category", vehicle.getType() != null ? vehicle.getType() : "N/A");
        
        // Trạng thái (tiếng Việt)
        String statusVi = mapStatusToVietnamese(vehicle.getStatus());
        viewData.put("status", statusVi);
        
        // Status class cho CSS
        String statusClass = mapStatusToClass(vehicle.getStatus());
        viewData.put("statusClass", statusClass);
        
        // Icon class
        viewData.put("iconClass", "icon-car");
        
        // Type detail
        String typeDetail = vehicle.getType() != null ? vehicle.getType() : "";
        viewData.put("typeDetail", typeDetail);
        
        // Ngày cập nhật (tạm thời dùng "N/A" vì không có trong DTO)
        viewData.put("updateDate", "N/A");
        
        return viewData;
    }
    
    /**
     * Map status sang tiếng Việt
     */
    private String mapStatusToVietnamese(String status) {
        if (status == null) return "Không xác định";
        
        String s = status.toLowerCase();
        if (isAvailableStatus(s)) {
            return "Sẵn sàng";
        } else if (isMaintenanceStatus(s)) {
            return "Bảo dưỡng";
        } else if (isBrokenStatus(s)) {
            return "Sửa chữa";
        } else if (s.contains("in service") || s.contains("đang sử dụng")) {
            return "Đang sử dụng";
        }
        
        return status; // Trả về status gốc nếu không match
    }
    
    /**
     * Map status sang CSS class
     */
    private String mapStatusToClass(String status) {
        if (status == null) return "unknown";
        
        String s = status.toLowerCase();
        if (isAvailableStatus(s)) {
            return "available";
        } else if (isMaintenanceStatus(s)) {
            return "maintenance";
        } else if (isBrokenStatus(s)) {
            return "broken";
        } else if (s.contains("in service") || s.contains("đang sử dụng")) {
            return "in-service";
        }
        
        return "unknown";
    }

    
    @GetMapping("/admin/enhanced-contract")
    public String EnhancedContractManagement(Model model) {
        model.addAttribute("pageTitle", "Quản Lý Hợp Đồng Điện Tử");
        model.addAttribute("pageDescription", "Quản lý hợp đồng pháp lý cho nhóm đồng sở hữu");
        return "admin/enhanced-contract-management";
    }

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
            // Gọi API: GET http://localhost:8083/api/vehicles
            List<VehicleDTO> vehicles = vehicleRestClient.getAllVehicles();
            model.addAttribute("vehicles", vehicles);
            System.out.println("✅ Đã load " + vehicles.size() + " xe từ bảng vehicle");
            
            // Load danh sách loại dịch vụ từ cột service_type trong bảng service
            // Gọi API: GET http://localhost:8083/api/services/types
            List<String> serviceTypes = serviceRestClient.getServiceTypes();
            model.addAttribute("serviceTypes", serviceTypes);
            System.out.println("✅ Đã load " + serviceTypes.size() + " loại dịch vụ từ cột service_type");
            
            // Load danh sách dịch vụ từ bảng service trong database
            // Gọi API: GET http://localhost:8083/api/services
            List<ServiceDTO> services = serviceRestClient.getAllServices();
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
