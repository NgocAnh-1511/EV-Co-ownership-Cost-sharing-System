package com.example.ui_service.repository;

import com.example.ui_service.model.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    /**
     * Đếm số lượng hợp đồng của một người dùng dựa trên trạng thái cụ thể.
     * SỬA LẠI QUERY: Join qua thuộc tính 'ownerships' của Contract và 'user' của Ownership.
     * @param userId ID của người dùng.
     * @param status Trạng thái hợp đồng cần đếm (ví dụ: 'active', 'pending').
     * @return Số lượng hợp đồng khớp.
     */
    @Query("SELECT COUNT(c) FROM Contract c JOIN c.ownerships o WHERE o.user.userId = :userId AND c.status = :status")
    long countByStatusAndUser(@Param("userId") Long userId, @Param("status") String status);

    /**
     * Đếm tổng số hợp đồng của một người dùng.
     * SỬA LẠI QUERY: Join qua thuộc tính 'ownerships' và 'user'.
     * @param userId ID của người dùng.
     * @return Tổng số hợp đồng của người dùng đó.
     */
    @Query("SELECT COUNT(c) FROM Contract c JOIN c.ownerships o WHERE o.user.userId = :userId")
    long countByUser(@Param("userId") Long userId);

    /**
     * Lấy danh sách hợp đồng (bao gồm thông tin Asset liên quan) cho một người dùng,
     * có hỗ trợ phân trang và sắp xếp.
     * SỬA LẠI QUERY: Join qua thuộc tính 'ownerships' và 'user'.
     * LEFT JOIN FETCH c.asset vẫn giữ nguyên để lấy thông tin Asset.
     * @param userId ID của người dùng.
     * @param pageable Đối tượng chứa thông tin phân trang (trang số mấy, bao nhiêu mục/trang) và sắp xếp.
     * @return Một trang (Page) chứa danh sách các đối tượng Contract.
     */
    @Query("SELECT c FROM Contract c LEFT JOIN FETCH c.asset a JOIN c.ownerships o WHERE o.user.userId = :userId ORDER BY c.createdAt DESC")
    Page<Contract> findContractsByUserIdWithAsset(@Param("userId") Long userId, Pageable pageable);

}