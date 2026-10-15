package com.charity.dto;

import com.charity.entity.SysUser;
import com.charity.entity.UserRole;

public class UserResponse {
    private final Long id;
    private final String username;
    private final UserRole role;

    public UserResponse(SysUser user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole();
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public UserRole getRole() { return role; }
}
