package com.charity.controller;

import com.charity.dto.*;
import com.charity.exception.BusinessException;
import com.charity.service.AuthService;
import javax.servlet.http.*;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;
    private final AuthenticationManager authenticationManager;
    private final SessionAuthenticationStrategy sessionStrategy;
    private final SecurityContextRepository contextRepository;
    private final CsrfTokenRepository csrfRepository;

    public AuthController(AuthService service, AuthenticationManager authenticationManager,
            SessionAuthenticationStrategy sessionStrategy, SecurityContextRepository contextRepository,
            CsrfTokenRepository csrfRepository) {
        this.service = service;
        this.authenticationManager = authenticationManager;
        this.sessionStrategy = sessionStrategy;
        this.contextRepository = contextRepository;
        this.csrfRepository = csrfRepository;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest input) {
        return ApiResponse.success(service.register(input));
    }

    @PostMapping("/login")
    public ApiResponse<UserResponse> login(@Valid @RequestBody LoginRequest input,
            HttpServletRequest request, HttpServletResponse response) {
        service.validatePasswordBytes(input.getPassword());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(input.getUsername(), input.getPassword()));
        } catch (AuthenticationServiceException exception) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "AUTH_SERVICE_UNAVAILABLE", "登录服务暂时不可用，请稍后重试");
        } catch (AuthenticationException exception) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "LOGIN_FAILED", "用户名或密码错误");
        }
        UserResponse user = service.currentUser(authentication.getName());
        // JSON 登录同样执行会话固定攻击防护，并在登录后清除旧 CSRF 令牌。
        sessionStrategy.onAuthentication(authentication, request, response);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
        return ApiResponse.success(user);
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(Authentication authentication) {
        return ApiResponse.success(service.currentUser(authentication.getName()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(Authentication authentication,
            HttpServletRequest request, HttpServletResponse response) {
        csrfRepository.saveToken(null, request, response);
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        Cookie cookie = new Cookie("JSESSIONID", "");
        cookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ApiResponse.success(null);
    }
}
