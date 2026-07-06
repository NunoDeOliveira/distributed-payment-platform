package com.tfg.accountservice.repository;

import com.tfg.accountservice.model.Account;
import com.tfg.accountservice.model.Operation;
import com.tfg.accountservice.model.OperationState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Operation> findByCorrelationId(String correlationId);

}

