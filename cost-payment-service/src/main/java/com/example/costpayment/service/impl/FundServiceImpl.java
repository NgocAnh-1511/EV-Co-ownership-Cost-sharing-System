package com.example.costpayment.service.impl;

import com.example.costpayment.dto.*;
import com.example.costpayment.entity.FundTransaction;
import com.example.costpayment.entity.FundTransaction.TransactionStatus;
import com.example.costpayment.entity.FundTransaction.TransactionType;
import com.example.costpayment.entity.GroupFund;
import com.example.costpayment.entity.TransactionVote;
import com.example.costpayment.repository.FundTransactionRepository;
import com.example.costpayment.repository.GroupFundRepository;
import com.example.costpayment.repository.TransactionVoteRepository;
import com.example.costpayment.service.FundService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private TransactionVoteRepository voteRepository;

    @Value("${group-management.service.url:http://localhost:8082}")
    private String groupManagementServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

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

        // Kiểm tra trạng thái: CHỈ approve khi đã có > 50% đồng ý (status = Approved)
        if (transaction.getStatus() != TransactionStatus.Approved) {
            throw new IllegalStateException(
                "Chỉ có thể phê duyệt yêu cầu khi đã có > 50% thành viên đồng ý. " +
                "Hiện tại trạng thái: " + transaction.getStatus());
        }

        // Lấy quỹ
        GroupFund fund = groupFundRepository.findById(transaction.getFundId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy quỹ"));

        // Kiểm tra số dư
        if (!fund.hasSufficientBalance(transaction.getAmount())) {
            throw new IllegalStateException("Số dư không đủ để thực hiện giao dịch này");
        }

        // Phê duyệt và hoàn tất
        transaction.setApprovedBy(request.getAdminId());
        transaction.setApprovedAt(LocalDateTime.now());
        transaction.complete(); // Chuyển sang Completed

        // Cập nhật quỹ
        fund.withdraw(transaction.getAmount());
        groupFundRepository.save(fund);

        // Lưu giao dịch
        FundTransaction saved = transactionRepository.save(transaction);
        logger.info("Transaction approved by admin: transactionId={}, adminId={}, amount={}", 
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

    @Override
    @Transactional
    public FundTransaction cancelWithdrawRequest(Integer transactionId, Integer userId) {
        // Lấy giao dịch
        FundTransaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));

        // Kiểm tra trạng thái
        if (transaction.getStatus() != TransactionStatus.Pending) {
            throw new IllegalStateException("Chỉ có thể hủy yêu cầu đang ở trạng thái Pending");
        }

        // Kiểm tra quyền: chỉ người tạo yêu cầu mới có thể hủy
        if (!transaction.getUserId().equals(userId)) {
            throw new IllegalStateException("Bạn không có quyền hủy yêu cầu này");
        }

        // Kiểm tra loại giao dịch: chỉ có thể hủy withdrawal request
        if (transaction.getTransactionType() != TransactionType.Withdraw) {
            throw new IllegalStateException("Chỉ có thể hủy yêu cầu rút tiền");
        }

        // Hủy yêu cầu (reject với lý do "User hủy yêu cầu")
        transaction.reject(userId);
        transaction.setPurpose(transaction.getPurpose() + " [Đã hủy bởi người yêu cầu]");

        FundTransaction saved = transactionRepository.save(transaction);
        logger.info("Transaction cancelled by user: transactionId={}, userId={}", 
            transactionId, userId);

        return saved;
    }

    // ========================================
    // USER VOTE CHO WITHDRAWAL REQUEST
    // ========================================

    @Override
    @Transactional
    public FundTransaction voteOnWithdrawRequest(VoteRequestDto request) {
        // Lấy giao dịch
        FundTransaction transaction = transactionRepository.findById(request.getTransactionId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));

        // Kiểm tra trạng thái
        if (transaction.getStatus() != TransactionStatus.Pending) {
            throw new IllegalStateException("Chỉ có thể vote cho giao dịch đang Pending");
        }

        // Kiểm tra không phải là người tạo request
        if (transaction.getUserId().equals(request.getUserId())) {
            throw new IllegalStateException("Bạn không thể vote cho yêu cầu của chính mình");
        }

        // Kiểm tra đã vote chưa
        Optional<TransactionVote> existingVote = voteRepository.findByTransactionIdAndUserId(
            request.getTransactionId(), request.getUserId());
        if (existingVote.isPresent()) {
            throw new IllegalStateException("Bạn đã vote cho yêu cầu này rồi");
        }

        // Lưu vote
        TransactionVote vote = new TransactionVote();
        vote.setTransactionId(request.getTransactionId());
        vote.setUserId(request.getUserId());
        vote.setApprove(request.getApprove());
        vote.setNote(request.getNote());
        vote.setVotedAt(LocalDateTime.now());
        voteRepository.save(vote);
        logger.info("User voted {}: transactionId={}, userId={}", 
            request.getApprove() ? "approve" : "reject",
            request.getTransactionId(), request.getUserId());

        // Nếu vote reject, kiểm tra xem có cần từ chối không
        if (!request.getApprove()) {
            // Đếm số phiếu từ chối
            long rejectCount = voteRepository.countRejectsByTransactionId(request.getTransactionId());
            
            // Lấy số thành viên nhóm
            GroupFund fund = groupFundRepository.findById(transaction.getFundId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quỹ"));
            
            int totalMembers = getGroupMemberCount(fund.getGroupId());
            if (totalMembers <= 0) {
                totalMembers = 1;
            }
            
            int eligibleVoters = totalMembers - 1; // Trừ người tạo request
            if (eligibleVoters <= 0) {
                eligibleVoters = 1;
            }
            
            // Nếu > 50% từ chối, từ chối ngay
            double rejectRate = (double) rejectCount / eligibleVoters;
            if (rejectRate > 0.5) {
                transaction.setStatus(TransactionStatus.Rejected);
                if (request.getNote() != null) {
                    transaction.setPurpose(transaction.getPurpose() + " [Từ chối bởi User #" + request.getUserId() + ": " + request.getNote() + "]");
                }
                transactionRepository.save(transaction);
                logger.info("Transaction {} rejected due to >50% reject votes", request.getTransactionId());
            }
            return transaction;
        }

        // Nếu vote approve, kiểm tra xem có đạt > 50% đồng ý không
        try {
            // Lấy số thành viên nhóm
            GroupFund fund = groupFundRepository.findById(transaction.getFundId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quỹ"));
            
            int totalMembers = getGroupMemberCount(fund.getGroupId());
            if (totalMembers <= 0) {
                logger.warn("Cannot get member count for groupId={}, assuming 1", fund.getGroupId());
                totalMembers = 1;
            }

            // Đếm số phiếu đồng ý
            long approveCount = voteRepository.countApprovesByTransactionId(request.getTransactionId());
            
            // Tính tỷ lệ: approveCount / (totalMembers - 1) vì trừ người tạo request
            int eligibleVoters = totalMembers - 1; // Trừ người tạo request
            if (eligibleVoters <= 0) {
                eligibleVoters = 1; // Fallback
            }

            double approvalRate = (double) approveCount / eligibleVoters;
            logger.info("Vote stats: approveCount={}, eligibleVoters={}, approvalRate={}%", 
                approveCount, eligibleVoters, String.format("%.2f", approvalRate * 100));

            // Nếu > 50% đồng ý, chuyển sang Approved (chờ admin xác nhận)
            if (approvalRate > 0.5) {
                // Chuyển sang Approved nhưng không set approvedBy (sẽ set khi admin approve)
                transaction.setStatus(TransactionStatus.Approved);
                transactionRepository.save(transaction);
                logger.info("Transaction {} reached >50% approval, status changed to Approved", 
                    request.getTransactionId());
            }
        } catch (Exception e) {
            logger.error("Error checking approval rate for transaction {}: {}", 
                request.getTransactionId(), e.getMessage(), e);
            // Tiếp tục với status Pending nếu có lỗi
        }

        return transaction;
    }

    /**
     * Lấy số thành viên trong nhóm từ group-management-service
     */
    private int getGroupMemberCount(Integer groupId) {
        try {
            String url = groupManagementServiceUrl + "/api/groups/" + groupId + "/members";
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            
            if (response.getBody() != null) {
                return response.getBody().size();
            }
            return 0;
        } catch (Exception e) {
            logger.error("Error getting member count for groupId={}: {}", groupId, e.getMessage());
            return 0;
        }
    }

    @Override
    public List<FundTransaction> getPendingVoteRequestsForUser(Integer userId) {
        // Lấy tất cả pending withdrawal requests từ các funds mà user tham gia
        // Note: Cần lấy từ các nhóm mà user là member
        // Tạm thời trả về tất cả pending withdrawal requests (sẽ filter ở frontend hoặc cần thêm logic)
        return transactionRepository.findPendingWithdrawRequests(null); // null = tất cả funds
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

