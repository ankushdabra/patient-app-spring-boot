package com.healthcare.service;

import com.healthcare.dto.AppointmentRequestDto;
import com.healthcare.entity.AppointmentEntity;
import com.healthcare.entity.DoctorEntity;
import com.healthcare.entity.PatientEntity;
import com.healthcare.enums.AppointmentStatus;
import com.healthcare.repository.AppointmentRepository;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AppointmentService {

    private final AppointmentRepository repository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(AppointmentRepository repository, PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.repository = repository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    public void bookAppointment(AppointmentRequestDto request, UUID userId) {

        // 🔒 Prevent double booking
        boolean slotTaken = repository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(request.getDoctorId(),
                request.getAppointmentDate(),
                request.getAppointmentTime());

        if (slotTaken) {
            throw new RuntimeException("Slot already booked");
        }

        PatientEntity patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Patient profile not found. Please complete your profile first."));

        DoctorEntity doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        repository.save(AppointmentEntity.builder()
                .doctor(doctor)
                .patient(patient)
                .appointmentDate(request.getAppointmentDate())
                .appointmentTime(request.getAppointmentTime())
                .status(AppointmentStatus.BOOKED)
                .build());
    }
}
