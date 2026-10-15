package com.charity;

import com.charity.entity.UserRole;
import com.charity.repository.*;
import com.charity.service.DemoDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

/** 仅连接显式配置的独立 MySQL 测试库；测试数据随事务回滚。 */
@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "MYSQL_INTEGRATION_TEST", matches = "true")
class MySqlFoundationTest {
    @Autowired private UserRepository users;
    @Autowired private ProjectRepository projects;
    @Autowired private DonationRepository donations;
    @Autowired private DemoDataService demoData;
    @Autowired private PasswordEncoder encoder;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void schemaMapsToJpaAndDemoInitializationIsRepeatable() {
        assertTrue(jdbc.queryForObject("SELECT VERSION()", String.class).startsWith("8.4."));
        String username = System.getenv("DEMO_ADMIN_USERNAME");
        String password = System.getenv("DEMO_ADMIN_PASSWORD");
        var admin = users.findByUsername(username).orElseThrow();
        assertEquals(UserRole.ADMIN, admin.getRole());
        assertTrue(encoder.matches(password, admin.getPasswordHash()));
        long userCount = users.count();
        long projectCount = projects.count();
        long donationCount = donations.count();
        demoData.initialize(username, password);
        demoData.initialize(username, password);
        assertEquals(userCount, users.count());
        assertEquals(projectCount, projects.count());
        assertEquals(donationCount, donations.count());
    }

    @Test
    void databaseRejectsZeroAndDuplicateRequest() {
        Long userId = users.findByUsername(System.getenv("DEMO_ADMIN_USERNAME")).orElseThrow().getId();
        Long projectId = projects.findAll().get(0).getId();
        String insert = "INSERT INTO donation(user_id, project_id, amount, request_id) VALUES (?, ?, ?, ?)";
        assertThrows(DataAccessException.class, () -> jdbc.update(insert, userId, projectId, 0, "zero-check"));
        jdbc.update(insert, userId, projectId, 10, "unique-check");
        assertThrows(DataAccessException.class, () -> jdbc.update(insert, userId, projectId, 10, "unique-check"));
    }

    @Test
    void databasePreservesDonationWhenDeletingItsProject() {
        Long userId = users.findByUsername(System.getenv("DEMO_ADMIN_USERNAME")).orElseThrow().getId();
        Long projectId = projects.findAll().get(0).getId();
        jdbc.update("INSERT INTO donation(user_id, project_id, amount, request_id) VALUES (?, ?, 10, 'foreign-key-check')", userId, projectId);
        assertThrows(DataAccessException.class, () -> jdbc.update("DELETE FROM charity_project WHERE id = ?", projectId));
    }
}
