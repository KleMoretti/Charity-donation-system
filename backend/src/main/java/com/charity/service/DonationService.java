package com.charity.service;

import com.charity.dto.*;
import com.charity.entity.*;
import com.charity.exception.BusinessException;
import com.charity.repository.*;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DonationService {
    private final DonationRepository donations;
    private final UserRepository users;
    private final ProjectRepository projects;

    public DonationService(DonationRepository donations, UserRepository users, ProjectRepository projects) {
        this.donations = donations;
        this.users = users;
        this.projects = projects;
    }

    @Transactional
    public DonationResponse donate(String username, DonationRequest input) {
        // 同一用户的写请求先锁用户行；首次普通读取在取得锁之后，避免重试读取旧快照。
        SysUser user = users.findForUpdateByUsername(username).orElseThrow(() ->
                new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "登录状态已失效，请重新登录"));
        Donation existing = donations.findByUserIdAndRequestId(user.getId(), input.getRequestId()).orElse(null);
        if (existing != null) {
            if (!existing.getProject().getId().equals(input.getProjectId())
                    || existing.getAmount().compareTo(input.getAmount()) != 0) {
                throw new BusinessException(HttpStatus.CONFLICT, "REQUEST_ID_CONFLICT", "该提交标识已用于另一笔模拟捐款");
            }
            // 已成功的同一请求在项目结束后仍返回原记录，不再新增记账。
            return new DonationResponse(existing);
        }
        CharityProject project = projects.findForUpdateById(input.getProjectId()).orElseThrow(() ->
                new BusinessException(HttpStatus.NOT_FOUND, "PROJECT_NOT_FOUND", "项目不存在"));
        if (project.getStatus() != ProjectStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.CONFLICT, "PROJECT_NOT_ACTIVE", "仅募集中项目接受模拟捐款");
        }
        Donation donation = new Donation(user, project, input.getAmount(), input.getRequestId());
        return new DonationResponse(donations.saveAndFlush(donation));
    }

    @Transactional(readOnly = true)
    public List<DonationResponse> mine(String username) {
        return donations.findByUserUsernameOrderByCreatedAtDescIdDesc(username).stream()
                .map(DonationResponse::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DonationResponse> all() {
        return donations.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(DonationResponse::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StatsResponse stats() {
        return new StatsResponse(projects.count(), donations.summarizeAll());
    }
}
