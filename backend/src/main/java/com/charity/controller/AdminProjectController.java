package com.charity.controller;

import com.charity.dto.*;
import com.charity.service.ProjectService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/projects")
public class AdminProjectController {
    private final ProjectService service;
    public AdminProjectController(ProjectService service) { this.service = service; }

    @GetMapping
    public ApiResponse<List<ProjectResponse>> list() { return ApiResponse.success(service.adminList()); }

    @GetMapping("/{id}")
    public ApiResponse<ProjectResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(service.adminDetail(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProjectResponse> create(@Valid @RequestBody ProjectRequest input) {
        return ApiResponse.success(service.create(input));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProjectResponse> edit(@PathVariable Long id, @Valid @RequestBody ProjectRequest input) {
        return ApiResponse.success(service.edit(id, input));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<ProjectResponse> publish(@PathVariable Long id) {
        return ApiResponse.success(service.publish(id));
    }

    @PostMapping("/{id}/close")
    public ApiResponse<ProjectResponse> close(@PathVariable Long id) {
        return ApiResponse.success(service.close(id));
    }
}
