package com.charity.service;

import com.charity.dto.*;
import com.charity.entity.*;
import com.charity.exception.BusinessException;
import com.charity.repository.ProjectRepository;
import com.charity.repository.DonationRepository;
import com.charity.repository.ProjectDonationSummary;
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {
    private static final List<ProjectStatus> PUBLIC_STATUSES = List.of(ProjectStatus.ACTIVE, ProjectStatus.CLOSED);
    private final ProjectRepository projects;
    private final DonationRepository donations;

    public ProjectService(ProjectRepository projects, DonationRepository donations) {
        this.projects = projects;
        this.donations = donations;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> publicList() {
        return responses(projects.findByStatusInOrderByCreatedAtDescIdDesc(PUBLIC_STATUSES));
    }

    @Transactional(readOnly = true)
    public ProjectResponse publicDetail(Long id) {
        CharityProject project = projects.findById(id)
                .filter(value -> PUBLIC_STATUSES.contains(value.getStatus())).orElseThrow(this::notFound);
        return response(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> adminList() {
        return responses(projects.findAllByOrderByCreatedAtDescIdDesc());
    }

    @Transactional(readOnly = true)
    public ProjectResponse adminDetail(Long id) {
        return response(projects.findById(id).orElseThrow(this::notFound));
    }

    @Transactional
    public ProjectResponse create(ProjectRequest input) {
        return new ProjectResponse(projects.save(new CharityProject(
                input.getTitle().trim(), input.getDescription().trim(), input.getTargetAmount())));
    }

    @Transactional
    public ProjectResponse edit(Long id, ProjectRequest input) {
        CharityProject project = projects.findForUpdateById(id).orElseThrow(this::notFound);
        project.edit(input.getTitle().trim(), input.getDescription().trim(), input.getTargetAmount());
        return response(project);
    }

    @Transactional
    public ProjectResponse publish(Long id) {
        CharityProject project = projects.findForUpdateById(id).orElseThrow(this::notFound);
        if (project.getStatus() != ProjectStatus.DRAFT) throw invalidState("仅草稿项目可以发布");
        project.publish();
        return response(project);
    }

    @Transactional
    public ProjectResponse close(Long id) {
        CharityProject project = projects.findForUpdateById(id).orElseThrow(this::notFound);
        if (project.getStatus() != ProjectStatus.ACTIVE) throw invalidState("仅募集中项目可以结束");
        project.close();
        return response(project);
    }

    private ProjectResponse response(CharityProject project) { return responses(List.of(project)).get(0); }

    private List<ProjectResponse> responses(List<CharityProject> values) {
        if (values.isEmpty()) return List.of();
        Map<Long, ProjectDonationSummary> summaries = donations.summarizeProjects(values.stream()
                .map(CharityProject::getId).collect(Collectors.toList())).stream()
                .collect(Collectors.toMap(ProjectDonationSummary::getProjectId, value -> value));
        return values.stream().map(project -> {
            ProjectDonationSummary summary = summaries.get(project.getId());
            return new ProjectResponse(project, summary == null ? BigDecimal.ZERO : summary.getRaisedAmount(),
                    summary == null ? 0 : summary.getDonationCount());
        }).collect(Collectors.toList());
    }

    private BusinessException notFound() {
        return new BusinessException(HttpStatus.NOT_FOUND, "PROJECT_NOT_FOUND", "项目不存在或尚未公开");
    }

    private BusinessException invalidState(String message) {
        return new BusinessException(HttpStatus.CONFLICT, "INVALID_PROJECT_STATUS", message);
    }
}
