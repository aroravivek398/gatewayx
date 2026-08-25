package com.gatewayx.repository;

import com.gatewayx.entity.UsageAggregate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface UsageAggregateRepository extends JpaRepository<UsageAggregate, Long> {
    Optional<UsageAggregate> findByApiKeyIdAndPeriodStart(Long apiKeyId, LocalDate periodStart);
}