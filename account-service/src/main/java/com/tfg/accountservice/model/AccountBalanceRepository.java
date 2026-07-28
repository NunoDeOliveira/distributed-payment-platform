package com.tfg.accountservice.repository;

import com.tfg.accountservice.model.AccountBalance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountBalanceRepository extends JpaRepository<AccountBalance, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT accountBalance
        FROM AccountBalance accountBalance
        WHERE accountBalance.id = :accountId """)

    Optional<AccountBalance> findByIdForUpdate(@Param("accountId") Long accountId);
}
