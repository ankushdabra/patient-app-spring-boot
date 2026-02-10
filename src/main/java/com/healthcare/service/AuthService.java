package com.healthcare.service;

import com.healthcare.config.security.JwtUtil;
import com.healthcare.dto.LoginRequestDto;
import com.healthcare.dto.RegistrationRequestDto;
import com.healthcare.entity.UserEntity;
import com.healthcare.enums.Role;
import com.healthcare.repository.UserRepository;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

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

        UserEntity userEntity = userRepository.findByEmail(request.getEmail())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .orElseThrow(() -> {
                    logger.warn("Login failed: Invalid credentials for user {}", request.getEmail());
                    return new BadCredentialsException("Invalid email or password");
                });

        logger.info("Login successful for user: {}", request.getEmail());
        return jwtUtil.generateToken(userEntity.getEmail(), userEntity.getRole().name());
    }

    @Transactional
    public Map<String, Object> registerUser(@Valid RegistrationRequestDto request) {
        Role patientRole = Optional.ofNullable(request.getRole())
                .filter(StringUtils::isNotEmpty)
                .map(String::toUpperCase)
                .map(role -> {
                    try {
                        return Role.valueOf(role);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid role");
                    }
                })
                .orElse(Role.PATIENT);

        UserEntity savedUser = userRepository.save(UserEntity.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(patientRole)
                .build());

        logger.info("Registering new user: {}", savedUser.getEmail());

        if (savedUser.getRole() == Role.PATIENT) {
            logger.info("Creating patient profile for user ID: {}", savedUser.getId());
            patientService.createPatientFromUser(savedUser, request);
        }

        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());

        return Map.of("user", savedUser, "token", token);
    }
}
