package com.example.disputemanagement.service;

import com.example.disputemanagement.entity.*;
import com.example.disputemanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DisputeService {
    
    @Autowired
    private DisputeRepository disputeRepository;
    
    @Autowired
    private DisputeCommentRepository commentRepository;
    
    @Autowired
    private DisputeResolutionRepository resolutionRepository;
    
    @Autowired
    private DisputeHistoryRepository historyRepository;
    
    @Autowired
    private DisputeAttachmentRepository attachmentRepository;
    
    // ========== DISPUTE METHODS ==========
    
    public List<Dispute> getAllDisputes() {
        return disputeRepository.findAll();
    }
    
    public Optional<Dispute> getDisputeById(Integer disputeId) {
        return disputeRepository.findById(disputeId);
    }
    
    public List<Dispute> getDisputesByGroup(Integer groupId) {
        return disputeRepository.findByGroupId(groupId);
    }
    
    public List<Dispute> getDisputesByStatus(Dispute.DisputeStatus status) {
        return disputeRepository.findByStatus(status);
    }
    
    public List<Dispute> getDisputesByPriority(Dispute.DisputePriority priority) {
        return disputeRepository.findByPriority(priority);
    }
    
    public List<Dispute> getUnassignedDisputes() {
        return disputeRepository.findUnassignedDisputes();
    }
    
    public List<Dispute> getPendingDisputesOrderedByPriority() {
        return disputeRepository.findPendingDisputesOrderedByPriority();
    }
    
    @Transactional
    public Dispute createDispute(Dispute dispute) {
        dispute.setStatus(Dispute.DisputeStatus.PENDING);
        dispute.setCreatedAt(LocalDateTime.now());
        dispute.setUpdatedAt(LocalDateTime.now());
        
        Dispute saved = disputeRepository.save(dispute);
        
        // Tạo lịch sử
        createHistory(saved.getDisputeId(), dispute.getCreatedBy(), null, 
                     dispute.getStatus().name(), "Tạo tranh chấp mới");
        
        return saved;
    }
    
    @Transactional
    public Dispute updateDispute(Integer disputeId, Dispute disputeUpdate) {
        Dispute existing = disputeRepository.findById(disputeId)
            .orElseThrow(() -> new RuntimeException("Dispute not found: " + disputeId));
        
        String oldStatus = existing.getStatus().name();
        
        // Cập nhật các trường
        if (disputeUpdate.getTitle() != null) existing.setTitle(disputeUpdate.getTitle());
        if (disputeUpdate.getDescription() != null) existing.setDescription(disputeUpdate.getDescription());
        if (disputeUpdate.getCategory() != null) existing.setCategory(disputeUpdate.getCategory());
        if (disputeUpdate.getStatus() != null) existing.setStatus(disputeUpdate.getStatus());
        if (disputeUpdate.getPriority() != null) existing.setPriority(disputeUpdate.getPriority());
        if (disputeUpdate.getAssignedTo() != null) existing.setAssignedTo(disputeUpdate.getAssignedTo());
        if (disputeUpdate.getResolutionNote() != null) existing.setResolutionNote(disputeUpdate.getResolutionNote());
        
        existing.setUpdatedAt(LocalDateTime.now());
        
        // Nếu trạng thái thay đổi, tạo lịch sử
        if (!oldStatus.equals(existing.getStatus().name())) {
            createHistory(disputeId, existing.getAssignedTo() != null ? existing.getAssignedTo() : existing.getCreatedBy(),
                         oldStatus, existing.getStatus().name(), "Cập nhật trạng thái");
            
            // Nếu đã giải quyết, set resolvedAt
            if (existing.getStatus() == Dispute.DisputeStatus.RESOLVED) {
                existing.setResolvedAt(LocalDateTime.now());
                existing.setResolvedBy(existing.getAssignedTo());
            }
            
            // Nếu đã đóng, set closedAt
            if (existing.getStatus() == Dispute.DisputeStatus.CLOSED) {
                existing.setClosedAt(LocalDateTime.now());
            }
        }
        
        return disputeRepository.save(existing);
    }
    
    @Transactional
    public Dispute assignDispute(Integer disputeId, Integer staffId) {
        Dispute dispute = disputeRepository.findById(disputeId)
            .orElseThrow(() -> new RuntimeException("Dispute not found: " + disputeId));
        
        String oldStatus = dispute.getStatus().name();
        dispute.setAssignedTo(staffId);
        
        if (dispute.getStatus() == Dispute.DisputeStatus.PENDING) {
            dispute.setStatus(Dispute.DisputeStatus.IN_REVIEW);
        }
        
        dispute.setUpdatedAt(LocalDateTime.now());
        
        createHistory(disputeId, staffId, oldStatus, dispute.getStatus().name(), 
                     "Giao tranh chấp cho staff: " + staffId);
        
        return disputeRepository.save(dispute);
    }
    
    @Transactional
    public void deleteDispute(Integer disputeId) {
        disputeRepository.deleteById(disputeId);
    }
    
    // ========== COMMENT METHODS ==========
    
    public List<DisputeComment> getCommentsByDispute(Integer disputeId, Boolean includeInternal) {
        if (includeInternal != null && includeInternal) {
            return commentRepository.findByDispute_DisputeIdOrderByCreatedAtAsc(disputeId);
        }
        return commentRepository.findByDispute_DisputeIdAndIsInternalFalseOrderByCreatedAtAsc(disputeId);
    }
    
    @Transactional
    public DisputeComment addComment(Integer disputeId, DisputeComment comment) {
        Dispute dispute = disputeRepository.findById(disputeId)
            .orElseThrow(() -> new RuntimeException("Dispute not found: " + disputeId));
        
        comment.setDispute(dispute);
        comment.setCreatedAt(LocalDateTime.now());
        
        return commentRepository.save(comment);
    }
    
    @Transactional
    public void deleteComment(Integer commentId) {
        commentRepository.deleteById(commentId);
    }
    
    // ========== RESOLUTION METHODS ==========
    
    @Transactional
    public DisputeResolution createResolution(Integer disputeId, DisputeResolution resolution) {
        Dispute dispute = disputeRepository.findById(disputeId)
            .orElseThrow(() -> new RuntimeException("Dispute not found: " + disputeId));
        
        // Kiểm tra xem đã có resolution chưa
        Optional<DisputeResolution> existing = resolutionRepository.findByDispute_DisputeId(disputeId);
        if (existing.isPresent()) {
            throw new RuntimeException("Resolution already exists for dispute: " + disputeId);
        }
        
        resolution.setDispute(dispute);
        resolution.setCreatedAt(LocalDateTime.now());
        
        DisputeResolution saved = resolutionRepository.save(resolution);
        
        // Cập nhật trạng thái dispute
        dispute.setStatus(Dispute.DisputeStatus.RESOLVED);
        dispute.setResolvedBy(resolution.getResolvedBy());
        dispute.setResolvedAt(LocalDateTime.now());
        dispute.setUpdatedAt(LocalDateTime.now());
        disputeRepository.save(dispute);
        
        // Tạo lịch sử
        createHistory(disputeId, resolution.getResolvedBy(), 
                     Dispute.DisputeStatus.IN_REVIEW.name(), 
                     Dispute.DisputeStatus.RESOLVED.name(), 
                     "Đã giải quyết: " + resolution.getResolutionType());
        
        return saved;
    }
    
    public Optional<DisputeResolution> getResolutionByDispute(Integer disputeId) {
        return resolutionRepository.findByDispute_DisputeId(disputeId);
    }
    
    // ========== HISTORY METHODS ==========
    
    public List<DisputeHistory> getHistoryByDispute(Integer disputeId) {
        return historyRepository.findByDispute_DisputeIdOrderByCreatedAtAsc(disputeId);
    }
    
    @Transactional
    private void createHistory(Integer disputeId, Integer changedBy, 
                              String oldStatus, String newStatus, String note) {
        DisputeHistory history = new DisputeHistory();
        Dispute dispute = disputeRepository.findById(disputeId)
            .orElseThrow(() -> new RuntimeException("Dispute not found: " + disputeId));
        
        history.setDispute(dispute);
        history.setChangedBy(changedBy);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangeNote(note);
        history.setCreatedAt(LocalDateTime.now());
        
        historyRepository.save(history);
    }
    
    // ========== ATTACHMENT METHODS ==========
    
    public List<DisputeAttachment> getAttachmentsByDispute(Integer disputeId) {
        return attachmentRepository.findByDispute_DisputeId(disputeId);
    }
    
    @Transactional
    public DisputeAttachment addAttachment(DisputeAttachment attachment) {
        attachment.setUploadedAt(LocalDateTime.now());
        return attachmentRepository.save(attachment);
    }
    
    // ========== STATISTICS METHODS ==========
    
    public long countByStatus(Dispute.DisputeStatus status) {
        return disputeRepository.countByStatus(status);
    }
    
    public long countByPriority(Dispute.DisputePriority priority) {
        return disputeRepository.countByPriority(priority);
    }
}

