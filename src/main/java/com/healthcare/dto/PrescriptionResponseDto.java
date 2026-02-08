package com.healthcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionResponseDto {
    private UUID id;
    private String doctorName;
    private String patientName;
    private String medications;
    private String instructions;
    private LocalDate prescriptionDate;
    private LocalDateTime createdAt;
    private String notes;
}