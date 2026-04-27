package com.nexusbank.identity_service.dto;

public record AuthResponse(
    String token, 
    String email, 
    String role
) {}