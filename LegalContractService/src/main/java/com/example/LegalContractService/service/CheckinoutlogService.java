package com.example.LegalContractService.service;

import com.example.LegalContractService.model.Checkinoutlog;
import com.example.LegalContractService.repository.CheckinoutlogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CheckinoutlogService {

    private final CheckinoutlogRepository repo;

    public CheckinoutlogService(CheckinoutlogRepository repo) {
        this.repo = repo;
    }

    public List<Checkinoutlog> getAll() {
        return repo.findAll();
    }

    public Checkinoutlog getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Checkinoutlog create(Checkinoutlog log) {
        return repo.save(log);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
