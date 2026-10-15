package com.charity.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.*;

@Entity
@Table(name = "charity_project")
public class CharityProject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "target_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal targetAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ProjectStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected CharityProject() {}

    public CharityProject(String title, String description, BigDecimal targetAmount) {
        edit(title, description, targetAmount);
        this.status = ProjectStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }

    public void edit(String title, String description, BigDecimal targetAmount) {
        this.title = title;
        this.description = description;
        this.targetAmount = targetAmount;
    }

    public void publish() { this.status = ProjectStatus.ACTIVE; }
    public void close() { this.status = ProjectStatus.CLOSED; }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public BigDecimal getTargetAmount() { return targetAmount; }
    public ProjectStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
