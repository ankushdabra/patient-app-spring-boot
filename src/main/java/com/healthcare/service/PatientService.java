package com.healthcare.service;

import com.healthcare.dto.RegistrationRequestDto;
import com.healthcare.dto.UpdateProfileRequestDto;
import com.healthcare.entity.PatientEntity;
import com.healthcare.entity.UserEntity;
import com.healthcare.repository.PatientRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public void createPatientFromUser(UserEntity user, @Valid RegistrationRequestDto request) {
        patientRepository.save(PatientEntity.builder().user(user)
                .age(request.getAge())
                .gender(request.getGender())
                .bloodGroup(request.getBloodGroup())
                .weight(request.getWeight())
                .height(request.getHeight())
                .build());
    }

    public Optional<PatientEntity> getPatientByUserId(UUID userId) {
        return patientRepository.findByUserId(userId);
    }

    @Transactional
    public void updatePatientProfile(UserEntity user, UpdateProfileRequestDto request) {
        PatientEntity patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Patient profile not found for user"));

        if (request.getAge() != null) patient.setAge(request.getAge());
        if (request.getGender() != null) patient.setGender(request.getGender());
        if (request.getBloodGroup() != null) patient.setBloodGroup(request.getBloodGroup());
        if (request.getWeight() != null) patient.setWeight(request.getWeight());
        if (request.getHeight() != null) patient.setHeight(request.getHeight());

        patientRepository.save(patient);
    }
}
