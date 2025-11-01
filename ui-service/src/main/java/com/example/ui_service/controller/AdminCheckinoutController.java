package com.example.ui_service.controller;

import com.example.ui_service.model.CheckinoutDTO;
import com.example.ui_service.model.ContractDTO;
import com.example.ui_service.model.VehicleDTO;
import com.example.ui_service.service.CheckinoutRestClient;
import com.example.ui_service.service.ContractRestClient;
import com.example.ui_service.service.VehicleRestClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/checkinout")
public class AdminCheckinoutController {

    private final CheckinoutRestClient checkinoutRestClient;
    private final VehicleRestClient vehicleRestClient;
    private final ContractRestClient contractRestClient;

    public AdminCheckinoutController(CheckinoutRestClient checkinoutRestClient,
                                     VehicleRestClient vehicleRestClient,
                                     ContractRestClient contractRestClient) {
        this.checkinoutRestClient = checkinoutRestClient;
        this.vehicleRestClient = vehicleRestClient;
        this.contractRestClient = contractRestClient;
    }

    @GetMapping
    public String showCheckinoutPage(Model model) {
        model.addAttribute("checkinoutList", checkinoutRestClient.getAllLogs());
        model.addAttribute("checkinout", new CheckinoutDTO());

        // Lấy danh sách xe và hợp đồng từ các dịch vụ khác
        List<VehicleDTO> vehicleList = vehicleRestClient.getAllVehicles();
        model.addAttribute("vehicleList", vehicleList);

        List<ContractDTO> contractList = contractRestClient.getAllContracts();
        model.addAttribute("contractList", contractList);

        return "admin/checkin-checkout";
    }
}
