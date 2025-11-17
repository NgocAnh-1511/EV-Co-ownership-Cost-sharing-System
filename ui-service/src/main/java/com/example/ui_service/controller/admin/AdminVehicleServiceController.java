package com.example.ui_service.controller.admin;

import com.example.ui_service.external.model.VehicleDTO;
import com.example.ui_service.external.service.ServiceRestClient;
import com.example.ui_service.external.service.VehicleRestClient;
import com.example.ui_service.external.service.VehicleServiceRestClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

import java.text.Normalizer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/admin/vehicle-services")
public class AdminVehicleServiceController {

    private final VehicleServiceRestClient vehicleServiceRestClient;
    private final ServiceRestClient serviceRestClient;
    private final VehicleRestClient vehicleRestClient;

    public AdminVehicleServiceController(VehicleServiceRestClient vehicleServiceRestClient,
                                        ServiceRestClient serviceRestClient,
                                        VehicleRestClient vehicleRestClient) {
        this.vehicleServiceRestClient = vehicleServiceRestClient;
        this.serviceRestClient = serviceRestClient;
        this.vehicleRestClient = vehicleRestClient;
    }

    @GetMapping
    public String page(Model model,
                       @RequestParam(value = "searchQuery", required = false, defaultValue = "") String searchQuery,
                       @RequestParam(value = "serviceFilter", required = false, defaultValue = "all") String serviceFilter) {
        model.addAttribute("pageTitle", "Quản lý dịch vụ xe");
        model.addAttribute("pageSubtitle", "Quản lý và theo dõi các dịch vụ bảo dưỡng, kiểm tra và sửa chữa xe");
        model.addAttribute("activePage", "vehicle-services");
        model.addAttribute("contentFragment", "admin/vehicle-services :: content");
        model.addAttribute("pageCss", new String[]{"/ext/css/vehicle-manager.css"});
        model.addAttribute("pageJs", new String[]{"/ext/js/vehicle-manager.js"});
        
        model.addAttribute("services", serviceRestClient.getAllServices());
        model.addAttribute("serviceTypes", serviceRestClient.getServiceTypes());

        List<VehicleDTO> vehicles = vehicleRestClient.getAllVehicles();
        List<Map<String, Object>> vehicleServices = vehicleServiceRestClient.getAllVehicleServices();

        Map<String, List<Map<String, Object>>> servicesByVehicle = vehicleServices.stream()
                .filter(service -> extractVehicleId(service) != null && !extractVehicleId(service).isBlank())
                .collect(Collectors.groupingBy(this::extractVehicleId));

        List<Map<String, Object>> vehicleViewModels = vehicles.stream()
                .map(vehicle -> buildVehicleViewModel(vehicle,
                        servicesByVehicle.getOrDefault(vehicle.getVehicleId(), Collections.emptyList())))
                .collect(Collectors.toList());

        List<Map<String, Object>> filteredVehicles = vehicleViewModels.stream()
                .filter(vehicle -> matchesSearch(vehicle, searchQuery))
                .filter(vehicle -> matchesServiceFilter(vehicle, serviceFilter))
                .collect(Collectors.toList());

        model.addAttribute("vehicles", filteredVehicles);
        model.addAttribute("totalVehicles", vehicles.size());
        model.addAttribute("maintenanceVehicles", countVehiclesByServiceType(servicesByVehicle, "maintenance"));
        model.addAttribute("inspectionVehicles", countVehiclesByServiceType(servicesByVehicle, "inspection"));
        model.addAttribute("brokenVehicles", countVehiclesByServiceType(servicesByVehicle, "repair"));
        model.addAttribute("vehicleServices", vehicleServices);
        model.addAttribute("completedServices", buildCompletedServices(vehicleServices, vehicles));
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("serviceFilter", serviceFilter);

        return "admin-vehicle-services";
    }

