package com.example.costpayment.repository;

import com.example.costpayment.entity.Cost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CostRepository extends JpaRepository<Cost, Integer> {
    List<Cost> findByVehicleId(Integer vehicleId);
    List<Cost> findByCostType(Cost.CostType costType);
}
