package com.healthcare.service;

import com.healthcare.dto.AppointmentRequestDto;
import com.healthcare.dto.AppointmentResponseDto;
import com.healthcare.entity.AppointmentEntity;
import com.healthcare.entity.DoctorEntity;
import com.healthcare.entity.PatientEntity;
import com.healthcare.entity.UserEntity;
import com.healthcare.enums.AppointmentStatus;
import com.healthcare.repository.AppointmentRepository;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository repository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserService userService;

    public AppointmentService(AppointmentRepository repository, PatientRepository patientRepository, DoctorRepository doctorRepository, UserService userService) {
        this.repository = repository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.userService = userService;
    }

    public void bookAppointment(AppointmentRequestDto request) {
        UserEntity user = userService.getCurrentUser();

        // 🔒 Prevent double booking
        boolean slotTaken = repository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(request.getDoctorId(),
                request.getAppointmentDate(),
                request.getAppointmentTime());

        if (slotTaken) {
            throw new RuntimeException("Slot already booked");
        }

        PatientEntity patient = patientRepository.findByUserId(user.getId())
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

    public List<AppointmentResponseDto> getAppointments() {
        UserEntity user = userService.getCurrentUser();

        PatientEntity patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        return repository.findByPatientId(patient.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public AppointmentResponseDto getAppointmentById(UUID appointmentId) {
        UserEntity user = userService.getCurrentUser();

        AppointmentEntity appointment = repository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Security check: Ensure the appointment belongs to the current user
        if (!appointment.getPatient().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: You can only view your own appointments");
        }

        return mapToDto(appointment);
    }

    private AppointmentResponseDto mapToDto(AppointmentEntity entity) {
        return AppointmentResponseDto.builder()
                .id(entity.getId())
                .doctorId(entity.getDoctor().getId())
                .doctorName(entity.getDoctor().getName())
                .specialization(entity.getDoctor().getSpecialization())
                .appointmentDate(entity.getAppointmentDate())
                .appointmentTime(entity.getAppointmentTime())
                .status(entity.getStatus())
                .build();
    }
}
