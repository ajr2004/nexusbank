package com.nexusbank.identity_service.controller;

import com.nexusbank.identity_service.model.User;
import com.nexusbank.identity_service.repository.UserRepository;
import com.nexusbank.identity_service.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.nexusbank.identity_service.dto.LoginRequest;
import com.nexusbank.identity_service.dto.AuthResponse;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        // Delegation: The Controller asks the Service to handle the logic
        User savedUser = userService.registerUser(user);
        return ResponseEntity.ok(savedUser);
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
       return ResponseEntity.ok(userService.login(request));
    }

    // Get all users to see their IDs for account creation
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}