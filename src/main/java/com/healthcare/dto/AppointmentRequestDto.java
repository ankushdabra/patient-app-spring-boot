package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentRequestDto {
    @NotNull
    @Schema(type = "string", example = "298e0933-32ef-4fc2-9360-60f4ed6096b2")
    private UUID doctorId;

    @NotNull
    @Schema(type = "string", example = "2026-02-06")
    private LocalDate appointmentDate;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "hh:mm a")
    @Schema(type = "string", example = "10:00 AM")
    private LocalTime appointmentTime;
}
