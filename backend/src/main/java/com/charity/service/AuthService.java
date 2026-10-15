package com.charity.service;

import com.charity.dto.*;
import com.charity.entity.*;
import com.charity.exception.BusinessException;
import com.charity.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest input) {
        validatePasswordBytes(input.getPassword());
        if (users.findByUsername(input.getUsername()).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "USERNAME_EXISTS", "用户名已被使用");
        }
        // 角色由后端固定，输入 DTO 不包含角色字段。
        return new UserResponse(users.saveAndFlush(new SysUser(
                input.getUsername(), encoder.encode(input.getPassword()), UserRole.USER)));
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(String username) {
        return new UserResponse(users.findByUsername(username).orElseThrow(() ->
                new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "登录状态已失效，请重新登录")));
    }

    public void validatePasswordBytes(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "密码UTF-8编码不能超过72字节");
        }
    }
}
