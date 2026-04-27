package com.nexusbank.identity_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nexusbank.identity_service.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Check if an email exists before registering a new user
    boolean existsByEmail(String email);

    // Find user by email (for login later)
    Optional<User> findByEmail(String email);
}