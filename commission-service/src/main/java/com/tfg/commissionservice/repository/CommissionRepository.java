package com.tfg.commissionservice.repository;

import com.tfg.commissionservice.model.Commission;
import com.tfg.commissionservice.model.CommissionState;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tfg.commissionservice.model.CommissionState;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, Long> {

    Optional<Commission> findOperationByIdAndCorrelationId(Long paymentId, String correlationId);
    long countByState(CommissionState state);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Commission p WHERE p.state = :state")
    Integer sumAmountByState(@Param("state") CommissionState state);
}
