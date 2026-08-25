package com.gatewayx.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "usage_events")
@Getter
@Setter
public class UsageEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long apiKeyId;

    @Column(nullable = false)
    private String endpoint;

    private int statusCode;

    private LocalDateTime occurredAt;
}