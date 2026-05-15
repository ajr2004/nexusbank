package com.nexusbank.identity_service.controller;

import com.nexusbank.identity_service.dto.AuthResponse;
import com.nexusbank.identity_service.dto.LoginRequest;
import com.nexusbank.identity_service.dto.UserDto;
import com.nexusbank.identity_service.model.User;
import com.nexusbank.identity_service.repository.UserRepository;
import com.nexusbank.identity_service.security.JwtService;
import com.nexusbank.identity_service.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    private final UserService userService;

    private final JwtService jwtService;

    // ✅ Register User
    @PostMapping
    public ResponseEntity<User> registerUser(@RequestBody User user) {

        User savedUser = userService.registerUser(user);

        return ResponseEntity.ok(savedUser);
    }

    // ✅ Login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request) {

        return ResponseEntity.ok(userService.login(request));
    }

    // ✅ Get all users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {

        return ResponseEntity.ok(userRepository.findAll());
    }

    // ✅ Check user exists
    @GetMapping("/exists/{userId}")
    public boolean checkUserExists(
            @PathVariable Long userId) {

        return userRepository.existsById(userId);
    }

    // ✅ Get current logged-in user
    @GetMapping("/me")
    public UserDto getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);

        String email = jwtService.extractUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDto dto = new UserDto();

        dto.setId(user.getUserId());
        dto.setUsername(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());

        return dto;
    }
}