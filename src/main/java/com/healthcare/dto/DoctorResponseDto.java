package com.healthcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorResponseDto {
    private UUID id;
    private String name;
    private String specialization;
    private Integer experience;
    private BigDecimal consultationFee;
}
