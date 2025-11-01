package com.example.LegalContractService.repository;

import com.example.LegalContractService.model.Checkinoutlog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckinoutlogRepository extends JpaRepository<Checkinoutlog, Long> {
}
