package com.healthcare.controller;

import com.healthcare.dto.ApiResponse;
import com.healthcare.dto.AppointmentRequestDto;
import com.healthcare.entity.UserEntity;
import com.healthcare.service.AppointmentService;
import com.healthcare.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserService userService;

    public AppointmentController(AppointmentService appointmentService, UserService userService) {
        this.appointmentService = appointmentService;
        this.userService = userService;
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping
    public ResponseEntity<ApiResponse> bookAppointment(@RequestBody AppointmentRequestDto request, Authentication authentication) {
        String email = (String) authentication.getPrincipal();

        UserEntity user = userService.getUserByEmail(email);

        appointmentService.bookAppointment(request, user.getId());

        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), "Appointment booked successfully"));
    }
}
