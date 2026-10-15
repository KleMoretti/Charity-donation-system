package com.charity.service;

import com.charity.dto.HealthResponse;
import javax.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HealthService {
    private final EntityManager entityManager;

    public HealthService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public HealthResponse check() {
        entityManager.createNativeQuery("SELECT 1").getSingleResult();
        return new HealthResponse();
    }
}
