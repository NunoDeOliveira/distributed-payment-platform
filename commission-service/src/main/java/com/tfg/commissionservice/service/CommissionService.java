package com.tfg.commissionservice.service;

import com.tfg.commissionservice.message.CommissionPublish;
import com.tfg.commissionservice.model.Commission;
import com.tfg.commissionservice.model.CommissionMethod;
import com.tfg.commissionservice.model.CommissionState;
import com.tfg.commissionservice.repository.CommissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Service
public class CommissionService {
    // Attributes
    private final CommissionRepository commissionRepository;
    private final CommissionPublish commissionPublish;

    // Constructor
    public CommissionService(CommissionRepository commissionRepository, CommissionPublish commissionPublish) {
        this.commissionRepository = commissionRepository;
        this.commissionPublish = commissionPublish;
    }

    // Given
    @Transactional
    public void calculateCommission(String correlationId, BigDecimal amount, String paymentMethod) {
        if (correlationId == null || correlationId.isEmpty()
                || amount == null || paymentMethod == null) {
            return;
        }
        if (commissionRepository.existsByCorrelationId(correlationId)) {
            return;
        }

        CommissionMethod commissionMethod = CommissionMethod.valueOf(paymentMethod);
        BigDecimal commissionAmount = amount.multiply(commissionMethod.getCommissionRate());
        BigDecimal totalAmount = amount.add(commissionAmount);

        Commission commission = new Commission(correlationId, amount, commissionAmount,
                totalAmount, commissionMethod, CommissionState.CALCULATED, LocalDateTime.now());

        //commission.calculated();
        commissionRepository.save(commission);
        commissionPublish.publishCommissionCalculated(commission.getCorrelationId(),
                            commission.getTotalAmount(), commission.getTotalAmount(), paymentMethod);
    }

    @Transactional
    public void releaseCommission(String correlationId) {
        // Check input data
        if (correlationId == null) {
            return;
        }

        // Get commission from repository
        Commission commission = commissionRepository.findByCorrelationId(correlationId).orElse(null);
        if (commission == null) {
            return;
        }

        if (!commission.getCorrelationId().equals(correlationId) ||
                commission.getState() == CommissionState.CALCULATED ||
                commission.getState() == CommissionState.RELEASED) {
            return;
        }

        // Release commission and save the state
        commission.released();
        commissionRepository.save(commission);

        // Publish in rabbit Payment queue
        commissionPublish.publishCommissionReleased(commission.getCorrelationId());
    }

    // If the payment/operation is canceled,
    @Transactional
    public void cancelCommission(String correlationId) {
        // Check input data
        if (correlationId == null) {
            return;
        }

        // Get commission from repository
        Commission commission = commissionRepository.findByCorrelationId(correlationId).orElse(null);
        if (commission == null) {
            return;
        }

        // Check current state
        if (commission.getState() != CommissionState.CALCULATED) {
            return;
        }

        // Local compensation
        commission.cancel();
        commissionRepository.save(commission);
        commissionPublish.publishCommissionCanceled(correlationId, commission.getTotalAmount());

    }

    // Given an Id of commission get operation of commission
    public Commission getCommission(Long id) {
        return commissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commission " + id + " not found"));
    }

    public List<Commission> getAllCommissions() {
        return commissionRepository.findAll();
    }

}
