package com.tfg.delivery_service.repository;

import com.tfg.delivery_service.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tfg.delivery_service.model.DeliveryState;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findFirstByStateOrderByStartTimeAsc(DeliveryState state);
    // Method of consulting for count state
    long countByState(DeliveryState state);
    // Added to get all the pendings
    List<Delivery> findByStateOrderByStartTimeAsc(DeliveryState state);
    // Sum amount by state for metrics in units
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Delivery d WHERE d.state = :state")
    long sumAmountByState(@Param("state") DeliveryState state);
}
