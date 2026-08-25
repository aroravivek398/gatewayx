package com.gatewayx.repository;

import com.gatewayx.entity.Developer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeveloperRepository extends JpaRepository<Developer,Long> {
    Optional<Developer> findByEmail(String email);
}
