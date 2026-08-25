package com.gatewayx.service;

import com.gatewayx.dto.request.LoginRequest;
import com.gatewayx.dto.request.RegisterDeveloperRequest;
import com.gatewayx.dto.response.DeveloperResponse;
import com.gatewayx.dto.response.LoginResponse;
import com.gatewayx.entity.Developer;
import com.gatewayx.exception.DeveloperNotFoundException;
import com.gatewayx.exception.InvalidCredentialsException;
import com.gatewayx.repository.DeveloperRepository;
import com.gatewayx.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeveloperService {

    private final DeveloperRepository developerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public DeveloperResponse register(RegisterDeveloperRequest request) {

        if(developerRepository.findByEmail(request.getEmail()).isPresent()){
            throw new RuntimeException("Email Already Exists");
        }
        Developer developer= new Developer();
        developer.setEmail(request.getEmail());
        developer.setName(request.getName());
        developer.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        Developer savedDeveloper = developerRepository.save(developer);

        DeveloperResponse developerResponse = new DeveloperResponse();
        developerResponse.setId(savedDeveloper.getId());
        developerResponse.setEmail(savedDeveloper.getEmail());
        developerResponse.setName(savedDeveloper.getName());
        developerResponse.setCreatedAt(savedDeveloper.getCreatedAt());

        return developerResponse;


    }
    public DeveloperResponse findDeveloper(Long id) {

        Developer developer = developerRepository.findById(id)
                .orElseThrow(() -> new DeveloperNotFoundException("Developer not found"));

        DeveloperResponse developerResponse = new DeveloperResponse();
        developerResponse.setId(developer.getId());
        developerResponse.setEmail(developer.getEmail());
        developerResponse.setName(developer.getName());
        developerResponse.setCreatedAt(developer.getCreatedAt());

        return developerResponse;
    }
    public LoginResponse login(LoginRequest loginRequest){
        Developer developer=developerRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(()-> new InvalidCredentialsException("Invalid Credentials"));
        if(!passwordEncoder.matches(loginRequest.getPassword(), developer.getPasswordHash())){
            throw new InvalidCredentialsException("Invalid Credentials");
        }
        String token= jwtService.generateToken(developer.getId());
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        return loginResponse;

    }

}