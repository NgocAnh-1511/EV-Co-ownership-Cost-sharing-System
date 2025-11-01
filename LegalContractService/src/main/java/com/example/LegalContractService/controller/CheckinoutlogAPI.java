package com.example.LegalContractService.controller;

import com.example.LegalContractService.model.Checkinoutlog;
import com.example.LegalContractService.repository.CheckinoutlogRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/checkinout")
@CrossOrigin(origins = "*")
public class CheckinoutlogAPI {

    private final CheckinoutlogRepository repository;

    public CheckinoutlogAPI(CheckinoutlogRepository repository) {
        this.repository = repository;
    }

    // ✅ Lấy tất cả bản ghi
    @GetMapping
    public List<Checkinoutlog> getAllLogs() {
        return repository.findAll();
    }

    // ✅ Lấy bản ghi theo ID
    @GetMapping("/{id}")
    public Optional<Checkinoutlog> getById(@PathVariable Long id) {
        return repository.findById(id);
    }

    // ✅ Thêm bản ghi mới
    @PostMapping
    public Checkinoutlog create(@RequestBody Checkinoutlog log) {
        return repository.save(log);
    }

    // ✅ Cập nhật bản ghi
    @PutMapping("/{id}")
    public Checkinoutlog update(@PathVariable Long id, @RequestBody Checkinoutlog log) {
        log.setCheckinoutId(id.toString());
        return repository.save(log);
    }

    // ✅ Xóa bản ghi
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
