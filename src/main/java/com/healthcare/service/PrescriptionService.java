package com.healthcare.service;

import com.healthcare.dto.PrescriptionRequestDto;
import com.healthcare.dto.PrescriptionResponseDto;
import com.healthcare.entity.AppointmentEntity;
import com.healthcare.entity.DoctorEntity;
import com.healthcare.entity.PatientEntity;
import com.healthcare.entity.PrescriptionEntity;
import com.healthcare.entity.UserEntity;
import com.healthcare.enums.AppointmentStatus;
import com.healthcare.enums.Role;
import com.healthcare.repository.AppointmentRepository;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserService userService;
    private final PatientService patientService;
    private final DoctorRepository doctorRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               AppointmentRepository appointmentRepository,
                               UserService userService, PatientService patientService,
                               DoctorRepository doctorRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.appointmentRepository = appointmentRepository;
        this.userService = userService;
        this.patientService = patientService;
        this.doctorRepository = doctorRepository;
    }

    public void createPrescription(PrescriptionRequestDto request) {
        AppointmentEntity appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        PrescriptionEntity prescription = PrescriptionEntity.builder()
                .patient(appointment.getPatient().getId())
                .doctor(request.getDoctorId())
                .appointment(appointment)
                .medications(request.getMedications())
                .instructions(request.getInstructions())
                .notes(request.getNotes())
                .prescriptionDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();

        prescriptionRepository.save(prescription);

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponseDto> getMyPrescriptions() {
        UserEntity currentUser = userService.getCurrentUser();
        List<PrescriptionEntity> prescriptions;

        if (currentUser.getRole() == Role.PATIENT) {
            prescriptions = prescriptionRepository.findByPatientWithDetails(patientService.getPatientByUserId(currentUser.getId())
                    .map(PatientEntity::getId)
                    .orElse(null));
        } else if (currentUser.getRole() == Role.DOCTOR) {
            DoctorEntity doctor = doctorRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Doctor profile not found"));
            prescriptions = prescriptionRepository.findByDoctorWithDetails(doctor.getId());
        } else {
            prescriptions = Collections.emptyList();
        }
        
        return prescriptions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PrescriptionResponseDto getPrescriptionById(UUID prescriptionId) {
        PrescriptionEntity prescription = prescriptionRepository.findByIdWithDetails(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));
        return mapToDto(prescription);
    }

    private PrescriptionResponseDto mapToDto(PrescriptionEntity entity) {
        return PrescriptionResponseDto.builder()
                .id(entity.getId())
                .medications(entity.getMedications())
                .instructions(entity.getInstructions())
                .notes(entity.getNotes())
                .prescriptionDate(entity.getPrescriptionDate())
                .appointmentId(entity.getAppointment().getId())
                .appointmentDate(entity.getAppointment().getAppointmentDate())
                .doctorName(entity.getAppointment().getDoctor().getUser().getName())
                .patientName(entity.getAppointment().getPatient().getUser().getName())
                .build();
    }
}