    @PostMapping("/register")
    @ResponseBody
    public Map<String, Object> registerVehicleService(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Nếu request có vehicleId và serviceId thì đăng ký dịch vụ cho xe
            if (requestData.containsKey("vehicleId") && requestData.containsKey("serviceId")) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("vehicleId", requestData.get("vehicleId"));
                payload.put("serviceId", requestData.get("serviceId"));
                if (requestData.containsKey("serviceDescription")) {
                    payload.put("serviceDescription", requestData.get("serviceDescription"));
                }
                vehicleServiceRestClient.registerVehicleService(payload);
                response.put("success", true);
                response.put("message", "Đăng ký dịch vụ thành công");
            } else {
                // Nếu chỉ có serviceId, serviceName, serviceType thì tạo dịch vụ mới
                // (Cần implement logic tạo service mới nếu cần)
                response.put("success", false);
                response.put("message", "Chức năng tạo dịch vụ mới chưa được implement");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/service/{serviceId}/vehicle/{vehicleId}/status")
    public String updateStatus(@PathVariable("serviceId") String serviceId,
                               @PathVariable("vehicleId") String vehicleId,
                               @RequestParam("status") String status) {
        vehicleServiceRestClient.updateServiceStatus(serviceId, vehicleId, status);
        return "redirect:/admin/vehicle-services";
    }
    
    @GetMapping("/api/vehicle/{vehicleId}/services")
    @ResponseBody
    public Map<String, Object> getVehicleServices(@PathVariable String vehicleId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> services = vehicleServiceRestClient.getVehicleServicesByVehicleId(vehicleId);
            response.put("success", true);
            response.put("services", services);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi lấy danh sách dịch vụ: " + e.getMessage());
            response.put("services", Collections.emptyList());
        }
        return response;
    }

    private Map<String, Object> buildVehicleViewModel(VehicleDTO vehicle, List<Map<String, Object>> services) {
        Map<String, Object> view = new HashMap<>();
        String vehicleId = vehicle.getVehicleId();
        String displayName = resolveVehicleName(vehicle, vehicleId);
        String plateNumber = defaultString(vehicle.getVehicleNumber(), "-");
        String vehicleType = defaultString(vehicle.getType(), "-");

        view.put("vehicleId", vehicleId);
        view.put("name", displayName);
        view.put("plateNumber", plateNumber);
        view.put("category", vehicleType);
        view.put("typeDetail", vehicleType);

        String iconClass = determineIconClass(vehicleType);
        view.put("iconClass", "vehicle-icon " + iconClass);

        List<Map<String, Object>> serviceDisplays = services.stream()
                .map(this::buildServiceDisplay)
                .collect(Collectors.toList());
        view.put("services", serviceDisplays);

        String overallStatus = determineOverallStatus(serviceDisplays);
        view.put("overallStatus", overallStatus);

        Instant latestRequest = services.stream()
                .map(service -> parseInstant(service.get("requestDate")))
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
        view.put("formattedRequestDate", formatInstant(latestRequest));

        return view;
    }

    private Map<String, Object> buildServiceDisplay(Map<String, Object> service) {
        Map<String, Object> display = new HashMap<>();
        String serviceName = defaultString(service.get("serviceName"), "Dịch vụ không tên");
        String statusRaw = defaultString(service.get("status"), "pending");
        String statusNormalized = normalizeStatus(statusRaw);
        String serviceTypeRaw = defaultString(service.get("serviceType"), "Khác");
        String serviceTypeNormalized = normalizeServiceTypeValue(serviceTypeRaw);

        display.put("serviceName", serviceName);
        display.put("status", statusRaw);
        display.put("statusClass", mapStatusToClass(statusNormalized));
        display.put("statusNormalized", statusNormalized);
        display.put("serviceType", prettyServiceType(serviceTypeRaw));
        display.put("serviceTypeNormalized", serviceTypeNormalized);
        display.put("requestDateRaw", service.get("requestDate"));

        return display;
    }

