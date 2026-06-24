package com.tfg.accountservice.repository;

import com.tfg.accountservice.model.StockEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.tfg.accountservice.model.StockReservation;

@Repository
public interface InventoryRepository extends JpaRepository<StockEntry, Long> {

    // Query for get the total stock of stock entry
    // An stock entry represents the production received from Production Service
    @Query("SELECT SUM(stock.amount) FROM StockEntry stock")
    Integer getTotalStock();
}
