package com.nexusbank.identity_service.service;

import com.nexusbank.identity_service.model.User;
import com.nexusbank.identity_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Look for the user in the database by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 2. Return a Spring Security 'UserDetails' object
        // This tells Spring: "Here is the user, and here is their hashed password to compare against."
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword()) // The BCrypt hash from your DB
                .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name()))// CUSTOMER or ADMIN [cite: 41]
                .build();
    }
}