    private String determineOverallStatus(List<Map<String, Object>> services) {
        if (services == null || services.isEmpty()) {
            return "pending";
        }
        boolean hasPending = services.stream()
                .anyMatch(service -> "pending".equals(service.get("statusNormalized")));
        if (hasPending) {
            return "pending";
        }
        boolean hasInProgress = services.stream()
                .anyMatch(service -> "in_progress".equals(service.get("statusNormalized")));
        if (hasInProgress) {
            return "in_progress";
        }
        boolean hasCompleted = services.stream()
                .anyMatch(service -> "completed".equals(service.get("statusNormalized")));
        if (hasCompleted) {
            return "complete";
        }
        return "pending";
    }

    private String extractVehicleId(Map<String, Object> service) {
        if (service == null) {
            return null;
        }
        Object vehicleId = service.get("vehicleId");
        if (vehicleId != null) {
            return vehicleId.toString();
        }
        Object compoundId = service.get("id");
        if (compoundId instanceof Map<?, ?> compoundMap) {
            Object nestedVehicleId = compoundMap.get("vehicleId");
            if (nestedVehicleId != null) {
                return nestedVehicleId.toString();
            }
        }
        return null;
    }

    private boolean matchesSearch(Map<String, Object> vehicle, String searchQuery) {
        if (searchQuery == null || searchQuery.isBlank()) {
            return true;
        }
        String keyword = normalizeText(searchQuery);
        return Stream.of("name", "plateNumber", "vehicleId")
                .map(key -> normalizeText(Objects.toString(vehicle.get(key), "")))
                .anyMatch(value -> value.contains(keyword));
    }

    @SuppressWarnings("unchecked")
    private boolean matchesServiceFilter(Map<String, Object> vehicle, String serviceFilter) {
        if (serviceFilter == null || serviceFilter.isBlank() || "all".equalsIgnoreCase(serviceFilter)) {
            return true;
        }
        String normalizedFilter = normalizeServiceTypeValue(serviceFilter);
        List<Map<String, Object>> services = (List<Map<String, Object>>) vehicle.get("services");
        if (services == null || services.isEmpty()) {
            return false;
        }
        return services.stream()
                .map(service -> Objects.toString(service.get("serviceTypeNormalized"), ""))
                .anyMatch(type -> type.equals(normalizedFilter));
    }

