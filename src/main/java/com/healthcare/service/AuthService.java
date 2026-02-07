package com.healthcare.service;

import com.healthcare.config.security.JwtUtil;
import com.healthcare.dto.LoginRequestDto;
import com.healthcare.dto.RegistrationRequestDto;
import com.healthcare.entity.UserEntity;
import com.healthcare.enums.Role;
import com.healthcare.repository.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PatientService patientService;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, PatientService patientService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.patientService = patientService;
    }

    public String login(LoginRequestDto request) {
        logger.info("Attempting login for user: {}", request.getEmail());
        logger.info("Received password length: {}", request.getPassword() != null ? request.getPassword().length() : "null");

        UserEntity userEntity = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (userEntity == null || !passwordEncoder.matches(request.getPassword(), userEntity.getPassword())) {
            logger.warn("Login failed: Invalid credentials for user {}", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        try {
            logger.info("Login successful for user: {}", request.getEmail());
            return jwtUtil.generateToken(userEntity.getEmail(), userEntity.getRole().name());
        } catch (Exception e) {
            logger.error("Error generating token for user {}: {}", request.getEmail(), e.getMessage());
            throw new RuntimeException("Error generating token: " + e.getMessage());
        }
    }

    public Map<String, Object> registerUser(UserEntity user, @Valid RegistrationRequestDto request) {
        logger.info("Registering new user: {}", user.getEmail());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        UserEntity savedUser = userRepository.save(user);

        if (savedUser.getRole() == Role.PATIENT) {
            logger.info("Creating patient profile for user ID: {}", savedUser.getId());
            patientService.createPatientFromUser(savedUser, request);
        }

        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());

        Map<String, Object> response = new HashMap<>();
        response.put("user", savedUser);
        response.put("token", token);
        return response;
    }
}
