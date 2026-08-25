package com.gatewayx.service;

import com.gatewayx.dto.request.RegisterDeveloperRequest;
import com.gatewayx.dto.response.DeveloperResponse;
import com.gatewayx.entity.Developer;
import com.gatewayx.repository.DeveloperRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeveloperServiceTest {

    @Mock
    private DeveloperRepository developerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DeveloperService developerService;

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        RegisterDeveloperRequest request = new RegisterDeveloperRequest();
        request.setEmail("existing@example.com");
        request.setName("Test");
        request.setPassword("password123");

        when(developerRepository.findByEmail("existing@example.com"))
                .thenReturn(Optional.of(new Developer()));

        assertThrows(RuntimeException.class, () -> developerService.register(request));
    }
    @Test
    void register_shouldSucceed_whenEmailDoesNotExist() {
        RegisterDeveloperRequest request = new RegisterDeveloperRequest();
        request.setEmail("new@example.com");
        request.setName("Vivek");
        request.setPassword("password123");

        when(developerRepository.findByEmail("new@example.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashedPassword123");

        Developer fakeSavedDeveloper = new Developer();
        fakeSavedDeveloper.setId(1L);
        fakeSavedDeveloper.setEmail("new@example.com");
        fakeSavedDeveloper.setName("Vivek");

        when(developerRepository.save(any(Developer.class)))
                .thenReturn(fakeSavedDeveloper);

        DeveloperResponse response = developerService.register(request);

        assertEquals("new@example.com", response.getEmail());
        assertEquals(1L, response.getId());
    }
}