package com.gatewayx.repository;

import com.gatewayx.entity.UsageEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsageEventRepository extends JpaRepository<UsageEvent, Long> {
}