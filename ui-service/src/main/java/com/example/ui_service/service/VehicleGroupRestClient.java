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
            // Xử lý khi API gọi bị lỗi (kết nối thất bại, trả về mã lỗi khác 2xx...).
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
            // Gọi API POST để thêm nhóm xe
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
            // Sử dụng exchange để có thể xử lý response tốt hơn
            ResponseEntity<VehiclegroupDTO> response = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(vehicleGroup),
                VehiclegroupDTO.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            // Nếu không có body, trả về vehicleGroup đã gửi (giả sử update thành công)
            return vehicleGroup;
        } catch (HttpClientErrorException e) {
            System.err.println("Lỗi khi cập nhật nhóm xe: " + e.getStatusCode() + " - " + e.getMessage());
            String errorBody = e.getResponseBodyAsString();
            if (errorBody != null && !errorBody.isEmpty()) {
                System.err.println("Error body: " + errorBody);
            }
            throw new RuntimeException("Không thể cập nhật nhóm xe: " + (errorBody != null ? errorBody : e.getMessage()), e);
        } catch (RestClientException e) {
            System.err.println("Lỗi khi cập nhật nhóm xe: " + e.getMessage());
            throw new RuntimeException("Lỗi kết nối khi cập nhật nhóm xe: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa nhóm xe theo groupId
     * @param groupId ID của nhóm xe cần xóa
     * @return Thông báo kết quả xóa
     * @throws RuntimeException nếu có lỗi xảy ra
     */
    public String deleteVehicleGroup(String groupId) {
        try {
            // Tạo URL với groupId cần xóa
            String url = BASE_URL + "/" + groupId;

            // Gửi yêu cầu DELETE tới API và nhận response
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.DELETE,
                null,
                String.class
            );

            // Kiểm tra status code
            if (response.getStatusCode().is2xxSuccessful()) {
                String message = response.getBody() != null ? response.getBody() : "Nhóm xe đã được xóa thành công.";
                System.out.println("Nhóm xe với ID " + groupId + " đã được xóa thành công.");
                return message;
            } else {
                String errorMessage = response.getBody() != null ? response.getBody() : "Lỗi không xác định khi xóa nhóm xe.";
                throw new RuntimeException(errorMessage);
            }
        } catch (HttpClientErrorException e) {
            // Xử lý lỗi HTTP (400, 404, etc.)
            String errorMessage = e.getResponseBodyAsString();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = "Lỗi khi xóa nhóm xe: " + e.getStatusCode() + " - " + e.getMessage();
            }
            System.err.println("Lỗi khi xóa nhóm xe: " + errorMessage);
            throw new RuntimeException(errorMessage, e);
        } catch (RestClientException e) {
            // Xử lý lỗi khi gọi RestClient
            String errorMessage = "Lỗi kết nối khi xóa nhóm xe: " + e.getMessage();
            System.err.println(errorMessage);
            throw new RuntimeException(errorMessage, e);
        }
    }
}
