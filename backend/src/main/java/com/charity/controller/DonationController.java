package com.charity.controller;

import com.charity.dto.*;
import com.charity.service.DonationService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/donations")
public class DonationController {
    private final DonationService service;
    public DonationController(DonationService service) { this.service = service; }

    @PostMapping
    public ApiResponse<DonationResponse> donate(Authentication authentication, @Valid @RequestBody DonationRequest input) {
        return ApiResponse.success(service.donate(authentication.getName(), input));
    }

    @GetMapping("/mine")
    public ApiResponse<List<DonationResponse>> mine(Authentication authentication) {
        return ApiResponse.success(service.mine(authentication.getName()));
    }
}
