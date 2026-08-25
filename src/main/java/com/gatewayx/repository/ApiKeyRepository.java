package com.gatewayx.repository;

import com.gatewayx.entity.ApiKey;
import com.gatewayx.entity.ApiKeyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface ApiKeyRepository extends JpaRepository<ApiKey,Long> {
    long countByDeveloperIdAndPlanIdAndStatus(Long developerId, Long planId, ApiKeyStatus status);
    Optional<ApiKey> findByKeyHash(String keyHash);
    List<ApiKey> findByDeveloperId(Long developerId);
}
