package com.charity.dto;

import javax.validation.constraints.*;

public class RegisterRequest {
    @NotBlank(message = "请输入用户名")
    @Pattern(regexp = "[a-zA-Z0-9_]{3,50}", message = "用户名需为3至50位字母、数字或下划线")
    private String username;

    @NotBlank(message = "请输入密码")
    @Size(min = 8, max = 72, message = "密码需为8至72个字符，且UTF-8最多72字节")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
