package com.tfg.transferservice.repository;

import com.tfg.transferservice.model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tfg.transferservice.model.TransferState;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {
    // Method to search a production in order by state if a production is rejected
    Optional<Transfer> findFirstByStateOrderByStartTimeAsc(TransferState state);
    // Method of consulting for count state
    long countByState(TransferState state);
    // Added to get all the pendings
    List<Transfer> findByStateOrderByStartTimeAsc(TransferState state);
    // Sum amount by state 
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Transfer p WHERE p.state = :state")
    long sumAmountByState(@Param("state") TransferState state);
}
