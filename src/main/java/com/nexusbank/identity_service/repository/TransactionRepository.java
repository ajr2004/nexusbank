package com.nexusbank.identity_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nexusbank.identity_service.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

}