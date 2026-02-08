package com.healthcare.service;

import com.healthcare.dto.AppointmentRequestDto;
import com.healthcare.dto.AppointmentResponseDto;
import com.healthcare.dto.DoctorDetailResponseDto;
import com.healthcare.dto.TimeSlotDto;
import com.healthcare.entity.AppointmentEntity;
import com.healthcare.entity.DoctorAvailabilityEntity;
import com.healthcare.entity.DoctorEntity;
import com.healthcare.entity.PatientEntity;
import com.healthcare.entity.UserEntity;
import com.healthcare.enums.AppointmentStatus;
import com.healthcare.repository.AppointmentRepository;
import com.healthcare.repository.DoctorAvailabilityRepository;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.repository.PatientRepository;
import com.healthcare.util.AvailabilityMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository repository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserService userService;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final AvailabilityMapper availabilityMapper;

    public AppointmentService(AppointmentRepository repository, PatientRepository patientRepository, DoctorRepository doctorRepository, UserService userService, DoctorAvailabilityRepository doctorAvailabilityRepository, AvailabilityMapper availabilityMapper) {
        this.repository = repository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.userService = userService;
        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
        this.availabilityMapper = availabilityMapper;
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
        DoctorEntity doctor = entity.getDoctor();
        List<DoctorAvailabilityEntity> availabilityEntities = doctorAvailabilityRepository.findByDoctorId(doctor.getId());

        Map<String, List<TimeSlotDto>> availabilityMap = availabilityMapper.map(availabilityEntities);

        DoctorDetailResponseDto doctorDto = DoctorDetailResponseDto.builder()
                .id(doctor.getId())
                .name(doctor.getUser().getName())
                .specialization(doctor.getSpecialization())
                .qualification(doctor.getQualification())
                .experience(doctor.getExperience())
                .rating(doctor.getRating())
                .consultationFee(doctor.getConsultationFee())
                .about(doctor.getAbout())
                .clinicAddress(doctor.getClinicAddress())
                .profileImage(doctor.getProfileImage())
                .availability(availabilityMap)
                .build();

        return AppointmentResponseDto.builder()
                .id(entity.getId())
                .doctor(doctorDto)
                .appointmentDate(entity.getAppointmentDate())
                .appointmentTime(entity.getAppointmentTime())
                .status(entity.getStatus())
                .build();
    }
}
