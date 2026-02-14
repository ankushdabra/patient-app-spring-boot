package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.healthcare.enums.AppointmentStatus;
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
public class AppointmentResponseDto {
    private UUID id;
    private DoctorDetailResponseDto doctor;
    private UserProfileDto patient;
    private LocalDate appointmentDate;
    @JsonFormat(pattern = "hh:mm a")
    private LocalTime appointmentTime;
    private AppointmentStatus status;
}
