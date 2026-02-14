package com.healthcare.controller;

import com.healthcare.dto.DoctorDetailResponseDto;
import com.healthcare.dto.DoctorResponseDto;
import com.healthcare.service.DoctorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(DoctorController.class);
    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping
    public ResponseEntity<List<DoctorResponseDto>> getAllDoctors() {
        logger.info("Fetching all doctors list");
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/{doctorId}")
    public ResponseEntity<DoctorDetailResponseDto> getDoctorDetail(@PathVariable UUID doctorId) {
        logger.info("Fetching details for doctor ID: {}", doctorId);
        return ResponseEntity.ok(doctorService.getDoctorDetail(doctorId));
    }
}
