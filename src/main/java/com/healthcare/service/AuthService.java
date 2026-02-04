package com.healthcare.service;

import com.healthcare.config.JwtUtil;
import com.healthcare.dto.LoginRequestDto;
import com.healthcare.entity.UserEntity;
import com.healthcare.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String login(LoginRequestDto request) {
        UserEntity userEntity = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        //Pass
        if (!passwordEncoder.matches(request.getPassword(), userEntity.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        try {
            return jwtUtil.generateToken(userEntity.getEmail(), userEntity.getRole().name());
        } catch (Exception e) {
            throw new RuntimeException("Error generating token: " + e.getMessage());
        }
    }
}
