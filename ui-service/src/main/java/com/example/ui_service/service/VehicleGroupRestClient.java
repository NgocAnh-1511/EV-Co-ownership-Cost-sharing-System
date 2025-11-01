package com.example.ui_service.service;

import com.example.ui_service.model.VehiclegroupDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Service
public class VehicleGroupRestClient {

    private final String BASE_URL = "http://localhost:8083/api/vehicle-groups";  // URL của API VehicleGroup

    @Autowired
    private RestTemplate restTemplate;

    // Lấy danh sách nhóm xe
    public List<VehiclegroupDTO> getAllVehicleGroups() {
        // Gọi API GET để lấy danh sách nhóm xe, đảm bảo kiểu trả về đúng
        VehiclegroupDTO[] response = restTemplate.getForObject(BASE_URL, VehiclegroupDTO[].class);

        // Chuyển từ array sang List
        return List.of(response);
    }
    // Thêm nhóm xe
    public VehiclegroupDTO addVehicleGroup(VehiclegroupDTO vehicleGroup) {
        return restTemplate.postForObject(BASE_URL, vehicleGroup, VehiclegroupDTO.class);
    }

    // Cập nhật nhóm xe
    public VehiclegroupDTO updateVehicleGroup(String id, VehiclegroupDTO vehicleGroup) {
        String url = BASE_URL + "/" + id;
        restTemplate.put(url, vehicleGroup);
        return vehicleGroup;  // Trả về vehicleGroup sau khi đã cập nhật
    }

    // Xóa nhóm xe
    public void deleteVehicleGroup(String id) {
        String url = BASE_URL + "/" + id;
        restTemplate.delete(url);
    }
}
