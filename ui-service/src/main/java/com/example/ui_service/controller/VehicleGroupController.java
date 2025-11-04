package com.example.ui_service.controller;

import com.example.ui_service.model.VehiclegroupDTO;
import com.example.ui_service.service.VehicleGroupRestClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
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
                                 @RequestParam(value = "deleteGroupId", required = false) String deleteGroupId) {

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

        // Thêm dữ liệu vào model
        model.addAttribute("filteredGroups", filteredGroups);
        model.addAttribute("totalGroups", filteredGroups.size());
        model.addAttribute("activeGroups", filteredGroups.stream().filter(group -> "active".equalsIgnoreCase(group.getActive())).count());
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("searchQuery", searchQuery);

        // Thêm thông báo trạng thái xóa nhóm xe (nếu có)
        if (deleteStatusMessage != null) {
            model.addAttribute("deleteStatusMessage", deleteStatusMessage);
            model.addAttribute("deleteSuccess", deleteSuccess);
        }

        // Nếu không có kết quả, thêm một thông báo
        if (filteredGroups.isEmpty()) {
            model.addAttribute("noDataMessage", "Không tìm thấy nhóm xe phù hợp.");
        }

        return "admin/staff-management";  // Trả về trang staff-management
    }

    /**
     * Xử lý cập nhật nhóm xe
     * @param groupId ID của nhóm xe cần cập nhật
     * @param name Tên nhóm xe
     * @param vehicleCount Số lượng xe
     * @param active Trạng thái (active/inactive)
     * @param description Mô tả
     * @param redirectAttributes Để truyền thông báo sau khi redirect
     * @return Redirect về trang staff-management với thông báo kết quả
     */
    @PostMapping("/admin/staff-management/update/{groupId}")
    public String updateVehicleGroup(@PathVariable String groupId,
                                     @RequestParam("name") String name,
                                     @RequestParam(value = "vehicleCount", required = false, defaultValue = "0") Integer vehicleCount,
                                     @RequestParam("active") String active,
                                     @RequestParam(value = "description", required = false, defaultValue = "") String description,
                                     RedirectAttributes redirectAttributes) {
        try {
            // Tạo DTO từ các tham số
            VehiclegroupDTO vehicleGroup = new VehiclegroupDTO();
            vehicleGroup.setGroupId(groupId);
            vehicleGroup.setName(name);
            vehicleGroup.setVehicleCount(vehicleCount);
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
        return "redirect:/admin/staff-management";
    }
}
