package com.nexusbank.loan_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.nexusbank.loan_service.dto.UserDto;

@FeignClient(name = "identity-service", url = "http://localhost:8080")
public interface IdentityClient {

    @GetMapping("/api/users/me")
    UserDto getCurrentUser(
            @RequestHeader("Authorization") String token);
}