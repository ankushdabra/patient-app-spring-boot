package com.healthcare.service;

import com.healthcare.dto.UserProfileDto;
import com.healthcare.entity.UserEntity;
import com.healthcare.enums.Role;
import com.healthcare.repository.PatientRepository;
import com.healthcare.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;

    public ProfileService(UserRepository userRepository, PatientRepository patientRepository, DoctorService doctorService) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
    }

    public UserProfileDto getUserProfile(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

        UserProfileDto.UserProfileDtoBuilder profileBuilder = UserProfileDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole());

        if (user.getRole() == Role.PATIENT) {
            patientRepository.findByUser(user).ifPresent(patient -> profileBuilder
                    .age(patient.getAge())
                    .gender(patient.getGender())
                    .bloodGroup(patient.getBloodGroup()));
        } else if (user.getRole() == Role.DOCTOR) {
            profileBuilder.doctorDetails(doctorService.getDoctorProfile());
        }

        return profileBuilder.build();
    }
}