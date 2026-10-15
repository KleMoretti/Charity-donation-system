package com.charity.controller;

import com.charity.dto.*;
import com.charity.service.HealthService;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SystemController {
    private final HealthService healthService;

    public SystemController(HealthService healthService) { this.healthService = healthService; }

    @GetMapping("/health")
    public ApiResponse<HealthResponse> health() {
        return ApiResponse.success(healthService.check());
    }

    @GetMapping("/auth/csrf")
    public ApiResponse<CsrfResponse> csrf(CsrfToken token) {
        return ApiResponse.success(new CsrfResponse(token));
    }
}
