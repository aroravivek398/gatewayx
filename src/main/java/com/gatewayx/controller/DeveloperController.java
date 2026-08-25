package com.gatewayx.controller;

import com.gatewayx.dto.request.LoginRequest;
import com.gatewayx.dto.request.RegisterDeveloperRequest;
import com.gatewayx.dto.response.ApiKeyResponse;
import com.gatewayx.dto.response.DeveloperResponse;
import com.gatewayx.dto.response.LoginResponse;
import com.gatewayx.service.ApiKeyService;
import com.gatewayx.service.DeveloperService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/developers")
@RequiredArgsConstructor
public class DeveloperController {


    private final DeveloperService developerService;
    private final ApiKeyService apiKeyService;
    @PostMapping("register")
    public ResponseEntity<DeveloperResponse> register(@Valid @RequestBody RegisterDeveloperRequest request) {
        DeveloperResponse response = developerService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeveloperResponse> findDeveloper(@PathVariable Long id) {
        DeveloperResponse response = developerService.findDeveloper(id);
        return ResponseEntity.ok(response);
    }
    @PostMapping("login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        LoginResponse response=developerService.login(request);
        return ResponseEntity.ok(response);
    }
}
