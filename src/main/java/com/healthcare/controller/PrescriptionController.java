package com.healthcare.controller;

import com.healthcare.dto.ApiResponse;
import com.healthcare.dto.PrescriptionRequestDto;
import com.healthcare.entity.PrescriptionEntity;
import com.healthcare.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse> createPrescription(@Valid @RequestBody PrescriptionRequestDto request) {
        prescriptionService.createPrescription(request);
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), "Prescription created successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<PrescriptionEntity>> getMyPrescriptions() {
        return ResponseEntity.ok(prescriptionService.getMyPrescriptions());
    }

}