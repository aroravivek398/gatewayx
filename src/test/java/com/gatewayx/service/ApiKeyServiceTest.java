package com.gatewayx.service;

import com.gatewayx.dto.request.CreateApiKeyRequest;
import com.gatewayx.dto.response.ApiKeyResponse;
import com.gatewayx.entity.*;
import com.gatewayx.exception.ApiKeyLimitExceededException;
import com.gatewayx.exception.DeveloperNotFoundException;
import com.gatewayx.exception.PlanNotFoundException;
import com.gatewayx.repository.ApiKeyRepository;
import com.gatewayx.repository.DeveloperRepository;
import com.gatewayx.repository.PlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private DeveloperRepository developerRepository;

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private ApiKeyService apiKeyService;

    @Test
    void create_shouldThrowException_whenPlanNotFound() {
        CreateApiKeyRequest request = new CreateApiKeyRequest();
        request.setPlanId(1L);

        when(planRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PlanNotFoundException.class, () -> apiKeyService.create(1L, request));
    }

    @Test
    void create_shouldThrowException_whenDeveloperNotFound() {
        CreateApiKeyRequest request = new CreateApiKeyRequest();
        request.setPlanId(1L);

        Plan plan = new Plan();
        plan.setId(1L);

        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(developerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(DeveloperNotFoundException.class, () -> apiKeyService.create(1L, request));
    }

    @Test
    void create_shouldThrowException_whenLimitExceeded() {
        CreateApiKeyRequest request = new CreateApiKeyRequest();
        request.setPlanId(1L);

        Plan plan = new Plan();
        plan.setId(1L);
        plan.setName("FREE");
        plan.setMaxApiKeysPerDeveloper(2);

        Developer developer = new Developer();
        developer.setId(1L);

        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(developerRepository.findById(1L)).thenReturn(Optional.of(developer));
        when(apiKeyRepository.countByDeveloperIdAndPlanIdAndStatus(1L, 1L, ApiKeyStatus.ACTIVE))
                .thenReturn(2L);

        assertThrows(ApiKeyLimitExceededException.class, () -> apiKeyService.create(1L, request));
    }

    @Test
    void create_shouldSucceed_whenUnderLimit() {
        CreateApiKeyRequest request = new CreateApiKeyRequest();
        request.setPlanId(1L);

        Plan plan = new Plan();
        plan.setId(1L);
        plan.setName("FREE");
        plan.setMaxApiKeysPerDeveloper(2);

        Developer developer = new Developer();
        developer.setId(1L);

        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(developerRepository.findById(1L)).thenReturn(Optional.of(developer));
        when(apiKeyRepository.countByDeveloperIdAndPlanIdAndStatus(1L, 1L, ApiKeyStatus.ACTIVE))
                .thenReturn(1L);

        ApiKey savedApiKey = new ApiKey();
        savedApiKey.setId(1L);
        savedApiKey.setKeyPrefix("gwx_live_ab");
        savedApiKey.setPlan(plan);
        savedApiKey.setStatus(ApiKeyStatus.ACTIVE);

        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(savedApiKey);

        ApiKeyResponse response = apiKeyService.create(1L, request);

        assertEquals("FREE", response.getPlanName());
        assertNotNull(response.getRawKey());
    }
}