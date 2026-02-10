package com.healthcare.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class PrescriptionResponseDto {
    private UUID id;
    private String medications;
    private String instructions;
    private String notes;
    private LocalDate prescriptionDate;
    private UUID appointmentId;
    private LocalDate appointmentDate;
    private String doctorName;
    private String patientName;
}
