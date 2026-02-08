package com.healthcare.service;

import com.healthcare.dto.PrescriptionRequestDto;
import com.healthcare.dto.PrescriptionResponseDto;
import com.healthcare.entity.AppointmentEntity;
import com.healthcare.entity.PatientEntity;
import com.healthcare.entity.PrescriptionEntity;
import com.healthcare.entity.UserEntity;
import com.healthcare.enums.Role;
import com.healthcare.repository.AppointmentRepository;
import com.healthcare.repository.PatientRepository;
import com.healthcare.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserService userService;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               PatientRepository patientRepository,
                               AppointmentRepository appointmentRepository,
                               UserService userService) {
        this.prescriptionRepository = prescriptionRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.userService = userService;
    }

    public void createPrescription(PrescriptionRequestDto request) {
        UserEntity doctor = userService.getCurrentUser();
        
        PatientEntity patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        AppointmentEntity appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        PrescriptionEntity prescription = PrescriptionEntity.builder()
                .doctor(doctor)
                .patient(patient)
                .appointment(appointment)
                .medications(request.getMedications())
                .instructions(request.getInstructions())
                .notes(request.getNotes())
                .prescriptionDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();

        prescriptionRepository.save(prescription);
    }

    public List<PrescriptionResponseDto> getMyPrescriptions() {
        UserEntity currentUser = userService.getCurrentUser();
        
        PatientEntity patient = patientRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        return prescriptionRepository.findByPatientId(patient.getId()).stream()
                .map(entity -> PrescriptionResponseDto.builder()
                        .id(entity.getId())
                        .doctorName(entity.getDoctor().getName())
                        .patientName(entity.getPatient().getUser().getName())
                        .medications(entity.getMedications())
                        .instructions(entity.getInstructions())
                        .prescriptionDate(entity.getPrescriptionDate())
                        .build())
                .collect(Collectors.toList());
    }

    public PrescriptionResponseDto getPrescriptionDetail(UUID id) {
        UserEntity currentUser = userService.getCurrentUser();
        PrescriptionEntity prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));

        if (currentUser.getRole() == Role.PATIENT) {
            if (!prescription.getPatient().getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Unauthorized access to prescription");
            }
        }

        return PrescriptionResponseDto.builder()
                .id(prescription.getId())
                .doctorName(prescription.getDoctor().getName())
                .patientName(prescription.getPatient().getUser().getName())
                .medications(prescription.getMedications())
                .instructions(prescription.getInstructions())
                .prescriptionDate(prescription.getPrescriptionDate())
                .notes(prescription.getNotes())
                //.appointmentId(prescription.getAppointmentId())
                .createdAt(prescription.getCreatedAt())
                .build();
    }
}