package com.example.costpayment.service.impl;

import com.example.costpayment.dto.*;
import com.example.costpayment.entity.FundTransaction;
import com.example.costpayment.entity.FundTransaction.TransactionStatus;
import com.example.costpayment.entity.FundTransaction.TransactionType;
import com.example.costpayment.entity.GroupFund;
import com.example.costpayment.repository.FundTransactionRepository;
import com.example.costpayment.repository.GroupFundRepository;
import com.example.costpayment.service.FundService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Service Implementation: Quản lý Quỹ chung
 * Phương án C: Yêu cầu rút tiền + Voting + Phê duyệt
 */
@Service
public class FundServiceImpl implements FundService {

    private static final Logger logger = LoggerFactory.getLogger(FundServiceImpl.class);

    @Autowired
    private GroupFundRepository groupFundRepository;

    @Autowired
    private FundTransactionRepository transactionRepository;

    // ========================================
    // QUẢN LÝ QUỸ
    // ========================================

    @Override
    public Optional<GroupFund> getFundByGroupId(Integer groupId) {
        return groupFundRepository.findByGroupId(groupId);
    }

    @Override
    @Transactional
    public GroupFund createFundForGroup(Integer groupId) {
        // Kiểm tra đã tồn tại chưa
        Optional<GroupFund> existing = groupFundRepository.findByGroupId(groupId);
        if (existing.isPresent()) {
            logger.warn("Fund already exists for groupId={}", groupId);
            return existing.get();
        }

        GroupFund fund = new GroupFund();
        fund.setGroupId(groupId);
        fund.setTotalContributed(0.0);
        fund.setCurrentBalance(0.0);
        fund.setUpdatedAt(LocalDateTime.now());
        fund.setNote("Quỹ chung nhóm " + groupId);

        GroupFund saved = groupFundRepository.save(fund);
        logger.info("Created fund for groupId={}, fundId={}", groupId, saved.getFundId());
        return saved;
    }

