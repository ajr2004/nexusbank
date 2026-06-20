package com.nexusbank.banking_service.dto;

public record UserDto(

    Long id,
    String username,
    String email,
    String role

) {}