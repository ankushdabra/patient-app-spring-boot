package com.healthcare.service;

import com.healthcare.dto.DoctorDetailResponseDto;
import com.healthcare.dto.DoctorRegistrationRequestDto;
import com.healthcare.dto.DoctorResponseDto;
import com.healthcare.dto.TimeSlotDto;
import com.healthcare.dto.UpdateProfileRequestDto;
import com.healthcare.entity.DoctorAvailabilityEntity;
import com.healthcare.entity.DoctorEntity;
import com.healthcare.entity.UserEntity;
import com.healthcare.enums.AppointmentStatus;
import com.healthcare.enums.DayOfWeekEnum;
import com.healthcare.repository.AppointmentRepository;
import com.healthcare.repository.DoctorAvailabilityRepository;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.util.AvailabilityMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final AvailabilityMapper availabilityMapper;
    private final UserService userService;
    private final AppointmentRepository appointmentRepository;

    public DoctorService(DoctorRepository doctorRepository, DoctorAvailabilityRepository availabilityRepository,
                         AvailabilityMapper availabilityMapper, UserService userService, AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.availabilityRepository = availabilityRepository;
        this.availabilityMapper = availabilityMapper;
        this.userService = userService;
        this.appointmentRepository = appointmentRepository;
    }

    public List<DoctorResponseDto> getAllDoctors() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        return doctorRepository.findAll()
                .stream()
                .map(doctor -> {
                    List<DoctorAvailabilityEntity> availabilityEntities = availabilityRepository.findByDoctorId(doctor.getId());
                    String nextAvailable = availabilityEntities.stream()
                            .sorted(Comparator.comparing(DoctorAvailabilityEntity::getDay)
                                    .thenComparing(DoctorAvailabilityEntity::getStartTime))
                            .map(availability -> formatDayOfWeek(availability.getDay()) + ", " + availability.getStartTime().format(timeFormatter))
                            .findFirst()
                            .orElse("Not Available");

                    return DoctorResponseDto.builder()
                            .id(doctor.getId())
                            .name(doctor.getUser().getName())
                            .specialization(doctor.getSpecialization())
                            .experience(doctor.getExperience())
                            .consultationFee(doctor.getConsultationFee())
                            .rating(doctor.getRating())
                            .profileImage(doctor.getProfileImage())
                            .nextAvailable(nextAvailable)
                            .build();
                })
                .toList();
    }

    private String formatDayOfWeek(DayOfWeekEnum day) {
        return switch (day) {
            case MON -> "Monday";
            case TUE -> "Tuesday";
            case WED -> "Wednesday";
            case THU -> "Thursday";
            case FRI -> "Friday";
            case SAT -> "Saturday";
            case SUN -> "Sunday";
        };
    }

    public DoctorDetailResponseDto getDoctorDetail(UUID doctorId) {
        DoctorEntity doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        List<DoctorAvailabilityEntity> availabilityEntities = availabilityRepository.findByDoctorId(doctorId);

        Map<String, List<TimeSlotDto>> availabilityMap = availabilityMapper.map(availabilityEntities);

        return DoctorDetailResponseDto.builder()
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
    }

    public DoctorDetailResponseDto getDoctorProfile() {
        UserEntity user = userService.getCurrentUser();
        DoctorEntity doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        return getDoctorDetail(doctor.getId());
    }

    public BigDecimal getTodayEarnings() {
        UserEntity user = userService.getCurrentUser();
        DoctorEntity doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        BigDecimal earnings = appointmentRepository.calculateEarningsForDoctor(doctor.getId(), LocalDate.now(), AppointmentStatus.COMPLETED);
        return earnings != null ? earnings : BigDecimal.ZERO;
    }

    @Transactional
    public void createDoctorProfile(UserEntity user, DoctorRegistrationRequestDto request) {
        DoctorEntity doctor = DoctorEntity.builder()
                .user(user)
                .name(request.getName())
                .specialization(request.getSpecialization())
                .qualification(request.getQualification())
                .experience(request.getExperience())
                .consultationFee(request.getConsultationFee())
                .about(request.getAbout())
                .clinicAddress(request.getClinicAddress())
                .profileImage(request.getProfileImage())
                .rating(BigDecimal.ZERO)
                .build();

        DoctorEntity savedDoctor = doctorRepository.save(doctor);

        if (request.getAvailability() != null && !request.getAvailability().isEmpty()) {
            updateAvailability(savedDoctor, request.getAvailability());
        }
    }

    @Transactional
    public void updateDoctorProfile(UserEntity user, UpdateProfileRequestDto request) {
        DoctorEntity doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        if (request.getSpecialization() != null) {
            doctor.setSpecialization(request.getSpecialization());
        }
        if (request.getQualification() != null) {
            doctor.setQualification(request.getQualification());
        }
        if (request.getExperience() != null) {
            doctor.setExperience(request.getExperience());
        }
        if (request.getConsultationFee() != null) {
            doctor.setConsultationFee(request.getConsultationFee());
        }
        if (request.getAbout() != null) {
            doctor.setAbout(request.getAbout());
        }
        if (request.getClinicAddress() != null) {
            doctor.setClinicAddress(request.getClinicAddress());
        }
        if (request.getProfileImage() != null) {
            doctor.setProfileImage(request.getProfileImage());
        }

        doctorRepository.save(doctor);

        if (request.getAvailability() != null) {
            List<DoctorAvailabilityEntity> existingAvailability = availabilityRepository.findByDoctorId(doctor.getId());
            availabilityRepository.deleteAll(existingAvailability);
            updateAvailability(doctor, request.getAvailability());
        }
    }

    private void updateAvailability(DoctorEntity doctor, Map<String, List<TimeSlotDto>> availability) {
        List<DoctorAvailabilityEntity> availabilityEntities = new ArrayList<>();

        for (Map.Entry<String, List<TimeSlotDto>> entry : availability.entrySet()) {
            try {
                DayOfWeekEnum day = DayOfWeekEnum.valueOf(entry.getKey().toUpperCase());
                for (TimeSlotDto timeSlot : entry.getValue()) {
                    availabilityEntities.add(DoctorAvailabilityEntity.builder()
                            .doctor(doctor)
                            .day(day)
                            .startTime(parseTime(timeSlot.getStartTime()))
                            .endTime(parseTime(timeSlot.getEndTime()))
                            .build());
                }
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid day of week: " + entry.getKey());
            }
        }
        availabilityRepository.saveAll(availabilityEntities);
    }

    private LocalTime parseTime(String time) {
        try {
            return LocalTime.parse(time, DateTimeFormatter.ofPattern("hh:mm a"));
        } catch (Exception e) {
            return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
        }
    }
}
