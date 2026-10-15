package com.charity.config;

import com.charity.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.*;
import org.springframework.security.web.authentication.session.*;
import java.util.List;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() { return new HttpSessionCsrfTokenRepository(); }

    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy(CsrfTokenRepository csrfTokenRepository) {
        return new CompositeSessionAuthenticationStrategy(List.of(
                new ChangeSessionIdAuthenticationStrategy(), new CsrfAuthenticationStrategy(csrfTokenRepository)));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ObjectMapper mapper,
            SecurityContextRepository contextRepository, CsrfTokenRepository csrfRepository) throws Exception {
        http.csrf().csrfTokenRepository(csrfRepository).and()
                .securityContext().securityContextRepository(contextRepository).and();
        http.authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/health", "/api/auth/csrf", "/api/projects/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .antMatchers("/error").permitAll()
                .anyRequest().authenticated()
                .and().formLogin().disable()
                .httpBasic().disable()
                .logout().disable()
                .requestCache().disable()
                .exceptionHandling()
                .authenticationEntryPoint((request, response, exception) ->
                        writeError(response, mapper, 401, "UNAUTHORIZED", "请先登录"))
                .accessDeniedHandler((request, response, exception) ->
                        writeError(response, mapper, 403, "FORBIDDEN", "权限不足或 CSRF 令牌无效"));
        return http.build();
    }

    private void writeError(HttpServletResponse response, ObjectMapper mapper,
                            int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(response.getWriter(), ApiResponse.error(code, message));
    }
}
