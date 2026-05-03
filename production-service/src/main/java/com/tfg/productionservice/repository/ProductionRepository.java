package com.tfg.productionservice.repository;

import com.tfg.productionservice.model.Production;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionRepository extends JpaRepository<Production, Long> {
    // Method to search a production in order by state if a production is rejected
    Optional<Production> findFirstByStateOrderByStartTimeAsc(ProductionState state);
}