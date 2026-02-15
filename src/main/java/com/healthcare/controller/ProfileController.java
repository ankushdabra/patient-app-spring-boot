package com.healthcare.controller;

import com.healthcare.dto.ApiResponse;
import com.healthcare.dto.UpdateProfileRequestDto;
import com.healthcare.dto.UserProfileDto;
import com.healthcare.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<UserProfileDto> getUserProfile(Authentication authentication) {
        return ResponseEntity.ok(profileService.getUserProfile(authentication.getName()));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<ApiResponse> updateProfile(@RequestBody UpdateProfileRequestDto request, Authentication authentication) {
        profileService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), "Profile updated successfully"));
    }
}
