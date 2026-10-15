package com.charity.config;

import com.charity.service.DemoDataService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoDataInitializer implements CommandLineRunner {
    private final DemoDataService service;
    private final String username;
    private final String password;

    public DemoDataInitializer(DemoDataService service,
            @Value("${app.demo.admin-username}") String username,
            @Value("${app.demo.admin-password}") String password) {
        this.service = service;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(String... args) { service.initialize(username, password); }
}
