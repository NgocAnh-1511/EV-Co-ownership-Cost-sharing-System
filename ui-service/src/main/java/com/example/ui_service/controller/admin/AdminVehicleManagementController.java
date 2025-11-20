package com.example.ui_service.controller.admin;

import com.example.ui_service.external.model.VehicleDTO;
import com.example.ui_service.external.model.VehiclegroupDTO;
import com.example.ui_service.external.service.VehicleGroupRestClient;
import com.example.ui_service.external.service.VehicleRestClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/vehicle-management")
public class AdminVehicleManagementController {

    private final VehicleRestClient vehicleRestClient;
    private final VehicleGroupRestClient vehicleGroupRestClient;

    public AdminVehicleManagementController(VehicleRestClient vehicleRestClient, VehicleGroupRestClient vehicleGroupRestClient) {
        this.vehicleRestClient = vehicleRestClient;
        this.vehicleGroupRestClient = vehicleGroupRestClient;
    }

    @GetMapping
    public String vehicleManagementPage(
            Model model,
            @RequestParam(value = "searchQuery", required = false, defaultValue = "") String searchQuery,
            @RequestParam(value = "statusFilter", required = false, defaultValue = "all") String statusFilter,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        
        model.addAttribute("pageTitle", "Quản lý xe");
        model.addAttribute("pageSubtitle", "Quản lý thông tin và trạng thái các xe trong hệ thống");

        List<VehicleDTO> allVehicles = vehicleRestClient.getAllVehicles();

        List<VehicleDTO> filteredVehicles = allVehicles.stream()
                .filter(vehicle -> {
                    boolean matchesSearch = searchQuery.isEmpty()
                            || (vehicle.getName() != null && vehicle.getName().toLowerCase().contains(searchQuery.toLowerCase()))
                            || (vehicle.getVehicleNumber() != null && vehicle.getVehicleNumber().toLowerCase().contains(searchQuery.toLowerCase()))
                            || (vehicle.getVehicleId() != null && vehicle.getVehicleId().toLowerCase().contains(searchQuery.toLowerCase()));

                    boolean matchesStatus = "all".equals(statusFilter)
                            || (vehicle.getStatus() != null && vehicle.getStatus().equalsIgnoreCase(statusFilter));

                    return matchesSearch && matchesStatus;
                })
                .collect(Collectors.toList());

        long totalVehicles = allVehicles.size();
        long readyVehicles = allVehicles.stream().filter(v -> "ready".equalsIgnoreCase(v.getStatus())).count();
        long inUseVehicles = allVehicles.stream().filter(v -> "in_use".equalsIgnoreCase(v.getStatus())).count();
        long maintenanceVehicles = allVehicles.stream().filter(v -> "maintenance".equalsIgnoreCase(v.getStatus())).count();
        long repairVehicles = allVehicles.stream().filter(v -> "repair".equalsIgnoreCase(v.getStatus())).count();
        long checkingVehicles = allVehicles.stream().filter(v -> "checking".equalsIgnoreCase(v.getStatus())).count();

        int totalPages = filteredVehicles.isEmpty() ? 1 : (int) Math.ceil((double) filteredVehicles.size() / size);
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, filteredVehicles.size());
        List<VehicleDTO> pagedVehicles = filteredVehicles.isEmpty()
                ? filteredVehicles : filteredVehicles.subList(startIndex, endIndex);

        List<Map<String, Object>> vehiclesForDisplay = pagedVehicles.stream()
                .map(this::formatVehicleForDisplay)
                .collect(Collectors.toList());

        List<VehiclegroupDTO> availableGroups = vehicleGroupRestClient.getAvailableVehicleGroups(null);

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

        return "admin-vehicle-management";
    }

    @GetMapping("/api/{vehicleId}")
    @ResponseBody
    public Map<String, Object> getVehicleDetails(@PathVariable String vehicleId) {
        Map<String, Object> response = new HashMap<>();
        VehicleDTO vehicle = vehicleRestClient.getVehicleById(vehicleId);
        if (vehicle == null) {
            response.put("error", "Không tìm thấy xe với ID: " + vehicleId);
            return response;
        }
        List<VehiclegroupDTO> availableGroups = vehicleGroupRestClient.getAvailableVehicleGroups(vehicle.getGroupId());
        response.put("vehicle", vehicle);
        response.put("availableGroups", availableGroups);
        return response;
    }

    @PostMapping("/api/add")
    @ResponseBody
    public Map<String, Object> addVehicle(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        Object result = vehicleRestClient.addVehicle(requestData);
        if (result != null) {
            response.put("success", true);
            response.put("message", "Thêm xe thành công!");
            response.put("data", result);
        } else {
            response.put("success", false);
            response.put("message", "Không thể thêm xe. Vui lòng thử lại.");
        }
        return response;
    }

    @PutMapping("/api/update/{vehicleId}")
    @ResponseBody
    public Map<String, Object> updateVehicle(@PathVariable String vehicleId,
                                             @RequestBody Map<String, Object> vehicleData) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> updated = vehicleRestClient.updateVehicle(vehicleId, vehicleData);
        if (updated != null) {
            response.put("success", true);
            response.put("message", "Cập nhật xe thành công!");
            response.put("data", updated);
        } else {
            response.put("success", false);
            response.put("message", "Không thể cập nhật xe. Vui lòng thử lại.");
        }
        return response;
    }

    @DeleteMapping("/api/delete/{vehicleId}")
    @ResponseBody
    public Map<String, Object> deleteVehicle(@PathVariable String vehicleId) {
        Map<String, Object> response = new HashMap<>();
        boolean deleted = vehicleRestClient.deleteVehicle(vehicleId);
        response.put("success", deleted);
        response.put("message", deleted ? "Xóa xe thành công!" : "Không thể xóa xe. Vui lòng thử lại.");
        return response;
    }

    private Map<String, Object> formatVehicleForDisplay(VehicleDTO vehicle) {
        Map<String, Object> vehicleMap = new HashMap<>();
        vehicleMap.put("id", vehicle.getVehicleId());
        vehicleMap.put("vehicleId", vehicle.getVehicleId());
        vehicleMap.put("name", vehicle.getName() != null && !vehicle.getName().isEmpty()
                ? vehicle.getName()
                : (vehicle.getVehicleNumber() != null ? vehicle.getVehicleNumber() : vehicle.getVehicleId()));
        vehicleMap.put("plateNumber", vehicle.getVehicleNumber() != null ? vehicle.getVehicleNumber() : "");
        vehicleMap.put("type", vehicle.getType() != null ? vehicle.getType() : "");
        vehicleMap.put("status", vehicle.getStatus() != null ? vehicle.getStatus() : "ready");
        vehicleMap.put("groupId", vehicle.getGroupId());
        if (vehicle.getLastServiceDate() != null) {
            vehicleMap.put("updateDate", vehicle.getLastServiceDate().toString());
        } else {
            vehicleMap.put("updateDate", "Chưa có");
        }
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
