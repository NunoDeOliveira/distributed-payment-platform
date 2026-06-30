package com.tfg.paymentservice.repository;

import com.tfg.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tfg.paymentservice.model.PaymentState;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Method to search a payment in order by state if a payment is rejected
    Optional<Payment> findFirstByStateOrderByStartTimeAsc(PaymentState state);
    // Method of consulting for count state
    long countByState(PaymentState state);
    // Added to get all the pendings
    List<Payment> findByStateOrderByStartTimeAsc(PaymentState state);
    // Sum amount by state 
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.state = :state")
    long sumAmountByState(@Param("state") PaymentState state);
}
