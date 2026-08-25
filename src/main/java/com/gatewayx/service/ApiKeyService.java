package com.gatewayx.service;

import com.gatewayx.dto.request.CreateApiKeyRequest;
import com.gatewayx.dto.response.ApiKeyResponse;
import com.gatewayx.entity.ApiKey;
import com.gatewayx.entity.ApiKeyStatus;
import com.gatewayx.entity.Developer;
import com.gatewayx.entity.Plan;
import com.gatewayx.exception.ApiKeyLimitExceededException;
import com.gatewayx.exception.ApiKeyOwnershipException;
import com.gatewayx.exception.DeveloperNotFoundException;
import com.gatewayx.exception.PlanNotFoundException;
import com.gatewayx.repository.ApiKeyRepository;
import com.gatewayx.repository.DeveloperRepository;
import com.gatewayx.repository.PlanRepository;
import com.gatewayx.util.ApiKeyGenerator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final DeveloperRepository developerRepository;
    private final PlanRepository planRepository;

    public ApiKeyResponse create(Long developerId, CreateApiKeyRequest request) {

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new PlanNotFoundException("Plan not found"));
        Developer developer = developerRepository.findById(developerId)
                .orElseThrow(() -> new DeveloperNotFoundException("Developer not found"));
        Integer maxLimit = plan.getMaxApiKeysPerDeveloper();
        if (maxLimit != null) {
            long count = apiKeyRepository.countByDeveloperIdAndPlanIdAndStatus(developerId, plan.getId(), ApiKeyStatus.ACTIVE);
            if (count >= maxLimit) throw new ApiKeyLimitExceededException("Api-Key Limit exceeded!");
        }
        String rawKey = ApiKeyGenerator.generateRawKey();
        String rawKeyHash = ApiKeyGenerator.hashKey(rawKey);
        String keyPrefix = rawKey.substring(0, 11);
        ApiKey apiKey = new ApiKey();
        apiKey.setKeyHash(rawKeyHash);
        apiKey.setKeyPrefix(keyPrefix);
        apiKey.setPlan(plan);
        apiKey.setDeveloper(developer);
        ApiKey savedApiKey = apiKeyRepository.save(apiKey);

        ApiKeyResponse response = new ApiKeyResponse();
        response.setId(savedApiKey.getId());
        response.setKeyPrefix(savedApiKey.getKeyPrefix());
        response.setRawKey(rawKey);
        response.setCreatedAt(savedApiKey.getCreatedAt());
        response.setPlanName(plan.getName());
        response.setStatus(savedApiKey.getStatus());
        return response;
    }

    public List<ApiKeyResponse> listByDeveloper(Long developerId) {
        List<ApiKey> keys = apiKeyRepository.findByDeveloperId(developerId);

        return keys.stream()
                .map(key -> {
                    ApiKeyResponse response = new ApiKeyResponse();
                    response.setId(key.getId());
                    response.setKeyPrefix(key.getKeyPrefix());
                    response.setPlanName(key.getPlan().getName());
                    response.setCreatedAt(key.getCreatedAt());
                    response.setStatus(key.getStatus());

                    return response;
                })
                .toList();
    }

    public void revoke(Long keyId,Long developerId) {
        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new RuntimeException("API Key not found"));

        if (!apiKey.getDeveloper().getId().equals(developerId)) {
            throw new ApiKeyOwnershipException("You do not have permission to revoke this API key");
        }
        apiKey.setStatus(ApiKeyStatus.REVOKED);
        apiKey.setRevokedAt(LocalDateTime.now());
        apiKeyRepository.save(apiKey);
    }
}