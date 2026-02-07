package com.healthcare.controller;

import com.healthcare.dto.ApiResponse;
import com.healthcare.dto.LoginRequestDto;
import com.healthcare.dto.RegistrationRequestDto;
import com.healthcare.entity.UserEntity;
import com.healthcare.enums.Role;
import com.healthcare.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequestDto request) {
        String token = authService.login(request);
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), token));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationRequestDto request) {
        UserEntity user = new UserEntity();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        if (request.getRole() != null && !request.getRole().isEmpty()) {
            try {
                user.setRole(Role.valueOf(request.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Invalid role"));
            }
        } else {
            user.setRole(Role.PATIENT);
        }

        Map<String, Object> response = authService.registerUser(user, request);
        return ResponseEntity.ok(response);
    }
}