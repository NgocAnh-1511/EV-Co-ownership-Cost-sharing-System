package com.example.ui_service.controller;

import com.example.ui_service.model.VehiclegroupDTO;
import com.example.ui_service.service.VehicleGroupRestClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class VehicleGroupController {

    private final VehicleGroupRestClient vehicleGroupRestClient;

    public VehicleGroupController(VehicleGroupRestClient vehicleGroupRestClient) {
        this.vehicleGroupRestClient = vehicleGroupRestClient;
    }

    @GetMapping("/admin/staff-management")
    public String StaffMangement(Model model,
                                 @RequestParam(value = "searchQuery", required = false, defaultValue = "") String searchQuery,
                                 @RequestParam(value = "statusFilter", required = false, defaultValue = "Tất cả") String statusFilter,
                                 @RequestParam(value = "deleteGroupId", required = false) String deleteGroupId,
                                 @RequestParam(value = "viewGroupId", required = false) String viewGroupId,
                                 @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                 @RequestParam(value = "size", required = false, defaultValue = "10") int size) {

        String deleteStatusMessage = null;
        boolean deleteSuccess = false;

        // Nếu có yêu cầu xóa nhóm xe, thực hiện gọi API xóa
        if (deleteGroupId != null && !deleteGroupId.isEmpty()) {
            try {
                String resultMessage = vehicleGroupRestClient.deleteVehicleGroup(deleteGroupId);
                // Sử dụng thông báo từ API hoặc thông báo mặc định
                deleteStatusMessage = resultMessage != null && !resultMessage.isEmpty() 
                    ? resultMessage 
                    : "Nhóm xe với ID " + deleteGroupId + " đã được xóa thành công.";
                deleteSuccess = true;
            } catch (RuntimeException e) {
                // Xử lý lỗi từ RestClient (bao gồm cả lỗi từ API)
                deleteStatusMessage = e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi khi xóa nhóm xe.";
                deleteSuccess = false;
            } catch (Exception e) {
                // Xử lý các lỗi khác
                deleteStatusMessage = "Lỗi không xác định khi xóa nhóm xe: " + e.getMessage();
                deleteSuccess = false;
            }
        }

        // Nếu có yêu cầu xem chi tiết nhóm xe, load thông tin
        VehiclegroupDTO viewGroup = null;
        List<?> viewGroupVehicles = null;
        boolean showViewModal = false;
        if (viewGroupId != null && !viewGroupId.isEmpty()) {
            System.out.println("DEBUG: viewGroupId = " + viewGroupId);
            try {
                viewGroup = vehicleGroupRestClient.getVehicleGroupById(viewGroupId);
                System.out.println("DEBUG: viewGroup = " + (viewGroup != null ? viewGroup.getName() : "null"));
                if (viewGroup != null) {
                    viewGroupVehicles = vehicleGroupRestClient.getVehiclesByGroupId(viewGroupId);
                    System.out.println("DEBUG: viewGroupVehicles size = " + (viewGroupVehicles != null ? viewGroupVehicles.size() : 0));
                    // Debug: In ra dữ liệu từng vehicle
                    if (viewGroupVehicles != null && !viewGroupVehicles.isEmpty()) {
                        System.out.println("DEBUG: First vehicle data: " + viewGroupVehicles.get(0));
                        if (viewGroupVehicles.get(0) instanceof Map) {
                            Map<String, Object> firstVehicle = (Map<String, Object>) viewGroupVehicles.get(0);
                            System.out.println("DEBUG: vehicleNumber = " + firstVehicle.get("vehicleNumber"));
                            System.out.println("DEBUG: All keys in vehicle: " + firstVehicle.keySet());
                        }
                    }
                    showViewModal = true;
                    System.out.println("DEBUG: showViewModal = true");
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi load chi tiết nhóm xe: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        model.addAttribute("viewGroup", viewGroup);
        model.addAttribute("viewGroupVehicles", viewGroupVehicles);
        model.addAttribute("showViewModal", showViewModal);
        System.out.println("DEBUG: Final showViewModal = " + showViewModal);

        // Thiết lập các tham số cho trang
        model.addAttribute("pageTitle", "Quản Lý Nhóm Xe Đồng Sở Hữu");
        model.addAttribute("pageDescription", "Quản lý thông tin nhóm xe");

        // Lấy danh sách tất cả các nhóm xe từ API
        List<VehiclegroupDTO> vehicleGroups = vehicleGroupRestClient.getAllVehicleGroups();

        // Lọc nhóm theo tên (searchQuery) và trạng thái (statusFilter)
        List<VehiclegroupDTO> filteredGroups = vehicleGroups.stream()
                .filter(group -> (searchQuery.isEmpty() || group.getName().toLowerCase().contains(searchQuery.toLowerCase())) &&
                        ("Tất cả".equals(statusFilter) || group.getActive().equalsIgnoreCase(statusFilter)))
                .collect(Collectors.toList());

        // Tính toán thống kê
        long totalGroups = filteredGroups.size();
        long activeGroups = filteredGroups.stream()
                .filter(group -> "active".equalsIgnoreCase(group.getActive()))
                .count();

        // Phân trang
        int totalPages = filteredGroups.isEmpty() ? 1 : (int) Math.ceil((double) filteredGroups.size() / size);
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, filteredGroups.size());
        List<VehiclegroupDTO> pagedGroups = filteredGroups.isEmpty() ? 
            filteredGroups : filteredGroups.subList(startIndex, endIndex);

        // Thêm dữ liệu vào model
        model.addAttribute("filteredGroups", pagedGroups);
        model.addAttribute("totalGroups", totalGroups);
        model.addAttribute("activeGroups", activeGroups);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalFiltered", filteredGroups.size());
        model.addAttribute("startIndex", startIndex + 1);
        model.addAttribute("endIndex", endIndex);

        // Thêm thông báo trạng thái xóa nhóm xe (nếu có)
        if (deleteStatusMessage != null) {
            model.addAttribute("deleteStatusMessage", deleteStatusMessage);
            model.addAttribute("deleteSuccess", deleteSuccess);
        }

        // Nếu không có kết quả, thêm một thông báo
        if (filteredGroups.isEmpty()) {
            model.addAttribute("noDataMessage", "Không tìm thấy nhóm xe phù hợp.");
        }

        return "/admin/staff-management";  // Trả về trang staff-management
    }

    /**
     * Xử lý thêm nhóm xe mới
     * @param groupId Mã nhóm xe
     * @param name Tên nhóm xe
     * @param active Trạng thái (active/inactive)
     * @param description Mô tả
     * @param vehiclesJson JSON string chứa danh sách xe cần thêm (nếu có)
     * @param redirectAttributes Để truyền thông báo sau khi redirect
     * @return Redirect về trang staff-management với thông báo kết quả
     */
    @PostMapping("/admin/staff-management/add")
    public String addVehicleGroup(@RequestParam("groupId") String groupId,
                                  @RequestParam("name") String name,
                                  @RequestParam(value = "active", required = false, defaultValue = "active") String active,
                                  @RequestParam(value = "description", required = false, defaultValue = "") String description,
                                  @RequestParam(value = "vehicles", required = false) String vehiclesJson,
                                  RedirectAttributes redirectAttributes) {
        try {
            // Tạo DTO từ các tham số
            VehiclegroupDTO vehicleGroup = new VehiclegroupDTO();
            vehicleGroup.setGroupId(groupId);
            vehicleGroup.setName(name);
            vehicleGroup.setActive(active);
            vehicleGroup.setDescription(description);
            
            VehiclegroupDTO addedGroup = vehicleGroupRestClient.addVehicleGroup(vehicleGroup);
            if (addedGroup != null) {
                // Xử lý thêm xe nếu có
                if (vehiclesJson != null && !vehiclesJson.isEmpty()) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        List<Map<String, Object>> vehicles = objectMapper.readValue(vehiclesJson, 
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                        
                        if (vehicles != null && !vehicles.isEmpty()) {
                            System.out.println("DEBUG: Thêm " + vehicles.size() + " xe vào nhóm mới " + addedGroup.getGroupId());
                            vehicleGroupRestClient.addVehiclesToGroup(addedGroup.getGroupId(), vehicles);
                            System.out.println("DEBUG: Đã thêm xe thành công");
                        }
                    } catch (Exception e) {
                        System.err.println("Lỗi khi parse vehicles JSON: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                redirectAttributes.addFlashAttribute("updateStatusMessage", "Nhóm xe đã được thêm thành công!");
                redirectAttributes.addFlashAttribute("updateSuccess", true);
            } else {
                redirectAttributes.addFlashAttribute("updateStatusMessage", "Không thể thêm nhóm xe. Vui lòng thử lại.");
                redirectAttributes.addFlashAttribute("updateSuccess", false);
            }
        } catch (Exception e) {
                redirectAttributes.addFlashAttribute("updateStatusMessage", "Lỗi khi thêm nhóm xe: " + e.getMessage());
                redirectAttributes.addFlashAttribute("updateSuccess", false);
        }
        return "redirect:/admin/staff-management?searchQuery=&statusFilter=Tất cả&page=1";
    }
    @PostMapping("/admin/staff-management/update/{groupId}")
    public String updateVehicleGroup(@PathVariable String groupId,
                                     @RequestParam("name") String name,
                                     @RequestParam("active") String active,
                                     @RequestParam(value = "description", required = false, defaultValue = "") String description,
                                     @RequestParam(value = "vehicles", required = false) String vehiclesJson,
                                     RedirectAttributes redirectAttributes) {
        try {
            // Xử lý thêm xe nếu có
            if (vehiclesJson != null && !vehiclesJson.isEmpty()) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    List<Map<String, Object>> vehicles = objectMapper.readValue(vehiclesJson, 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                    
                    if (vehicles != null && !vehicles.isEmpty()) {
                        System.out.println("DEBUG: Thêm " + vehicles.size() + " xe vào nhóm " + groupId);
                        vehicleGroupRestClient.addVehiclesToGroup(groupId, vehicles);
                        System.out.println("DEBUG: Đã thêm xe thành công");
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi parse vehicles JSON: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Tạo DTO từ các tham số
            VehiclegroupDTO vehicleGroup = new VehiclegroupDTO();
            vehicleGroup.setGroupId(groupId);
            vehicleGroup.setName(name);
            vehicleGroup.setActive(active);
            vehicleGroup.setDescription(description);
            
            VehiclegroupDTO updatedGroup = vehicleGroupRestClient.updateVehicleGroup(groupId, vehicleGroup);
            if (updatedGroup != null) {
                redirectAttributes.addFlashAttribute("updateStatusMessage", "Nhóm xe đã được cập nhật thành công!");
                redirectAttributes.addFlashAttribute("updateSuccess", true);
            } else {
                redirectAttributes.addFlashAttribute("updateStatusMessage", "Không thể cập nhật nhóm xe. Vui lòng thử lại.");
                redirectAttributes.addFlashAttribute("updateSuccess", false);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("updateStatusMessage", "Lỗi khi cập nhật nhóm xe: " + e.getMessage());
            redirectAttributes.addFlashAttribute("updateSuccess", false);
        }
        return "redirect:/admin/staff-management?searchQuery=&statusFilter=Tất cả&page=1";
    }
}
