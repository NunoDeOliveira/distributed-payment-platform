package com.tfg.productionservice.repository;

import com.tfg.productionservice.model.Production;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tfg.productionservice.model.ProductionState;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


@Repository
public interface ProductionRepository extends JpaRepository<Production, Long> {
    // Method to search a production in order by state if a production is rejected
    Optional<Production> findFirstByStateOrderByStartTimeAsc(ProductionState state);
    // Method of consulting for count state
    long countByState(ProductionState state);
    // Added to get all the pendings
    List<Production> findByStateOrderByStartTimeAsc(ProductionState state);
    // Sum amount by state 
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Production p WHERE p.state = :state")
    long sumAmountByState(@Param("state") ProductionState state);
}
