package com.healthcare.service;

import com.healthcare.dto.AppointmentRequestDto;
import com.healthcare.dto.AppointmentResponseDto;
import com.healthcare.dto.DoctorDetailResponseDto;
import com.healthcare.dto.TimeSlotDto;
import com.healthcare.dto.TodayAppointmentsResponseDto;
import com.healthcare.dto.UserProfileDto;
import com.healthcare.entity.AppointmentEntity;
import com.healthcare.entity.DoctorAvailabilityEntity;
import com.healthcare.entity.DoctorEntity;
import com.healthcare.entity.PatientEntity;
import com.healthcare.entity.UserEntity;
import com.healthcare.enums.AppointmentStatus;
import com.healthcare.enums.Role;
import com.healthcare.repository.AppointmentRepository;
import com.healthcare.repository.DoctorAvailabilityRepository;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.repository.PatientRepository;
import com.healthcare.util.AvailabilityMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
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
    @Value("${app.timezone:Asia/Kolkata}")
    private String appTimezone;

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

        if (repository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(request.getDoctorId(), request.getAppointmentDate(), request.getAppointmentTime())) {
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

    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getAppointments() {
        UserEntity user = userService.getCurrentUser();
        ZoneId zoneId = ZoneId.of(appTimezone);
        LocalDate currentDate = LocalDate.now(zoneId);
        LocalTime currentTime = LocalTime.now(zoneId);

        List<AppointmentEntity> appointments;
        if (user.getRole() == Role.DOCTOR) {
            DoctorEntity doctor = doctorRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Doctor profile not found"));
            appointments = repository.findUpcomingAppointmentsForDoctor(doctor.getId(), currentDate, currentTime, AppointmentStatus.BOOKED);
        } else {
            PatientEntity patient = patientRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Patient profile not found"));
            appointments = repository.findUpcomingAppointmentsForPatient(patient.getId(), currentDate, currentTime, AppointmentStatus.BOOKED);
        }

        List<UUID> doctorIds = appointments.stream().map(a -> a.getDoctor().getId()).distinct().collect(Collectors.toList());
        Map<UUID, List<DoctorAvailabilityEntity>> availabilitiesByDoctor = doctorAvailabilityRepository.findByDoctorIdIn(doctorIds)
                .stream()
                .collect(Collectors.groupingBy(da -> da.getDoctor().getId()));

        return appointments.stream()
                .map(appointment -> mapToDtoWithAvailability(appointment, availabilitiesByDoctor.getOrDefault(appointment.getDoctor().getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TodayAppointmentsResponseDto getTodayAppointments() {
        UserEntity user = userService.getCurrentUser();
        if (user.getRole() != Role.DOCTOR) {
            throw new RuntimeException("Access denied: Only doctors can view today's appointments");
        }

        DoctorEntity doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        ZoneId zoneId = ZoneId.of(appTimezone);
        LocalDate currentDate = LocalDate.now(zoneId);
        LocalTime currentTime = LocalTime.now(zoneId);

        List<AppointmentEntity> appointments = repository.findTodayAppointmentsForDoctor(doctor.getId(), currentDate, currentTime, AppointmentStatus.BOOKED);
        
        List<UUID> doctorIds = appointments.stream().map(a -> a.getDoctor().getId()).distinct().collect(Collectors.toList());
        Map<UUID, List<DoctorAvailabilityEntity>> availabilitiesByDoctor = doctorAvailabilityRepository.findByDoctorIdIn(doctorIds)
                .stream()
                .collect(Collectors.groupingBy(da -> da.getDoctor().getId()));

        List<AppointmentResponseDto> appointmentDtos = appointments.stream()
                .map(appointment -> mapToDtoWithAvailability(appointment, availabilitiesByDoctor.getOrDefault(appointment.getDoctor().getId(), Collections.emptyList())))
                .collect(Collectors.toList());

        BigDecimal totalEarnings = repository.calculateTotalEarningsByDoctorAndDateAndStatus(doctor.getId(), currentDate, AppointmentStatus.COMPLETED);
        if (totalEarnings == null) {
            totalEarnings = BigDecimal.ZERO;
        }

        return TodayAppointmentsResponseDto.builder()
                .appointments(appointmentDtos)
                .totalEarnings(totalEarnings)
                .build();
    }

    @Transactional(readOnly = true)
    public AppointmentResponseDto getAppointmentById(UUID appointmentId) {
        UserEntity user = userService.getCurrentUser();
        AppointmentEntity appointment = repository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if ((user.getRole() == Role.PATIENT && !appointment.getPatient().getUser().getId().equals(user.getId())) ||
                (user.getRole() == Role.DOCTOR && !appointment.getDoctor().getUser().getId().equals(user.getId()))) {
            throw new RuntimeException("Access denied: You can only view your own appointments");
        }

        List<DoctorAvailabilityEntity> availabilities = doctorAvailabilityRepository.findByDoctorId(appointment.getDoctor().getId());
        return mapToDtoWithAvailability(appointment, availabilities);
    }

    private AppointmentResponseDto mapToDtoWithAvailability(AppointmentEntity entity, List<DoctorAvailabilityEntity> availabilityEntities) {
        DoctorEntity doctor = entity.getDoctor();
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

        UserProfileDto patientDto = UserProfileDto.builder()
                .id(entity.getPatient().getId())
                .name(entity.getPatient().getUser().getName())
                .email(entity.getPatient().getUser().getEmail())
                .role(entity.getPatient().getUser().getRole())
                .age(entity.getPatient().getAge())
                .gender(entity.getPatient().getGender())
                .bloodGroup(entity.getPatient().getBloodGroup())
                .build();

        return AppointmentResponseDto.builder()
                .id(entity.getId())
                .doctor(doctorDto)
                .patient(patientDto)
                .appointmentDate(entity.getAppointmentDate())
                .appointmentTime(entity.getAppointmentTime())
                .status(entity.getStatus())
                .build();
    }
}
