package com.example.reservationservice.service;

import com.example.reservationservice.model.GroupMember;
import com.example.reservationservice.model.Vehicle;
import com.example.reservationservice.repository.GroupMemberRepository;
import com.example.reservationservice.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository vehicleRepo;
    private final GroupMemberRepository groupMemberRepo;

    @Transactional(readOnly = true)
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
    
    // Get vehicles that a specific user co-owns
    @Transactional(readOnly = true)
    public List<Map<String,Object>> getUserVehicles(Long userId) {
        // Get all groups that user belongs to
        List<GroupMember> userGroups = groupMemberRepo.findGroupsByUserId(userId);
        
        // Get group IDs
        Set<String> groupIds = userGroups.stream()
                .map(gm -> gm.getGroup().getGroupId())
                .collect(Collectors.toSet());
        
        // Get vehicles in those groups
        List<Map<String,Object>> list = new ArrayList<>();
        for (Vehicle v : vehicleRepo.findAll()) {
            if (groupIds.contains(v.getVehicleGroup().getGroupId())) {
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("vehicleId", v.getVehicleId());
                m.put("vehicleName", v.getVehicleName());
                m.put("vehicleType", v.getVehicleType());
                m.put("licensePlate", v.getLicensePlate());
                m.put("groupId", v.getVehicleGroup().getGroupId());
                m.put("groupName", v.getVehicleGroup().getGroupName());
                
                // Get ownership percentage for this user
                var members = groupMemberRepo.findByGroup_GroupId(v.getVehicleGroup().getGroupId());
                var userMember = members.stream()
                        .filter(gm -> gm.getUser().getUserId().equals(userId))
                        .findFirst();
                
                m.put("ownershipPercentage", userMember.map(GroupMember::getOwnershipPercentage).orElse(0.0));
                
                var owners = members.stream()
                        .map(gm -> gm.getUser().getFullName() + " (" + gm.getOwnershipPercentage() + "%)")
                        .toList();
                m.put("owners", owners);
                list.add(m);
            }
        }
        return list;
    }
    
    // Get group information for a specific vehicle
    @Transactional(readOnly = true)
    public Map<String,Object> getVehicleGroupInfo(Long vehicleId) {
        Vehicle vehicle = vehicleRepo.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        
        var group = vehicle.getVehicleGroup();
        var members = groupMemberRepo.findByGroup_GroupId(group.getGroupId());
        
        Map<String,Object> groupInfo = new LinkedHashMap<>();
        groupInfo.put("groupId", group.getGroupId());
        groupInfo.put("groupName", group.getGroupName());
        groupInfo.put("description", group.getDescription());
        
        List<Map<String,Object>> memberList = new ArrayList<>();
        for (GroupMember gm : members) {
            Map<String,Object> member = new LinkedHashMap<>();
            member.put("userId", gm.getUser().getUserId());
            member.put("fullName", gm.getUser().getFullName());
            member.put("email", gm.getUser().getEmail());
            member.put("ownershipPercentage", gm.getOwnershipPercentage());
            memberList.add(member);
        }
        groupInfo.put("members", memberList);
        
        return groupInfo;
    }
}
