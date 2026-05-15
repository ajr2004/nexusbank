package com.nexusbank.banking_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nexusbank.banking_service.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

}