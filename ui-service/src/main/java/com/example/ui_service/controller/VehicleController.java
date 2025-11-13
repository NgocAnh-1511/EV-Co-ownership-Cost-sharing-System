package com.example.ui_service.controller;

import com.example.ui_service.model.VehicleDTO;
import com.example.ui_service.model.VehiclegroupDTO;
import com.example.ui_service.service.VehicleGroupRestClient;
import com.example.ui_service.service.VehicleRestClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/vehicle-management")
public class VehicleController {

    private final VehicleRestClient vehicleRestClient;
    private final VehicleGroupRestClient vehicleGroupRestClient;

    public VehicleController(VehicleRestClient vehicleRestClient, VehicleGroupRestClient vehicleGroupRestClient) {
        this.vehicleRestClient = vehicleRestClient;
        this.vehicleGroupRestClient = vehicleGroupRestClient;
    }

    /**
     * Hiển thị trang quản lý xe với danh sách xe, tìm kiếm, lọc và phân trang
     */
    @GetMapping
    public String vehicleManagementPage(
            Model model,
            @RequestParam(value = "searchQuery", required = false, defaultValue = "") String searchQuery,
            @RequestParam(value = "statusFilter", required = false, defaultValue = "all") String statusFilter,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {

        try {
            // Lấy danh sách tất cả xe từ API
            List<VehicleDTO> allVehicles = vehicleRestClient.getAllVehicles();

            // Lọc xe theo tên/biển số (searchQuery) và trạng thái (statusFilter)
            List<VehicleDTO> filteredVehicles = allVehicles.stream()
                    .filter(vehicle -> {
                        // Lọc theo searchQuery (tên xe, biển số, mã xe)
                        boolean matchesSearch = searchQuery.isEmpty() ||
                                (vehicle.getName() != null && vehicle.getName().toLowerCase().contains(searchQuery.toLowerCase())) ||
                                (vehicle.getVehicleNumber() != null && vehicle.getVehicleNumber().toLowerCase().contains(searchQuery.toLowerCase())) ||
                                (vehicle.getVehicleId() != null && vehicle.getVehicleId().toLowerCase().contains(searchQuery.toLowerCase()));

                        // Lọc theo statusFilter
                        boolean matchesStatus = "all".equals(statusFilter) ||
                                (vehicle.getStatus() != null && vehicle.getStatus().equalsIgnoreCase(statusFilter));

                        return matchesSearch && matchesStatus;
                    })
                    .collect(Collectors.toList());

            // Tính toán thống kê
            long totalVehicles = allVehicles.size();
            long readyVehicles = allVehicles.stream()
                    .filter(v -> "ready".equalsIgnoreCase(v.getStatus()))
                    .count();
            long inUseVehicles = allVehicles.stream()
                    .filter(v -> "in_use".equalsIgnoreCase(v.getStatus()))
                    .count();
            long maintenanceVehicles = allVehicles.stream()
                    .filter(v -> "maintenance".equalsIgnoreCase(v.getStatus()))
                    .count();
            long repairVehicles = allVehicles.stream()
                    .filter(v -> "repair".equalsIgnoreCase(v.getStatus()))
                    .count();
            long checkingVehicles = allVehicles.stream()
                    .filter(v -> "checking".equalsIgnoreCase(v.getStatus()))
                    .count();

            // Phân trang
            int totalPages = filteredVehicles.isEmpty() ? 1 : (int) Math.ceil((double) filteredVehicles.size() / size);
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, filteredVehicles.size());
            List<VehicleDTO> pagedVehicles = filteredVehicles.isEmpty() ?
                    filteredVehicles : filteredVehicles.subList(startIndex, endIndex);

            // Format dữ liệu xe cho hiển thị
            List<Map<String, Object>> vehiclesForDisplay = pagedVehicles.stream()
                    .map(this::formatVehicleForDisplay)
                    .collect(Collectors.toList());

            // Lấy danh sách nhóm xe chưa có xe (để hiển thị trong form thêm/sửa)
            List<VehiclegroupDTO> availableGroups = vehicleGroupRestClient.getAvailableVehicleGroups(null);

            // Thêm dữ liệu vào model
            model.addAttribute("vehicles", vehiclesForDisplay);
            model.addAttribute("vehicleGroups", availableGroups);
            model.addAttribute("totalVehicles", totalVehicles);
            model.addAttribute("readyVehicles", readyVehicles);
            model.addAttribute("inUseVehicles", inUseVehicles);
            model.addAttribute("maintenanceVehicles", maintenanceVehicles);
            model.addAttribute("repairVehicles", repairVehicles);
            model.addAttribute("checkingVehicles", checkingVehicles);
            model.addAttribute("statusFilter", statusFilter);
            model.addAttribute("searchQuery", searchQuery);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalFiltered", filteredVehicles.size());
            model.addAttribute("startIndex", filteredVehicles.isEmpty() ? 0 : startIndex + 1);
            model.addAttribute("endIndex", endIndex);
            model.addAttribute("pageTitle", "Quản Lý Xe");
            model.addAttribute("pageDescription", "Quản lý thông tin xe trong hệ thống");

            return "admin/vehicle-management";

        } catch (Exception e) {
            System.err.println("Lỗi khi load trang quản lý xe: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi tải dữ liệu: " + e.getMessage());
            model.addAttribute("vehicles", Collections.emptyList());
            model.addAttribute("vehicleGroups", Collections.emptyList());
            model.addAttribute("totalVehicles", 0);
            model.addAttribute("readyVehicles", 0);
            model.addAttribute("inUseVehicles", 0);
            model.addAttribute("maintenanceVehicles", 0);
            model.addAttribute("repairVehicles", 0);
            model.addAttribute("checkingVehicles", 0);
            return "admin/vehicle-management";
        }
    }

