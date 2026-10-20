package com.charity;

import com.charity.config.SecurityConfig;
import com.charity.controller.SystemController;
import com.charity.dto.HealthResponse;
import com.charity.dto.DonationRequest;
import com.charity.service.HealthService;
import javax.validation.Valid;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SystemController.class)
@Import({SecurityConfig.class, SystemControllerTest.SecurityProbe.class})
class SystemControllerTest {
    @Autowired private MockMvc mvc;
    @MockBean private HealthService healthService;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    void healthIsPublic() throws Exception {
        when(healthService.check()).thenReturn(new HealthResponse());
        mvc.perform(get("/api/health")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    void csrfIsPublic() throws Exception {
        mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.headerName").value("X-CSRF-TOKEN"))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void anonymousCannotReadAdminData() throws Exception {
        mvc.perform(get("/api/admin/test")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotReadAdminData() throws Exception {
        mvc.perform(get("/api/admin/test")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanReadProtectedRoute() throws Exception {
        mvc.perform(get("/api/admin/test")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void writeRequiresCsrfToken() throws Exception {
        mvc.perform(post("/api/test/write")).andExpect(status().isForbidden());
        mvc.perform(post("/api/test/write").with(csrf())).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void rejectsFractionalProjectId() throws Exception {
        String input = "{\"projectId\":1,\"amount\":\"1.00\",\"requestId\":\"integer-id-check\"}";
        mvc.perform(post("/api/test/donation-input").with(csrf()).contentType("application/json").content(input))
                .andExpect(status().isOk()).andExpect(jsonPath("$.projectId").value(1));
        mvc.perform(post("/api/test/donation-input").with(csrf()).contentType("application/json")
                .content(input.replace("\"projectId\":1", "\"projectId\":1.9")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    // 仅在测试上下文注册，不会进入实际应用。
    @RestController
    static class SecurityProbe {
        @GetMapping("/api/admin/test") String admin() { return "ok"; }
        @PostMapping("/api/test/write") String write() { return "ok"; }
        @PostMapping(value = "/api/test/donation-input", consumes = "application/json")
        DonationRequest donationInput(@Valid @RequestBody DonationRequest input) { return input; }
    }
}
