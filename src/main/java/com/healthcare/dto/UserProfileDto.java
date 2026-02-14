package com.healthcare.dto;

import com.healthcare.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserProfileDto {
    private UUID id;
    private String name;
    private String email;
    private Role role;
    private Integer age;
    private String gender;
    private String bloodGroup;
    private DoctorDetailResponseDto doctorDetails;
}