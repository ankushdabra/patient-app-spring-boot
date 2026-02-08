package com.healthcare.service;

import com.healthcare.dto.RegistrationRequestDto;
import com.healthcare.entity.PatientEntity;
import com.healthcare.entity.UserEntity;
import com.healthcare.repository.PatientRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

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
                .build());
    }
}