    /**
     * API endpoint để lấy thông tin chi tiết xe (cho form sửa)
     */
    @GetMapping("/api/{vehicleId}")
    @ResponseBody
    public Map<String, Object> getVehicleDetails(@PathVariable String vehicleId) {
        Map<String, Object> response = new HashMap<>();
        try {
            VehicleDTO vehicle = vehicleRestClient.getVehicleById(vehicleId);
            if (vehicle == null) {
                response.put("error", "Không tìm thấy xe với ID: " + vehicleId);
                return response;
            }

            // Lấy danh sách nhóm xe available (bao gồm nhóm hiện tại của xe)
            String currentGroupId = vehicle.getGroupId();
            List<VehiclegroupDTO> availableGroups = vehicleGroupRestClient.getAvailableVehicleGroups(currentGroupId);

            response.put("vehicle", vehicle);
            response.put("availableGroups", availableGroups);
            return response;

        } catch (Exception e) {
            System.err.println("Lỗi khi lấy chi tiết xe: " + e.getMessage());
            e.printStackTrace();
            response.put("error", "Đã xảy ra lỗi khi lấy thông tin xe: " + e.getMessage());
            return response;
        }
    }

    /**
     * Thêm xe mới
     */
    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addVehicle(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Gọi API để thêm xe
            Object result = vehicleRestClient.addVehicle(requestData);
            
            if (result != null) {
                response.put("success", true);
                response.put("message", "Thêm xe thành công!");
                response.put("data", result);
            } else {
                response.put("success", false);
                response.put("message", "Không thể thêm xe. Vui lòng thử lại.");
            }

        } catch (RuntimeException e) {
            System.err.println("Lỗi khi thêm xe: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi khi thêm xe.");
        } catch (Exception e) {
            System.err.println("Lỗi khi thêm xe: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Đã xảy ra lỗi khi thêm xe: " + e.getMessage());
        }

        return response;
    }

    /**
     * Cập nhật thông tin xe
     */
    @PutMapping("/update/{vehicleId}")
    @ResponseBody
    public Map<String, Object> updateVehicle(
            @PathVariable String vehicleId,
            @RequestBody Map<String, Object> vehicleData) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Gọi API để cập nhật xe
            Map<String, Object> updatedVehicle = vehicleRestClient.updateVehicle(vehicleId, vehicleData);
            
            if (updatedVehicle != null) {
                response.put("success", true);
                response.put("message", "Cập nhật xe thành công!");
                response.put("data", updatedVehicle);
            } else {
                response.put("success", false);
                response.put("message", "Không thể cập nhật xe. Vui lòng thử lại.");
            }

        } catch (RuntimeException e) {
            System.err.println("Lỗi khi cập nhật xe: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi khi cập nhật xe.");
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật xe: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Đã xảy ra lỗi khi cập nhật xe: " + e.getMessage());
        }

        return response;
    }

    /**
     * Xóa xe
     */
    @DeleteMapping("/delete/{vehicleId}")
    @ResponseBody
    public Map<String, Object> deleteVehicle(@PathVariable String vehicleId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Gọi API để xóa xe
            boolean deleted = vehicleRestClient.deleteVehicle(vehicleId);
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "Xóa xe thành công!");
            } else {
                response.put("success", false);
                response.put("message", "Không thể xóa xe. Vui lòng thử lại.");
            }

        } catch (RuntimeException e) {
            System.err.println("Lỗi khi xóa xe: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi khi xóa xe.");
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa xe: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Đã xảy ra lỗi khi xóa xe: " + e.getMessage());
        }

        return response;
    }

    /**
     * Format VehicleDTO thành Map để hiển thị trong template
     */
    private Map<String, Object> formatVehicleForDisplay(VehicleDTO vehicle) {
        Map<String, Object> vehicleMap = new HashMap<>();
        
        // Thông tin cơ bản
        vehicleMap.put("id", vehicle.getVehicleId());
        vehicleMap.put("vehicleId", vehicle.getVehicleId());
        vehicleMap.put("name", vehicle.getName() != null && !vehicle.getName().isEmpty() 
                ? vehicle.getName() 
                : (vehicle.getVehicleNumber() != null ? vehicle.getVehicleNumber() : vehicle.getVehicleId()));
        vehicleMap.put("plateNumber", vehicle.getVehicleNumber() != null ? vehicle.getVehicleNumber() : "");
        vehicleMap.put("type", vehicle.getType() != null ? vehicle.getType() : "");
        vehicleMap.put("status", vehicle.getStatus() != null ? vehicle.getStatus() : "ready");
        vehicleMap.put("groupId", vehicle.getGroupId());

        // Format ngày cập nhật
        if (vehicle.getLastServiceDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            vehicleMap.put("updateDate", vehicle.getLastServiceDate().format(formatter));
        } else {
            vehicleMap.put("updateDate", "Chưa có");
        }

        // Icon và màu sắc dựa trên loại xe
        String vehicleType = vehicle.getType() != null ? vehicle.getType().toLowerCase() : "";
        if (vehicleType.contains("electric")) {
            vehicleMap.put("icon", "fas fa-bolt");
            vehicleMap.put("iconColor", "electric");
        } else if (vehicleType.contains("sedan")) {
            vehicleMap.put("icon", "fas fa-car");
            vehicleMap.put("iconColor", "sedan");
        } else if (vehicleType.contains("suv")) {
            vehicleMap.put("icon", "fas fa-car-side");
            vehicleMap.put("iconColor", "suv");
        } else {
            vehicleMap.put("icon", "fas fa-car");
            vehicleMap.put("iconColor", "default");
        }

        return vehicleMap;
    }
}
