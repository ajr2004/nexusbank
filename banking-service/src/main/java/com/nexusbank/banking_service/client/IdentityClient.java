package com.nexusbank.banking_service.client;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.nexusbank.banking_service.dto.UserDto;

@FeignClient(name = "identity-service")
public interface IdentityClient {

    // ✅ Check if user exists
    @GetMapping("/api/users/exists/{userId}")
    boolean checkUserExists(
            @PathVariable("userId") Long userId
    );

    // ✅ Get logged-in user from JWT
    @GetMapping("/api/users/me")
    UserDto getCurrentUser(
            @RequestHeader("Authorization") String token
    );
}