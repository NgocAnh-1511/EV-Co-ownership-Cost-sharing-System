package com.example.ui_service.repository;

import com.example.ui_service.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    // --- SỬA LẠI TÊN PHƯƠNG THỨC ---
    // Đổi "Name" thành "AssetName" để khớp với tên trường trong Asset.java
    List<Asset> findAllByOrderByAssetNameAsc();
    // --- KẾT THÚC SỬA ---
}