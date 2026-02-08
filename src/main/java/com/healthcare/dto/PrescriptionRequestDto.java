package com.healthcare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionRequestDto {
    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    private UUID appointmentId;

    @NotBlank(message = "Medications cannot be empty")
    private String medications;

    private String instructions;

    private String notes;
}