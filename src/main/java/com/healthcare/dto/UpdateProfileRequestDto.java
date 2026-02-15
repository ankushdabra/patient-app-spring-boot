package com.healthcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequestDto {
    private String name;
    private String email;
    
    // Patient fields
    private Integer age;
    private String gender;
    private String bloodGroup;
    private Double weight;
    private Double height;

    // Doctor fields
    private String specialization;
    private String qualification;
    private Integer experience;
    private BigDecimal consultationFee;
    private String about;
    private String clinicAddress;
    private String profileImage;
    private Map<String, List<TimeSlotDto>> availability;
}
