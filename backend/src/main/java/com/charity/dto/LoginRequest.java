package com.charity.dto;

import javax.validation.constraints.*;

public class LoginRequest {
    @NotBlank(message = "请输入用户名")
    @Size(max = 50, message = "用户名不能超过50个字符")
    private String username;

    @NotBlank(message = "请输入密码")
    @Size(max = 72, message = "密码不能超过72个字符")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
