package com.gatewayx.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "usage_aggregates")
@Getter
@Setter
public class UsageAggregate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long apiKeyId;

    private Long requestCount = 0L;

    private LocalDate periodStart;
}