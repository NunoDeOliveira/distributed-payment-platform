package com.tfg.delivery_service.repository;

import com.tfg.delivery_service.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tfg.delivery_service.model.DeliveryState;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findFirstByStateOrderByStartTimeAsc(DeliveryState state);
    // Method of consulting for count state
    long countByState(DeliveryState state);
}
