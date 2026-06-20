package com.nexusbank.identity_service.controller;

import com.nexusbank.identity_service.model.User;
import com.nexusbank.identity_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/admin/user-management")
@RequiredArgsConstructor
public class AdminUserManagementController {

    private final UserRepository userRepository;

    @Value("${super.admin.key:NexusSuperSecretKey2026}")
    private String superAdminKey;

    @GetMapping("/validate-super-key")
    public ResponseEntity<Boolean> validateSuperKey(@RequestParam String key) {
        return ResponseEntity.ok(superAdminKey.equals(key));
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        // Returns users list directly to feed into your management dashboards
        return ResponseEntity.ok(userRepository.findAll());
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Target user account entry not found"));

        userRepository.delete(user);
        return ResponseEntity.ok(Map.of("message", "User account profile wiped from security registry successfully."));
    }
}