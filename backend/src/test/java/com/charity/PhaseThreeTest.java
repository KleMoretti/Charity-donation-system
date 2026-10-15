package com.charity;

import com.charity.config.SecurityConfig;
import com.charity.controller.*;
import com.charity.entity.*;
import com.charity.repository.*;
import com.charity.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 第三阶段基本流程；Repository 使用替身，不代表真实 MySQL 联调通过。 */
@WebMvcTest({AuthController.class, ProjectController.class, AdminProjectController.class, SystemController.class})
@Import({SecurityConfig.class, AuthService.class, ProjectService.class, DatabaseUserDetailsService.class})
class PhaseThreeTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @MockBean private UserRepository users;
    @MockBean private ProjectRepository projects;
    @MockBean private DonationRepository donations;
    @MockBean private HealthService healthService;

    @Test
    void registerLoginCurrentUserAndLogout() throws Exception {
        AtomicReference<SysUser> saved = new AtomicReference<>();
        when(users.findByUsername(anyString())).thenAnswer(call -> Optional.ofNullable(saved.get())
                .filter(user -> user.getUsername().equals(call.getArgument(0))));
        when(users.saveAndFlush(any(SysUser.class))).thenAnswer(call -> {
            SysUser user = call.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 1L);
            saved.set(user);
            return user;
        });
        var csrfResult = mvc.perform(get("/api/auth/csrf")).andReturn();
        MockHttpSession session = (MockHttpSession) csrfResult.getRequest().getSession();
        String token = mapper.readTree(csrfResult.getResponse().getContentAsString()).at("/data/token").asText();
        String input = "{\"username\":\"phase_user\",\"password\":\"PhaseThree2026!\"}";
        mvc.perform(post("/api/auth/register").session(session).header("X-CSRF-TOKEN", token)
                .contentType("application/json").content(input)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
        mvc.perform(post("/api/auth/login").session(session).header("X-CSRF-TOKEN", token)
                .contentType("application/json").content("{\"username\":\"phase_user\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").session(session).header("X-CSRF-TOKEN", token)
                .contentType("application/json").content(input)).andExpect(status().isOk());
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("phase_user"));
        String newToken = mapper.readTree(mvc.perform(get("/api/auth/csrf").session(session)).andReturn()
                .getResponse().getContentAsString()).at("/data/token").asText();
        mvc.perform(post("/api/auth/logout").session(session).header("X-CSRF-TOKEN", newToken))
                .andExpect(status().isOk());
        assertTrue(session.isInvalid());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEditPublishBrowseAndCloseProject() throws Exception {
        AtomicReference<CharityProject> saved = new AtomicReference<>();
        when(projects.save(any(CharityProject.class))).thenAnswer(call -> {
            CharityProject project = call.getArgument(0);
            ReflectionTestUtils.setField(project, "id", 1L);
            saved.set(project);
            return project;
        });
        when(projects.findById(1L)).thenAnswer(call -> Optional.ofNullable(saved.get()));
        when(projects.findForUpdateById(1L)).thenAnswer(call -> Optional.ofNullable(saved.get()));
        when(projects.findByStatusInOrderByCreatedAtDescIdDesc(anyCollection())).thenAnswer(call ->
                saved.get() != null && saved.get().getStatus() != ProjectStatus.DRAFT ? List.of(saved.get()) : List.of());
        String input = "{\"title\":\"阅读角\",\"description\":\"为儿童提供图书\",\"targetAmount\":\"100.00\"}";
        mvc.perform(post("/api/admin/projects").with(csrf()).contentType("application/json").content(input))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.status").value("DRAFT"));
        mvc.perform(get("/api/projects/1")).andExpect(status().isNotFound());
        mvc.perform(put("/api/admin/projects/1").with(csrf()).contentType("application/json")
                .content(input.replace("阅读角", "乡村阅读角"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("乡村阅读角"));
        mvc.perform(post("/api/admin/projects/1/publish").with(csrf())).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        mvc.perform(get("/api/projects")).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].id").value(1));
        mvc.perform(get("/api/projects/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetAmount").value("100.00"));
        mvc.perform(post("/api/admin/projects/1/close").with(csrf())).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
        mvc.perform(get("/api/projects/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
        mvc.perform(post("/api/admin/projects/1/publish").with(csrf())).andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectClientRoleAndInvalidTargetAmount() throws Exception {
        mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json")
                .content("{\"username\":\"new_admin\",\"password\":\"PhaseThree2026!\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/admin/projects").with(csrf()).contentType("application/json")
                .content("{\"title\":\"公益项目\",\"description\":\"项目说明\",\"targetAmount\":\"0.001\"}"))
                .andExpect(status().isBadRequest());
        verify(users, never()).saveAndFlush(any());
        verify(projects, never()).save(any());
    }
}
