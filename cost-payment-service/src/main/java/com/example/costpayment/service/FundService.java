package com.example.costpayment.service;

import com.example.costpayment.dto.*;
import com.example.costpayment.entity.FundTransaction;
import com.example.costpayment.entity.GroupFund;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface: Quản lý Quỹ chung
 * Phương án C: Yêu cầu rút tiền + Voting + Phê duyệt
 */
public interface FundService {

    // ========================================
    // QUẢN LÝ QUỸ
    // ========================================

    /**
     * Lấy thông tin quỹ theo groupId
     */
    Optional<GroupFund> getFundByGroupId(Integer groupId);

    /**
     * Tạo quỹ mới cho nhóm
     */
    GroupFund createFundForGroup(Integer groupId);

    /**
     * Lấy tổng quan quỹ
     */
    FundSummaryDto getFundSummary(Integer fundId);

    // ========================================
    // NẠP TIỀN (USER/ADMIN)
    // ========================================

    /**
     * Nạp tiền vào quỹ
     * - Ai cũng có thể nạp
     * - Không cần phê duyệt
     */
    FundTransaction deposit(DepositRequestDto request);

    // ========================================
    // RÚT TIỀN - USER (CẦN VOTE)
    // ========================================

    /**
     * Tạo yêu cầu rút tiền (USER)
     * - Status: Pending
     * - Cần vote để duyệt
     */
    FundTransaction createWithdrawRequest(WithdrawRequestDto request);

    /**
     * Lấy danh sách yêu cầu đang chờ duyệt
     */
    List<FundTransaction> getPendingRequests(Integer fundId);

    // ========================================
    // RÚT TIỀN - ADMIN (TRỰC TIẾP)
    // ========================================

    /**
     * Rút tiền trực tiếp (ADMIN)
     * - Không cần vote
     * - Status: Completed ngay
     */
    FundTransaction adminDirectWithdraw(WithdrawRequestDto request);

    // ========================================
    // PHÊ DUYỆT YÊU CẦU (ADMIN)
    // ========================================

    /**
     * Admin phê duyệt yêu cầu rút tiền
     * - Sau khi vote pass
     * - Hoặc Admin quyết định trực tiếp
     */
    FundTransaction approveWithdrawRequest(ApproveRequestDto request);

    /**
     * Admin từ chối yêu cầu rút tiền
     */
    FundTransaction rejectWithdrawRequest(ApproveRequestDto request);

    // ========================================
    // LỊCH SỬ GIAO DỊCH
    // ========================================

    /**
     * Lấy tất cả giao dịch của quỹ
     */
    List<FundTransaction> getAllTransactions(Integer fundId);

    /**
     * Lấy giao dịch của user
     */
    List<FundTransaction> getTransactionsByUser(Integer userId);

    /**
     * Lấy giao dịch theo loại
     */
    List<FundTransaction> getTransactionsByType(Integer fundId, String type);

    /**
     * Lấy giao dịch theo khoảng thời gian
     */
    List<FundTransaction> getTransactionsByDateRange(
        Integer fundId, LocalDateTime startDate, LocalDateTime endDate
    );

    /**
     * Lấy chi tiết giao dịch
     */
    Optional<FundTransaction> getTransactionById(Integer transactionId);

    // ========================================
    // THỐNG KÊ
    // ========================================

    /**
     * Tổng tiền nạp
     */
    Double getTotalDeposit(Integer fundId);

    /**
     * Tổng tiền rút
     */
    Double getTotalWithdraw(Integer fundId);

    /**
     * Số dư hiện tại
     */
    Double getCurrentBalance(Integer fundId);

    /**
     * Đếm số yêu cầu chờ duyệt
     */
    Long countPendingRequests(Integer fundId);
}

