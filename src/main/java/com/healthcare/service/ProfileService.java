package com.healthcare.service;

import com.healthcare.dto.UpdateProfileRequestDto;
import com.healthcare.dto.UserProfileDto;
import com.healthcare.entity.UserEntity;
import com.healthcare.enums.Role;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.repository.PatientRepository;
import com.healthcare.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public ProfileService(UserRepository userRepository, PatientRepository patientRepository, DoctorRepository doctorRepository,
                          DoctorService doctorService, PatientService patientService) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    public UserProfileDto getUserProfile(String email) {
        UserEntity user = findUserByEmail(email);

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
            doctorRepository.findByUserId(user.getId())
                    .ifPresent(doctor -> profileBuilder.doctorDetails(doctorService.getDoctorDetail(doctor.getId())));
        }

        return profileBuilder.build();
    }

    @Transactional
    public void updateProfile(String email, UpdateProfileRequestDto request) {
        UserEntity user = findUserByEmail(email);

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }

        userRepository.save(user);

        if (user.getRole() == Role.PATIENT) {
            patientService.updatePatientProfile(user, request);
        } else if (user.getRole() == Role.DOCTOR) {
            doctorService.updateDoctorProfile(user, request);
        }
    }

    private UserEntity findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));
    }
}
