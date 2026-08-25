package com.gatewayx.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;



@Entity
@Table(name = "api_keys")
@Getter
@Setter
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String keyHash;

    @Column(nullable = false)
    private String keyPrefix;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApiKeyStatus status=ApiKeyStatus.ACTIVE;

    private LocalDateTime createdAt;

    private LocalDateTime revokedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @ManyToOne
    @JoinColumn(name = "developer_id", nullable = false)
    private Developer developer;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;


}