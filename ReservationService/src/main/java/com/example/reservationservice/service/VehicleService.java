package com.example.reservationservice.service;

import com.example.reservationservice.model.Vehicle;
import com.example.reservationservice.repository.GroupMemberRepository;
import com.example.reservationservice.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service @RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository vehicleRepo;
    private final GroupMemberRepository groupMemberRepo;

    public List<Map<String,Object>> getAllVehiclesWithOwners() {
        List<Map<String,Object>> list = new ArrayList<>();
        for (Vehicle v : vehicleRepo.findAll()) {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("vehicleId", v.getVehicleId());
            m.put("vehicleName", v.getVehicleName());
            m.put("vehicleType", v.getVehicleType());
            m.put("licensePlate", v.getLicensePlate());
            m.put("groupId", v.getVehicleGroup().getGroupId());
            m.put("groupName", v.getVehicleGroup().getGroupName());
            var owners = groupMemberRepo.findByGroup_GroupId(v.getVehicleGroup().getGroupId())
                    .stream().map(gm -> gm.getUser().getFullName()).toList();
            m.put("owners", owners);
            list.add(m);
        }
        return list;
    }
}
