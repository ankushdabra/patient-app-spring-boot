package com.healthcare.controller;

import com.healthcare.dto.ApiResponse;
import com.healthcare.dto.AppointmentRequestDto;
import com.healthcare.dto.AppointmentResponseDto;
import com.healthcare.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping
    public ResponseEntity<ApiResponse> bookAppointment(@RequestBody AppointmentRequestDto request) {
        appointmentService.bookAppointment(request);
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), "Appointment booked successfully"));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping
    public ResponseEntity<List<AppointmentResponseDto>> getAppointments() {
        List<AppointmentResponseDto> appointments = appointmentService.getAppointments();
        return ResponseEntity.ok(appointments);
    }
}
