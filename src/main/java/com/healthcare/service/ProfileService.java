package com.healthcare.service;

import com.healthcare.dto.UpdateProfileRequestDto;
import com.healthcare.dto.UserProfileDto;
import com.healthcare.entity.UserEntity;
import com.healthcare.enums.Role;
import com.healthcare.repository.PatientRepository;
import com.healthcare.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                    .bloodGroup(patient.getBloodGroup())
                    .weight(patient.getWeight())
                    .height(patient.getHeight()));
        } else if (user.getRole() == Role.DOCTOR) {
            profileBuilder.doctorDetails(doctorService.getDoctorProfile());
        }

        return profileBuilder.build();
    }

    @Transactional
    public void updateProfile(String email, UpdateProfileRequestDto request) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

        if (request.getName() != null) user.setName(request.getName());
        
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
             if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                 throw new RuntimeException("Email already in use");
             }
             user.setEmail(request.getEmail());
        }
        
        userRepository.save(user);

        if (user.getRole() == Role.PATIENT) {
            patientRepository.findByUser(user).ifPresent(patient -> {
                if (request.getAge() != null) patient.setAge(request.getAge());
                if (request.getGender() != null) patient.setGender(request.getGender());
                if (request.getBloodGroup() != null) patient.setBloodGroup(request.getBloodGroup());
                if (request.getWeight() != null) patient.setWeight(request.getWeight());
                if (request.getHeight() != null) patient.setHeight(request.getHeight());
                patientRepository.save(patient);
            });
        } else if (user.getRole() == Role.DOCTOR) {
            doctorService.updateDoctorProfile(user, request);
        }
    }
}
