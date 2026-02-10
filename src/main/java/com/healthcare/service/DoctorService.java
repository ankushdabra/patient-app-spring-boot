package com.healthcare.service;

import com.healthcare.dto.DoctorDetailResponseDto;
import com.healthcare.dto.DoctorRegistrationRequestDto;
import com.healthcare.dto.DoctorResponseDto;
import com.healthcare.dto.TimeSlotDto;
import com.healthcare.entity.DoctorAvailabilityEntity;
import com.healthcare.entity.DoctorEntity;
import com.healthcare.entity.UserEntity;
import com.healthcare.enums.Role;
import com.healthcare.repository.DoctorAvailabilityRepository;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.repository.UserRepository;
import com.healthcare.util.AvailabilityMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final AvailabilityMapper availabilityMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DoctorService(DoctorRepository doctorRepository, DoctorAvailabilityRepository availabilityRepository,
                         AvailabilityMapper availabilityMapper, UserRepository userRepository,
                         PasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.availabilityRepository = availabilityRepository;
        this.availabilityMapper = availabilityMapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<DoctorResponseDto> getAllDoctors() {
        return doctorRepository.findAll()
                .stream()
                .map(doctor -> DoctorResponseDto.builder()
                        .id(doctor.getId())
                        .name(doctor.getUser().getName())
                        .specialization(doctor.getSpecialization())
                        .experience(doctor.getExperience())
                        .consultationFee(doctor.getConsultationFee())
                        .rating(doctor.getRating())
                        .profileImage(doctor.getProfileImage())
                        .build())
                .toList();
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

    @Transactional
    public void createDoctor(DoctorRegistrationRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        UserEntity savedUser = userRepository.save(UserEntity.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.DOCTOR)
                .build());

        DoctorEntity doctor = DoctorEntity.builder()
                .user(savedUser)
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

        doctorRepository.save(doctor);
    }
}
