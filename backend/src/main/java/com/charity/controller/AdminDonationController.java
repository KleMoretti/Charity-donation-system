package com.charity.controller;

import com.charity.dto.*;
import com.charity.service.DonationService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminDonationController {
    private final DonationService service;
    public AdminDonationController(DonationService service) { this.service = service; }

    @GetMapping("/donations")
    public ApiResponse<List<DonationResponse>> donations() { return ApiResponse.success(service.all()); }

    @GetMapping("/stats")
    public ApiResponse<StatsResponse> stats() { return ApiResponse.success(service.stats()); }
}
