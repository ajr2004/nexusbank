package com.nexusbank.banking_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nexusbank.banking_service.model.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // Find all accounts belonging to a specific user
    List<Account> findByUserId(Long userId);

    // Find account by account number
    Optional<Account> findByAccountNumber(String accountNumber);
}