package com.charity.service;

import com.charity.entity.*;
import com.charity.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import javax.sql.DataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoDataService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final DataSource dataSource;

    public DemoDataService(UserRepository users, PasswordEncoder encoder, DataSource dataSource) {
        this.users = users;
        this.encoder = encoder;
        this.dataSource = dataSource;
    }

    @Transactional
    public void initialize(String username, String password) {
        if (username == null || !username.matches("[a-zA-Z0-9_]{3,50}") || password == null
                || password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72
                || username.startsWith("<") || password.startsWith("<")) {
            throw new IllegalStateException("演示初始化需要有效的管理员用户名和8位以上、UTF-8不超过72字节的密码环境变量");
        }
        users.findByUsername(username).ifPresentOrElse(existing -> {
            if (existing.getRole() != UserRole.ADMIN) {
                throw new IllegalStateException("演示管理员用户名已被普通用户占用，请更换初始化用户名");
            }
        }, () -> users.saveAndFlush(new SysUser(username, encoder.encode(password), UserRole.ADMIN)));
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("db/demo-projects.sql"));
        populator.setSqlScriptEncoding("UTF-8");
        populator.execute(dataSource);
    }
}
