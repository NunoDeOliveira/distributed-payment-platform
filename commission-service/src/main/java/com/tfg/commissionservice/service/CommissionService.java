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
    public void calculateCommission(Long Id, String correlationId, BigDecimal amount, String paymentMethod) {

        // Convert paymentMethod received into CommissionMethod class
        CommissionMethod commissionMethod = CommissionMethod.valueOf(paymentMethod);
        // Get rate of commission
        BigDecimal commissionRate = commissionMethod.getCommissionRate();
        // Calculate commission amount
        BigDecimal commissionAmount = amount.multiply(commissionRate);
        // Calculate total amount. Commission + amount of operation
        BigDecimal totalAmount = amount.add(commissionAmount);

        // Create object commission to save in database
        Commission commission = new Commission(Id, correlationId, amount, commissionAmount, totalAmount,
                                                commissionMethod, CommissionState.CALCULATED, LocalDateTime.now());

        // Save commission apply into database
        Commission storedCommission = commissionRepository.save(commission);

        // Publish event in rabbit Account queue
        commissionPublish.publishCommissionCalculated(storedCommission.getId(),commission.getCorrelationId(),
                                                            commission.getTotalAmount(), paymentMethod);
    }

    @Transactional
    public void commissionRelease(Long paymentId, String correId) {
        // Get commission from repository
        Commission commission = commissionRepository.
                                findOperationByIdAndCorrelationId(paymentId, correId).orElse(null);
        if (commission == null) {
            return;
        }

        if (!commission.getCorrelationId().equals(correId) && commission.getState() != CommissionState.CALCULATED) {
            return;
        }

        // Release commission and save the state
        commission.released();
        commissionRepository.save(commission);

        // Publish in rabbit Payment queue
        commissionPublish.publishCommissionReleased(commission);
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
