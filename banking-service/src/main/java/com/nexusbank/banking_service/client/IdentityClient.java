package com.nexusbank.banking_service.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "identity-service") // 👈 Tells Eureka to find this service
public interface IdentityClient {

    @GetMapping("/api/users/exists/{userId}")
    boolean checkUserExists(@PathVariable("userId") Long userId);
}