    private long countVehiclesByServiceType(Map<String, List<Map<String, Object>>> servicesByVehicle, String targetType) {
        if (servicesByVehicle == null || servicesByVehicle.isEmpty()) {
            return 0;
        }
        String normalizedTarget = normalizeServiceTypeValue(targetType);
        return servicesByVehicle.entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .anyMatch(service -> normalizeServiceTypeValue(service.get("serviceType")).equals(normalizedTarget)))
                .count();
    }

    private List<Map<String, Object>> buildCompletedServices(List<Map<String, Object>> vehicleServices,
                                                             List<VehicleDTO> vehicles) {
        if (vehicleServices == null || vehicleServices.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, VehicleDTO> vehicleIndex = vehicles.stream()
                .filter(vehicle -> vehicle.getVehicleId() != null && !vehicle.getVehicleId().isBlank())
                .collect(Collectors.toMap(VehicleDTO::getVehicleId, Function.identity(), (existing, replacement) -> existing));

        return vehicleServices.stream()
                .filter(service -> "completed".equals(normalizeStatus(service.get("status"))))
                .map(service -> {
                    Map<String, Object> view = new HashMap<>();
                    String vehicleId = extractVehicleId(service);
                    VehicleDTO vehicle = vehicleIndex.get(vehicleId);

                    view.put("vehicleName", resolveVehicleName(vehicle, vehicleId));
                    view.put("vehicleNumber", vehicle != null && vehicle.getVehicleNumber() != null
                            ? vehicle.getVehicleNumber() : "-");
                    view.put("vehicleType", vehicle != null && vehicle.getType() != null
                            ? vehicle.getType() : "-");
                    view.put("serviceName", defaultString(service.get("serviceName"), "-"));
                    view.put("serviceType", prettyServiceType(service.get("serviceType")));

                    Instant requestInstant = parseInstant(service.get("requestDate"));
                    Instant completionInstant = parseInstant(service.get("completionDate"));
                    view.put("formattedRequestDate", formatInstant(requestInstant));
                    view.put("formattedCompletionDate", formatInstant(completionInstant));
                    view.put("_completionInstant", completionInstant);
                    return view;
                })
                .sorted(Comparator.comparing(
                        (Map<String, Object> item) -> (Instant) item.get("_completionInstant"),
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(item -> {
                    item.remove("_completionInstant");
                    return item;
                })
                .collect(Collectors.toList());
    }

    private String determineIconClass(String vehicleType) {
        String normalized = normalizeText(vehicleType);
        if (normalized.contains("electric") || normalized.contains("ev")) {
            return "icon-electric";
        }
        if (normalized.contains("suv")) {
            return "icon-suv";
        }
        if (normalized.contains("truck")) {
            return "icon-truck";
        }
        if (normalized.contains("sedan")) {
            return "icon-sedan";
        }
        return "icon-default";
    }

    private String resolveVehicleName(VehicleDTO vehicle, String vehicleId) {
        if (vehicle == null) {
            return vehicleId != null && !vehicleId.isBlank() ? vehicleId : "-";
        }
        if (vehicle.getName() != null && !vehicle.getName().isBlank()) {
            return vehicle.getName();
        }
        if (vehicle.getVehicleNumber() != null && !vehicle.getVehicleNumber().isBlank()) {
            return vehicle.getVehicleNumber();
        }
        return vehicleId != null && !vehicleId.isBlank() ? vehicleId : "-";
    }

    private String defaultString(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String str = value.toString().trim();
        return str.isEmpty() ? defaultValue : str;
    }

    private String normalizeText(Object value) {
        if (value == null) {
            return "";
        }
        String str = value.toString().trim().toLowerCase(Locale.ENGLISH);
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private String normalizeStatus(Object status) {
        if (status == null) {
            return "pending";
        }
        String normalized = normalizeText(status);
        if ("inprogress".equals(normalized)) {
            return "in_progress";
        }
        if ("completed".equals(normalized) || "complete".equals(normalized)) {
            return "completed";
        }
        if ("pending".equals(normalized)) {
            return "pending";
        }
        return normalized;
    }

    private String mapStatusToClass(String status) {
        if ("completed".equals(status)) {
            return "completed";
        }
        if ("in_progress".equals(status)) {
            return "in-progress";
        }
        return "pending";
    }

    private String normalizeServiceTypeValue(Object type) {
        if (type == null) {
            return "";
        }
        String normalized = normalizeText(type);
        if (normalized.contains("maintenance") || normalized.contains("baoduong")) {
            return "maintenance";
        }
        if (normalized.contains("inspection") || normalized.contains("kiemtra")) {
            return "inspection";
        }
        if (normalized.contains("repair") || normalized.contains("suachua")) {
            return "repair";
        }
        return normalized.replaceAll("[^a-z]", "");
    }

    private String prettyServiceType(Object type) {
        if (type == null) {
            return "-";
        }
        return type.toString();
    }

    private Instant parseInstant(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Instant instant) {
            return instant;
        }
        if (raw instanceof java.sql.Timestamp timestamp) {
            return timestamp.toInstant();
        }
        if (raw instanceof java.time.LocalDateTime localDateTime) {
            return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        }
        String value = raw.toString();
        try {
            return Instant.parse(value);
        } catch (Exception ignored) {
            try {
                return java.time.LocalDateTime.parse(value)
                        .atZone(ZoneId.systemDefault()).toInstant();
            } catch (Exception ignoredAgain) {
                return null;
            }
        }
    }

    private String formatInstant(Instant instant) {
        if (instant == null) {
            return "-";
        }
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }
}

