package com.healthcare.service;

import com.healthcare.dto.PrescriptionRequestDto;
import com.healthcare.entity.AppointmentEntity;
import com.healthcare.entity.PatientEntity;
import com.healthcare.entity.PrescriptionEntity;
import com.healthcare.entity.UserEntity;
import com.healthcare.repository.AppointmentRepository;
import com.healthcare.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserService userService;
    private final PatientService patientService;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               AppointmentRepository appointmentRepository,
                               UserService userService, PatientService patientService) {
        this.prescriptionRepository = prescriptionRepository;
        this.appointmentRepository = appointmentRepository;
        this.userService = userService;
        this.patientService = patientService;
    }

    public void createPrescription(PrescriptionRequestDto request) {
        AppointmentEntity appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        PrescriptionEntity prescription = PrescriptionEntity.builder()
                .patient(appointment.getPatient().getId())
                .appointment(appointment)
                .medications(request.getMedications())
                .instructions(request.getInstructions())
                .notes(request.getNotes())
                .prescriptionDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();

        prescriptionRepository.save(prescription);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionEntity> getMyPrescriptions() {
        UserEntity currentUser = userService.getCurrentUser();
        return prescriptionRepository.findByPatient(patientService.getPatientByUserId(currentUser.getId())
                .map(PatientEntity::getId)
                .orElse(null));
    }

    @Transactional(readOnly = true)
    public PrescriptionEntity getPrescriptionById(UUID prescriptionId) {
        return prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));
    }
}
