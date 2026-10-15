package com.charity.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.*;

@Entity
@Table(name = "donation", uniqueConstraints = @UniqueConstraint(
        name = "uk_donation_user_request", columnNames = {"user_id", "request_id"}))
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private SysUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private CharityProject project;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "request_id", nullable = false, length = 64)
    private String requestId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Donation() {}

    public Donation(SysUser user, CharityProject project, BigDecimal amount, String requestId) {
        this.user = user;
        this.project = project;
        this.amount = amount;
        this.requestId = requestId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public SysUser getUser() { return user; }
    public CharityProject getProject() { return project; }
    public BigDecimal getAmount() { return amount; }
    public String getRequestId() { return requestId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
