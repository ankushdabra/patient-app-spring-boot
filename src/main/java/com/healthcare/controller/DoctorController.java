package com.healthcare.controller;

import com.healthcare.dto.DoctorDetailResponseDto;
import com.healthcare.dto.DoctorResponseDto;
import com.healthcare.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping
    public List<DoctorResponseDto> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/{doctorId}")
    public ResponseEntity<DoctorDetailResponseDto> getDoctorDetail(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(doctorService.getDoctorDetail(doctorId));
    }
}
