package com.example.ui_service.controller.rest;

import com.example.ui_service.client.GroupManagementClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller để proxy các request Groups từ frontend sang backend
 */
@RestController
@RequestMapping("/api/groups")
public class GroupRestController {

    @Autowired
    private GroupManagementClient groupManagementClient;

    /**
     * Lấy tất cả groups
     * GET /api/groups
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllGroups() {
        try {
            List<Map<String, Object>> groups = groupManagementClient.getAllGroupsAsMap();
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            System.err.println("Error fetching groups: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy group theo ID
     * GET /api/groups/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getGroupById(@PathVariable Integer id) {
        try {
            Map<String, Object> group = groupManagementClient.getGroupByIdAsMap(id);
            if (group != null) {
                return ResponseEntity.ok(group);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error fetching group: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}

