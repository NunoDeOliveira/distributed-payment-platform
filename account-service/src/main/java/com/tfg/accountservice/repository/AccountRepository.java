package com.tfg.accountservice.repository;

import com.tfg.accountservice.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.tfg.accountservice.model.BalanceHold;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // Query for get the total stock of stock entry
    // An stock entry represents the production received from Production Service
    @Query("SELECT SUM(stock.amount) FROM Account stock")
    Integer getTotalStock();
}
