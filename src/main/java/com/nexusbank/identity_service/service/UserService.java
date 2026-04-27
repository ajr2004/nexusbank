package com.nexusbank.identity_service.service;

import com.nexusbank.identity_service.model.User;
import com.nexusbank.identity_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import com.nexusbank.identity_service.dto.LoginRequest;
import com.nexusbank.identity_service.dto.AuthResponse;
import com.nexusbank.identity_service.security.JwtService;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public User registerUser(User user) {
        // 1. Business Logic: Prevent duplicate emails [cite: 42, 43]
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered!");
        }

        // 2. Security Logic: Hash the password using BCrypt [cite: 30, 53]
        String hashedPw = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPw);

        // 3. Metadata Setup
        user.setRole(User.Role.CUSTOMER); // Default role [cite: 13, 27]
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        // 1. Find user
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // 2. Verify Password
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // 3. Generate Token
        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

}