    @Override
    public FundSummaryDto getFundSummary(Integer fundId) {
        GroupFund fund = groupFundRepository.findById(fundId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy quỹ với ID: " + fundId));

        Long pendingCount = transactionRepository.countPendingTransactions(fundId);
        Double totalDeposit = transactionRepository.getTotalDeposit(fundId);
        Double totalWithdraw = transactionRepository.getTotalWithdraw(fundId);

        FundSummaryDto summary = new FundSummaryDto();
        summary.setFundId(fund.getFundId());
        summary.setGroupId(fund.getGroupId());
        summary.setTotalContributed(fund.getTotalContributed());
        summary.setCurrentBalance(fund.getCurrentBalance());
        summary.setTotalDeposit(totalDeposit);
        summary.setTotalWithdraw(totalWithdraw);
        summary.setPendingRequests(pendingCount);
        summary.setUpdatedAt(fund.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return summary;
    }

    // ========================================
    // NẠP TIỀN (USER/ADMIN)
    // ========================================

    @Override
    @Transactional
    public FundTransaction deposit(DepositRequestDto request) {
        // Validate
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("Số tiền nạp phải > 0");
        }

        // Lấy quỹ
        GroupFund fund = groupFundRepository.findById(request.getFundId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy quỹ"));

        // Tạo giao dịch
        FundTransaction transaction = FundTransaction.createDeposit(
            request.getFundId(),
            request.getUserId(),
            request.getAmount(),
            request.getPurpose() != null ? request.getPurpose() : "Nạp tiền vào quỹ"
        );

        // Cập nhật quỹ
        fund.deposit(request.getAmount());
        groupFundRepository.save(fund);

        // Lưu giao dịch
        FundTransaction saved = transactionRepository.save(transaction);
        logger.info("Deposit: userId={}, amount={}, fundId={}", 
            request.getUserId(), request.getAmount(), request.getFundId());

        return saved;
    }

    // ========================================
    // RÚT TIỀN - USER (CẦN VOTE)
    // ========================================

    @Override
    @Transactional
    public FundTransaction createWithdrawRequest(WithdrawRequestDto request) {
        // Validate
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("Số tiền rút phải > 0");
        }

        // Kiểm tra số dư
        GroupFund fund = groupFundRepository.findById(request.getFundId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy quỹ"));

        if (!fund.hasSufficientBalance(request.getAmount())) {
            throw new IllegalStateException(
                String.format("Số dư không đủ. Hiện có: %.2f VND, yêu cầu: %.2f VND",
                    fund.getCurrentBalance(), request.getAmount())
            );
        }

        // Tạo yêu cầu (status = Pending)
        FundTransaction transaction = FundTransaction.createWithdrawRequest(
            request.getFundId(),
            request.getUserId(),
            request.getAmount(),
            request.getPurpose()
        );
        transaction.setReceiptUrl(request.getReceiptUrl());

        FundTransaction saved = transactionRepository.save(transaction);
        logger.info("Withdraw request created: userId={}, amount={}, transactionId={}", 
            request.getUserId(), request.getAmount(), saved.getTransactionId());

        return saved;
    }

    @Override
    public List<FundTransaction> getPendingRequests(Integer fundId) {
        return transactionRepository.findPendingWithdrawRequests(fundId);
    }

    // ========================================
    // RÚT TIỀN - ADMIN (TRỰC TIẾP)
    // ========================================

    @Override
    @Transactional
    public FundTransaction adminDirectWithdraw(WithdrawRequestDto request) {
        // Validate
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("Số tiền rút phải > 0");
        }

        // Lấy quỹ
        GroupFund fund = groupFundRepository.findById(request.getFundId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy quỹ"));

        // Kiểm tra số dư
        if (!fund.hasSufficientBalance(request.getAmount())) {
            throw new IllegalStateException(
                String.format("Số dư không đủ. Hiện có: %.2f VND, yêu cầu: %.2f VND",
                    fund.getCurrentBalance(), request.getAmount())
            );
        }

        // Tạo giao dịch (status = Completed)
        FundTransaction transaction = FundTransaction.createDirectWithdraw(
            request.getFundId(),
            request.getUserId(), // adminId
            request.getAmount(),
            request.getPurpose()
        );
        transaction.setReceiptUrl(request.getReceiptUrl());

        // Cập nhật quỹ
        fund.withdraw(request.getAmount());
        groupFundRepository.save(fund);

        // Lưu giao dịch
        FundTransaction saved = transactionRepository.save(transaction);
        logger.info("Admin direct withdraw: adminId={}, amount={}, fundId={}", 
            request.getUserId(), request.getAmount(), request.getFundId());

        return saved;
    }

    // ========================================
    // PHÊ DUYỆT YÊU CẦU (ADMIN)
    // ========================================

    @Override
    @Transactional
    public FundTransaction approveWithdrawRequest(ApproveRequestDto request) {
        // Lấy giao dịch
        FundTransaction transaction = transactionRepository.findById(request.getTransactionId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));

        // Kiểm tra trạng thái
        if (transaction.getStatus() != TransactionStatus.Pending 
            && transaction.getStatus() != TransactionStatus.Approved) {
            throw new IllegalStateException("Chỉ có thể duyệt giao dịch đang Pending hoặc Approved");
        }

        // Lấy quỹ
        GroupFund fund = groupFundRepository.findById(transaction.getFundId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy quỹ"));

        // Kiểm tra số dư
        if (!fund.hasSufficientBalance(transaction.getAmount())) {
            throw new IllegalStateException("Số dư không đủ để thực hiện giao dịch này");
        }

        // Phê duyệt
        transaction.approve(request.getAdminId());
        transaction.complete(); // Chuyển sang Completed

        // Cập nhật quỹ
        fund.withdraw(transaction.getAmount());
        groupFundRepository.save(fund);

        // Lưu giao dịch
        FundTransaction saved = transactionRepository.save(transaction);
        logger.info("Transaction approved: transactionId={}, adminId={}, amount={}", 
            transaction.getTransactionId(), request.getAdminId(), transaction.getAmount());

        return saved;
    }

    @Override
    @Transactional
    public FundTransaction rejectWithdrawRequest(ApproveRequestDto request) {
        // Lấy giao dịch
        FundTransaction transaction = transactionRepository.findById(request.getTransactionId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));

        // Từ chối
        transaction.reject(request.getAdminId());
        if (request.getNote() != null) {
            transaction.setPurpose(transaction.getPurpose() + " [Từ chối: " + request.getNote() + "]");
        }

        FundTransaction saved = transactionRepository.save(transaction);
        logger.info("Transaction rejected: transactionId={}, adminId={}", 
            transaction.getTransactionId(), request.getAdminId());

        return saved;
    }

    // ========================================
    // LỊCH SỬ GIAO DỊCH
    // ========================================

    @Override
    public List<FundTransaction> getAllTransactions(Integer fundId) {
        return transactionRepository.findByFundIdOrderByDateDesc(fundId);
    }

    @Override
    public List<FundTransaction> getTransactionsByUser(Integer userId) {
        return transactionRepository.findByUserIdOrderByDateDesc(userId);
    }

    @Override
    public List<FundTransaction> getTransactionsByType(Integer fundId, String type) {
        TransactionType transactionType = TransactionType.valueOf(type);
        return transactionRepository.findByFundIdAndTransactionTypeOrderByDateDesc(fundId, transactionType);
    }

    @Override
    public List<FundTransaction> getTransactionsByDateRange(
        Integer fundId, LocalDateTime startDate, LocalDateTime endDate
    ) {
        return transactionRepository.findByDateRange(fundId, startDate, endDate);
    }

    @Override
    public Optional<FundTransaction> getTransactionById(Integer transactionId) {
        return transactionRepository.findById(transactionId);
    }

    // ========================================
    // THỐNG KÊ
    // ========================================

    @Override
    public Double getTotalDeposit(Integer fundId) {
        return transactionRepository.getTotalDeposit(fundId);
    }

    @Override
    public Double getTotalWithdraw(Integer fundId) {
        return transactionRepository.getTotalWithdraw(fundId);
    }

    @Override
    public Double getCurrentBalance(Integer fundId) {
        GroupFund fund = groupFundRepository.findById(fundId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy quỹ"));
        return fund.getCurrentBalance();
    }

    @Override
    public Long countPendingRequests(Integer fundId) {
        return transactionRepository.countPendingTransactions(fundId);
    }
}

