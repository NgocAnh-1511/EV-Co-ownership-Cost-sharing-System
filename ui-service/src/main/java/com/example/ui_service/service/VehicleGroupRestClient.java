package com.example.ui_service.service;

import com.example.ui_service.model.VehiclegroupDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;

@Service
public class VehicleGroupRestClient {

    private final String BASE_URL = "http://localhost:8083/api/vehicle-groups";  // URL của API VehicleGroup

    @Autowired
    private RestTemplate restTemplate;

    // Lấy danh sách nhóm xe
    public List<VehiclegroupDTO> getAllVehicleGroups() {
        try {
            // Gọi API GET để lấy danh sách nhóm xe
            ResponseEntity<VehiclegroupDTO[]> responseEntity = restTemplate.getForEntity(BASE_URL, VehiclegroupDTO[].class);

            // Kiểm tra mã trạng thái HTTP và xử lý
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                VehiclegroupDTO[] response = responseEntity.getBody();
                // Chuyển từ array sang List
                return List.of(response);
            } else {
                // Nếu không thành công, trả về một danh sách rỗng hoặc xử lý lỗi phù hợp
                return new ArrayList<>();
            }
        } catch (HttpClientErrorException e) {
            // Xử lý khi API gọi bị lỗi (kết nối thất bại, trả về mã lỗi khác 2xx...)
            System.err.println("Lỗi khi gọi API: " + e.getMessage());
            return new ArrayList<>();
        } catch (RestClientException e) {
            // Xử lý khi API gọi gặp lỗi khác
            System.err.println("Lỗi khi gọi API: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Thêm nhóm xe
    public VehiclegroupDTO addVehicleGroup(VehiclegroupDTO vehicleGroup) {
        try {
            return restTemplate.postForObject(BASE_URL, vehicleGroup, VehiclegroupDTO.class);
        } catch (HttpClientErrorException e) {
            System.err.println("Lỗi khi thêm nhóm xe: " + e.getMessage());
            return null;
        } catch (RestClientException e) {
            System.err.println("Lỗi khi thêm nhóm xe: " + e.getMessage());
            return null;
        }
    }

    // Cập nhật nhóm xe
    public VehiclegroupDTO updateVehicleGroup(String id, VehiclegroupDTO vehicleGroup) {
        try {
            String url = BASE_URL + "/" + id;
            restTemplate.put(url, vehicleGroup);
            return vehicleGroup;  // Trả về vehicleGroup sau khi đã cập nhật
        } catch (HttpClientErrorException e) {
            System.err.println("Lỗi khi cập nhật nhóm xe: " + e.getMessage());
            return null;
        } catch (RestClientException e) {
            System.err.println("Lỗi khi cập nhật nhóm xe: " + e.getMessage());
            return null;
        }
    }

    // Xóa nhóm xe
    public void deleteVehicleGroup(String id) {
        try {
            String url = BASE_URL + "/" + id;
            restTemplate.delete(url);
        } catch (HttpClientErrorException e) {
            System.err.println("Lỗi khi xóa nhóm xe: " + e.getMessage());
        } catch (RestClientException e) {
            System.err.println("Lỗi khi xóa nhóm xe: " + e.getMessage());
        }
    }
}
