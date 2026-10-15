package com.charity.controller;

import com.charity.dto.*;
import com.charity.service.ProjectService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService service;
    public ProjectController(ProjectService service) { this.service = service; }

    @GetMapping
    public ApiResponse<List<ProjectResponse>> list() { return ApiResponse.success(service.publicList()); }

    @GetMapping("/{id}")
    public ApiResponse<ProjectResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(service.publicDetail(id));
    }
}
